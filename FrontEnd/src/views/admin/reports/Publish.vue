<script setup lang="ts">
import {computed, onMounted, reactive} from 'vue'
import {ElDialog, ElMessageBox, ElSelect, ElLoading,ElMessage } from 'element-plus'
import { ref } from 'vue'
import type { UploadUserFile } from 'element-plus'
import {Delete, Plus, UploadFilled} from "@element-plus/icons-vue"
import request from "@/utils/request";
import {baseURL} from "@/utils/request";
import {getTimeStamp} from "@/utils/date";
import {TestCase, LabReport, Section} from "@/entities"
import { nextTick} from 'vue'

const file = ref<UploadUserFile[]>()

const beforeUpload =(selectedFile) =>{
  const postfix=selectedFile.name.substring(selectedFile.name.lastIndexOf('.')+1)
  const pass = postfix === 'docx'
  if(!pass) {
    ElMessageBox.alert('请上传docx格式的实验报告', '提示', {
      confirmButtonText: '确定'
    })
    file.value.pop()
  }
}


const data = reactive({
  templates: [],
  languages: [],
  sections: [],
  decPolicy: 'NONE',
  curQuestion: 0,
  visibleDialogIdx: -1,
  startDateTime: 0,
  testCases: [],
});
const shouldShowDialog = computed({
  get: () => data.visibleDialogIdx === data.curQuestion,
  set: () => data.visibleDialogIdx = -1
});


const form = reactive({} as LabReport)
const onAddCases = () => {
  data.visibleDialogIdx = -1
  data.curQuestion = data.testCases.length
  tempCase.input = ''
  tempCase.output = ''
}

const tempCase = reactive({} as TestCase)
const deleteCase = (index: number) => {
  data.testCases[data.curQuestion].splice(index, 1)
}
const deleteLast = () => {
  ElMessageBox.confirm(`确定删除第${data.testCases.length}题的测试用例吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    data.testCases.pop()
    data.curQuestion = data.testCases.length
  }).catch(() => {
  })
}
const onAddCase = () => {
  if(data.testCases[data.curQuestion] === undefined){
    data.testCases[data.curQuestion] = []
  }
  data.testCases[data.curQuestion].push({
    input: tempCase.input,
    output: tempCase.output
  })
  tempCase.input = ''
  tempCase.output = ''
}

const onPublish = async () => {
  //校验表单
  if (file.value === undefined || file.value.length === 0) {
    ElMessageBox.alert('请上传实验报告', '提示', {
      confirmButtonText: '确定'
    })
    return
  }
  if (form.language === '' || selectedSections.value.length === 0 || form.labId === 0) {
    ElMessageBox.alert('请补全实验相关信息，如班级、语言、实验ID等', '提示', {
      confirmButtonText: '确定'
    })
    return
  }
  if (data.decPolicy !== 'NONE') {
    if (data.startDateTime === 0 || form.gap === 0 || form.unit === '' || form.decScore === 0) {
      ElMessageBox.alert('您开启了罚分策略，请补充相关信息', '提示', {
        confirmButtonText: '确定'
      })
      return
    }
  }else {
    form.decScore = 0
  }
  //将form的testCase转为Map<Integer,List<TestCase>>
  const testCases = new Map<number, Array<TestCase>>()
  for (let i = 0; i < data.testCases.length; i++) {
    testCases.set(i + 1, data.testCases[i])
  }
  form.testCases = Object.fromEntries(testCases)

  form.publishDateTime = getTimeStamp(new Date().getTime())
  form.startDateTime = getTimeStamp(data.startDateTime)
  form.fileName = file.value[0].name
  const reportJson = JSON.stringify(form)
  const reportBlob = new Blob([reportJson], {
    type: 'application/json',
  });
  const body = new FormData()
  body.append('file', file.value[0].raw)
  body.append('report', reportBlob)
  body.append('sections', selectedSections.value.join(','))
  const loading = ElLoading.service({
    lock: true,
    text: '发布中...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  await request.post('/admin/labReport', body).then(
      async res => {
        console.log(res)
        loading.close()
        if (res.success) {
          ElMessage.success('发布成功')
        }else {
          await ElMessageBox.alert('发布失败:' + err, '提示', {
            confirmButtonText: '确定'
          })
        }
      }
  )

}


onMounted(() => {
  request.get('/labReport/languages').then(
      res => {
        data.languages = res.data.languages
      }
  )
  request.get('/section/all').then(
      res => {
        res.data.sections.forEach((value: Section) => {
          data.sections.push(value.section)
        })
      }
  )
  request.get('/admin/template/all').then(
      res => {
        data.templates.push(...res.data.templates)
      }
  )

})

const selectedSection = ref('')
const selectedSections = ref([])
const selectVisible = ref(false)
const SelectRef = ref<InstanceType<typeof ElSelect>>()

const handleClose = (tag: string) => {
  selectedSections.value.splice(selectedSections.value.indexOf(tag), 1)
  console.log(selectedSections.value)
}

const showSelect = () => {
  selectVisible.value = true
  nextTick(() => {
    SelectRef.value!.toggleMenu();
  })
}

const handleSelectConfirm = () => {
  if (!selectedSections.value.includes(selectedSection.value)) {
    selectedSections.value.push(selectedSection.value)
  }
  selectVisible.value = false
  selectedSection.value = ''
  console.log(selectedSections.value)
}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 600px;max-width: 600px;" >
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">发布一份实验</span>
    </template>
  <el-form  label-width="auto" style="max-width: 600px; height: 67vh;overflow-y: auto">
    <el-form-item label="实验报告" class="custom-label-color">
      <el-upload
          v-model:file-list="file"
          drag
          :action="`${baseURL}`+'/admin/labReport'"
          :before-upload="beforeUpload"
          :limit="1"
          style="width: 600px;"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到这里或 <em>点击上传文件</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传docx格式的实验报告
          </div>
        </template>
      </el-upload>
    </el-form-item>
    <el-form-item label="模板">
      <el-select v-model="form.templateId" placeholder="选择模板">
        <el-option
            v-for="item in data.templates"
            :key="item"
            :label="item"
            :value="item"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="语言">
      <el-select v-model="form.language" placeholder="选择语言">
        <el-option
            v-for="item in data.languages"
            :key="item"
            :label="item"
            :value="item"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="班级">
      <el-tag
          v-for="tag in selectedSections"
          :key="tag"
          closable
          @close="handleClose(tag)"
          style="margin-right: 10px;margin-bottom: 10px"
      >
        {{ tag }}
      </el-tag>
      <el-select
          v-if="selectVisible"
          ref="SelectRef"
          fit-input-width
          @change="handleSelectConfirm"
          v-model="selectedSection" placeholder="选择班级">
        <el-option
            v-for="item in data.sections"
            :key="item"
            :label="item"
            :value="item"
        />
      </el-select>
      <el-button v-else class="button-new-tag" size="default" @click="showSelect" style="margin-bottom: 10px">
        + 添加班级
      </el-button>

    </el-form-item>
    <el-form-item label="实验ID">
      <el-input-number v-model="form.labId" :min="1" :max="16"/>
    </el-form-item>
    <el-form-item label="测试用例">
      <el-button v-for="(value , index) in data.testCases" :key="value" type="primary" @click="data.curQuestion = data.visibleDialogIdx = index">
        {{ index + 1 }}
      </el-button>
      <el-button type="primary" @click="data.visibleDialogIdx = data.curQuestion">
        <el-icon size="10" color="#24613b" ><plus/></el-icon>
      </el-button>
      <el-button v-if="data.testCases.length>0" type="primary" @click="deleteLast">
        <el-icon size="10" color="#24613b" ><delete/></el-icon>
      </el-button>
      <div>
        <el-dialog
            v-model="shouldShowDialog"
            :title="`添加第${ data.curQuestion + 1 }题的测试用例`"
            width="600px"
            :show-close="false"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :destroy-on-close="true"
        >
          <el-table :data="data.testCases[data.curQuestion]" style="width: auto" max-height="250">
            <el-table-column type="index" label="序号" width="80px" />
            <el-table-column prop="input" label="输入" width="200px" />
            <el-table-column prop="output" label="输出" width="200px" />
            <el-table-column fixed="right" label="操作" width="80px">
              <template #default="scope">
                <el-button
                    link
                    type="primary"
                    size="small"
                    @click.prevent="deleteCase(scope.$index)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-row>
            <el-col :span="11">
              <el-form-item label="输入">
                <el-input v-model="tempCase.input" />
              </el-form-item>
            </el-col>
            <el-col :span="2">

            </el-col>
            <el-col :span="11">
              <el-form-item label="输出">
                <el-input v-model="tempCase.output" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-button class="mt-4" style="width: 100%" @click="onAddCase">添加</el-button>


          <template #footer>
            <div class="dialog-footer">
              <el-button @click="data.curQuestion = 1 + data.testCases.length + (data.visibleDialogIdx = -1)">取消</el-button>
              <el-button type="primary" @click="onAddCases">
                添加
              </el-button>
            </div>
          </template>
        </el-dialog>
      </div>
    </el-form-item>
    <el-form-item label="罚分策略">
      <el-radio-group v-model="data.decPolicy">
        <el-radio value="NONE">无</el-radio>
        <el-radio value="TIME">超时罚分</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="开始时间">
      <el-date-picker
          v-model="data.startDateTime"
          type="datetime"
          placeholder="Pick a Date"
          format="YYYY/MM/DD HH:mm:ss"
          value-format="x"
          :disabled="data.decPolicy === 'NONE'"
      />
    </el-form-item>
    <el-form-item label="间隔" >
      <el-col :span="12">
        <el-input v-model="form.gap" :disabled="data.decPolicy === 'NONE'" />
      </el-col>
      <el-col :span="1" class="text-center">
        <span class="text-gray-500"></span>
      </el-col>
      <el-col :span="11">
        <el-select v-model="form.unit" placeholder="单位" :disabled="data.decPolicy === 'NONE'">
          <el-option label="分钟" value="MINUTE"/>
          <el-option label="小时" value="HOUR"/>
          <el-option label="天" value="DAY"/>
        </el-select>
      </el-col>
    </el-form-item>
    <el-form-item label="处罚分值">
      <el-input-number v-model="form.decScore" :min="1" :max="100" :disabled="data.decPolicy === 'NONE'"/>
    </el-form-item>
  </el-form>
    <template #footer>
      <el-button class="publish" type="primary" @click="onPublish" style="width: 100%">发布</el-button>
    </template>
  </el-card>
</template>

<style scoped>
.el-icon--upload{
  font-size: 60px;
  color: #3399ff;
}
</style>