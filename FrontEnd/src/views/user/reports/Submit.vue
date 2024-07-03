<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import request from "@/utils/request.js";
import {LabReport, SubmittedReport} from "@/entities"
import {ElDialog, ElMessage, ElMessageBox, UploadUserFile} from "element-plus";
import {SuccessFilled, UploadFilled, WarnTriangleFilled} from "@element-plus/icons-vue";
import {baseURL} from "@/utils/request";
import {getTimeStamp} from "@/utils/date";
import {useUserStore} from "@/stores/user";

const getReports = async () => {
  loading.value = true
  await request.get('/publishedReports/section',{params:{section:useUserStore().section}}).then(
      res => {
        res.data.reports.forEach((item: LabReport) => {
          if(item.decScore === 0) {
            item.startDateTime = '本题不罚分'
            item.gap = null
            item.unit = null
          }
        })
        data.push(...res.data.reports)
        loading.value = false
      }
  )
}
const getSubmitStates = async () => {
  await request.get('/submittedRecord/user').then(
      res => {
        submitStates.value = new Map(Object.entries(res.data.records));
      }
  )
}

onMounted(() => {
  getReports()
  getSubmitStates()
})
const loading = ref(true)
const data = reactive([] as LabReport[])
const submitStates = ref({} as Map<string,string>)
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
const download = async (fileName: string) => {
    await request.get("/publishedReports/url", {params: {fileName: fileName}}).then(
        res => {
          const link = document.createElement('a');
          link.href = res.data.url;
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
    )
}
const hasFile = (fileName: string) => {
  //如果是有has属性的话
  return ('has' in submitStates.value) && (submitStates.value.has(fileName))
}

const correct = async (labName: string, reportName: string) => {
  const formData = new FormData();
  formData.append('labName',labName)
  formData.append('reportName',reportName)
  await request.post('/user/labReport/correction', formData).then(
      res => {
        console.log(res)
        if (res.success){
          ElMessage.success('批改完成')
        }else{
          ElMessage.error(res.message)
        }
      }
  )
}

const dialogsVisible = ref([] as boolean[])
const toSubmit = (index: number) => {
  // await request.post('/user/labReport', {fileName: data[0].fileName}).then()
  file.value = []
  dialogsVisible.value[index] = true
}
const submit = async (index: number) => {
  const submittedReport: SubmittedReport = {
    language: data[index].language,
    labId: data[index].labId,
    section: data[index].section,
    studentId: useUserStore().id,
    labName: data[index].fileName,
    fileName: file.value[0].name,
    submitDateTime: getTimeStamp(new Date().getTime()),
  }
  const json = JSON.stringify(submittedReport)
  const blob = new Blob([json], {
    type: 'application/json',
  });
  const body = new FormData()
  body.append('file', file.value[0].raw)
  body.append('report', blob)
  await request.post('/user/labReport', body).then(
      async res => {
        console.log(res)
        if (res.success){
          dialogsVisible.value[index] = false
          ElMessage.success('提交成功,正在批改中...')
          await getSubmitStates()
          await correct(data[index].fileName,file.value[0].name)
        }else{
          ElMessage.error(res.message)
        }
      }
  )


}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%;" max-height="600">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">班级列表</span>
    </template>
  <el-table
      v-loading="loading"
      :data="data"
      :default-sort="{ prop: 'publishDateTime', order: 'descending' }"
      style="width: 100%"
  >
    <el-table-column prop="publishDateTime" label="发布时间" sortable min-width="180" align="center" />
    <el-table-column prop="language" label="语言" min-width="80" align="center" />
    <el-table-column prop="labId" label="实验ID" min-width="80" align="center" />
    <el-table-column prop="section" label="班级" min-width="100" align="center" />
    <el-table-column prop="fileName" label="文件名" min-width="280" align="center"/>
    <el-table-column prop="startDateTime" label="开始罚分时间" min-width="200" align="center" />
    <el-table-column prop="gap" label="间隔" min-width="100" align="center" />
    <el-table-column label="时间单位" min-width="100" align="center">
      <template #default="{row}">
        <span v-if="row.unit === 'MINUTE'">分钟</span>
        <span v-else-if="row.unit === 'HOUR'">小时</span>
        <span v-else-if="row.unit === 'DAY'">天</span>
        <span v-else></span>
      </template>
    </el-table-column>
    <el-table-column prop="decScore" label="扣除分数" min-width="100" align="center" />


    <el-table-column fixed="right" label="提交状态" min-width="100" align="center">
      <template #default="scope">
        <div style="margin-top: 5px">
          <el-icon v-if="hasFile(data[scope.$index].fileName)" color="#00BFFF" size="20"><SuccessFilled /></el-icon>
          <el-icon v-else color="#F18F01" size="20"><WarnTriangleFilled /></el-icon>
        </div>
      </template>
    </el-table-column>
    <el-table-column fixed="right" label="操作" min-width="150" align="center">
      <template #default="scope">
        <el-button link type="primary" size="small" @click.prevent="download(data[scope.$index].fileName)">下载</el-button>
<!--        :disabled="hasFile(data[scope.$index].fileName)"-->
        <el-button link type="primary" size="small" @click.prevent="toSubmit(scope.$index)"  >提交</el-button>
        <el-dialog
            :title="`当前正在提交实验:${data[scope.$index].fileName}`"
            v-model="dialogsVisible[scope.$index]"
            width="600px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :destroy-on-close="true"
        >
          <el-upload
              v-model:file-list="file"
              drag
              :action="`${baseURL}/user/labReport`"
              :before-upload="beforeUpload"
              :limit="1"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到这里或 <em>点击上传文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                注意：只有一次提交机会，请上传docx类型且格式正确的实验报告
              </div>
            </template>
          </el-upload>
          <el-button type="primary" @click="submit(scope.$index)">确认提交</el-button>
        </el-dialog>
      </template>
    </el-table-column>

  </el-table>
  </el-card>
</template>

<style scoped>
.el-icon--upload{
  font-size: 60px;
  color: #3399ff;
}
</style>