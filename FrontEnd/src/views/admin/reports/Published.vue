<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage, ElMessageBox} from "element-plus"
import {Refresh, UploadFilled} from "@element-plus/icons-vue"
import {LabReport, SubmittedReport} from "@/entities"
import {baseURL} from "@/utils/request";
import {getTimeStamp} from "@/utils/date";

const getReports = async () => {
  loading.value = true
  await request.get('/publishedReports').then(
      res => {
        data.push(...res.data.reports)
        loading.value = false
      }
  )
}
const getSectionCounts = async () => {
  await request.get('/section/counts').then(
      res => {
        sectionCounts.value = res.data.counts as Map<string,number>
      }
  )
}
const getCommittedCounts = async () => {
  await request.get('/submittedRecord/reports').then(
      res => {
        committedCounts.value = res.data.records as Map<string,number>
      }
  )
}
onMounted(() => {
  getReports()
  getSectionCounts()
  getCommittedCounts()
})

const loading = ref(true)
const isRefreshing = ref(false)
const showUpload = ref(false)
const refreshData = () => {
  getSectionCounts()
  getCommittedCounts()
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
  }, 1000)
}
const beforeUpload =(selectedFile) =>{
  const postfix=selectedFile.name.substring(selectedFile.name.lastIndexOf('.')+1)
  const pass = postfix === 'docx'
  if(!pass) {
    ElMessage.error('请上传docx格式的实验报告')
    fileList.value.pop()
  }
  else if (!selectedFile.name.match(studentIdReg)) {
    ElMessageBox.alert(`实验报告《${selectedFile.name}》的文件名中似乎没有合法的学号，请检查文件名格式是否符合要求`,'提示', {
      confirmButtonText: '确定'
    })
    fileList.value.pop()
  }
}

const data = reactive([] as LabReport[])
const proxyLab = ref({} as LabReport)
const sectionCounts = ref<Map<string,number>>(new Map())
const committedCounts = ref<Map<string,number>>(new Map())
const dialogsVisible = ref(false)
const submittedRecords = ref(new Array<{ studentId:string,fileName:string }>())
const fileList = ref<UploadUserFile[]>()
const studentIdReg = /\d{12}/g
const deleteRow = (fileName: string) => {
  ElMessageBox.confirm('确定移除该实验报告？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete("/admin/publishedReports", {params: {fileName: fileName}}).then(
        res => {
          console.log(res)
          if (res.success) {
            data.splice(data.findIndex(item => item.fileName === fileName), 1)
          }else {
            ElMessageBox.alert(res.message, '提示', {
              confirmButtonText: '确定'
            })
          }
        }
    )

  }).catch(() => {})
}

const submitDetail = (fileName: string) => {
  request.get('/submittedRecord/lab', {params: {labName: fileName}}).then(
      res => {
        submittedRecords.value = (res.data.records as Array<string>).map(item => {
          const idAndFileName = item.split(':')
          return {
            studentId: idAndFileName[0],
            fileName: idAndFileName[1]
          }
        })
      }
  )
  dialogsVisible.value = true
}
const proxySubmit = (labReport :LabReport) => {
  proxyLab.value = labReport
  showUpload.value = true
}
const onCancelSubmit = () => {
  showUpload.value = false
  fileList.value = []
  proxyLab.value = {} as LabReport
}

const onSubmit = async () => {
  showUpload.value = false
  const submittedReport: SubmittedReport = {
    language: proxyLab.value.language,
    labId: proxyLab.value.labId,
    section: proxyLab.value.section,
    studentId: '',
    labName: proxyLab.value.fileName,
    fileName: '',
    submitDateTime: getTimeStamp(new Date().getTime()),
  }
  for (const file of fileList.value) {
    submittedReport.studentId = file.name.match(studentIdReg)![0] as string
    submittedReport.fileName = file.name
    const json = JSON.stringify(submittedReport)
    const blob = new Blob([json], {
      type: 'application/json',
    });
    const body = new FormData()
    body.append('file', file.raw)
    body.append('report', blob)
    ElMessage.success(`正在提交 ${fileList.value.indexOf(file) + 1}/${fileList.value.length}`)
    await request.post('/admin/submit/proxy', body).then(
        async res => {
          if (res.success) {
            ElMessage.success(`提交成功,已批改 ${fileList.value.indexOf(file) + 1}/${fileList.value.length}`)
          } else {
            ElMessage.error(`第 ${fileList.value.indexOf(file) + 1}/${fileList.value.length} 批改失败 : ` + res.message)
          }
        }
    )
  }
  fileList.value = []
  proxyLab.value = {} as LabReport
}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%" max-height="700">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">已发布实验列表</span>
    </template>
  <el-table

      v-loading="loading"
      :data="data"
      :default-sort="{ prop: 'publishDateTime', order: 'descending' }"
      style="width: 100%;"
      max-height="700"
  >
    <el-table-column prop="publishDateTime" label="发布时间" sortable min-width="180" align="center" />
    <el-table-column prop="language" label="语言" min-width="80" align="center" />
    <el-table-column prop="labId" label="实验ID" min-width="80" align="center" />
    <el-table-column prop="section" label="班级" min-width="100" align="center" />
    <el-table-column prop="fileName" label="文件名" min-width="280" align="center" />
    <el-table-column width="180"  align="center">
      <template #default="scope">
        <span>{{ committedCounts[scope.row.fileName] + '/' + sectionCounts[scope.row.section] }}</span>
      </template>
      <template #header>
        <div style="display: flex; justify-content: center; position: relative; left: 13%;">
          <div>已提交/班级人数</div>
          <el-button :class="{ 'rotate-animation': isRefreshing }" link type="primary" size="small" @click="refreshData">
            <el-icon><refresh/></el-icon>
          </el-button>
        </div>
      </template>

    </el-table-column>
    <el-table-column fixed="right" label="操作" min-width="180" align="center">
      <template #default="scope">
        <el-button link type="primary" size="small" @click.prevent="deleteRow(scope.row.fileName)">移除</el-button>
        <el-button link type="primary" size="small" @click.prevent="submitDetail(scope.row.fileName)">提交详情</el-button>
          <el-dialog
              :title="`《 ${scope.row.fileName} 》的提交详情`"
              v-model="dialogsVisible"
              width="600px"
              max-height="600"
              :close-on-click-modal="false"
              :close-on-press-escape="false"
              :destroy-on-close="true"
              :show-close="false"
          >
            <div>
              <el-table
                  :data="submittedRecords"
                  :default-sort="{ prop: 'studentId', order: 'descending' }"
                  style="width: auto"
              >
                <el-table-column fixed prop="studentId" label="学号" sortable width="200" align="center" />
                <el-table-column prop="fileName" label="提交文件名" width="360" align="center" />
              </el-table>
            </div>
            <el-button type="primary" @click="dialogsVisible = false" style="margin-top: 10px">确定</el-button>
          </el-dialog>
        <el-button link type="primary" size="small" @click.prevent="proxySubmit(scope.row)">代理提交</el-button>
        <el-dialog
            :title="`《 ${scope.row.fileName} 》的提交详情`"
            v-model="dialogsVisible"
            width="600px"
            max-height="600"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :destroy-on-close="true"
            :show-close="false"
        >
          <div>
            <el-table
                :data="submittedRecords"
                :default-sort="{ prop: 'studentId', order: 'descending' }"
                style="width: auto"
            >
              <el-table-column fixed prop="studentId" label="学号" sortable width="200" align="center" />
              <el-table-column prop="fileName" label="提交文件名" width="360" align="center" />
            </el-table>
          </div>
          <el-button type="primary" @click="dialogsVisible = false" style="margin-top: 10px">确定</el-button>
        </el-dialog>
      </template>

    </el-table-column>
  </el-table>
  <el-dialog
      :title="`正在代理提交：${proxyLab.fileName}`"
      v-model="showUpload"
      width="600px"
      max-height="600"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :destroy-on-close="true"
      :show-close="false"

  >
    <div>
      <el-form label-width="auto" style="max-width: 600px;height: 400px;overflow-y: auto">
        <el-form-item class="custom-label-color">
          <el-upload
              v-model:file-list="fileList"
              drag
              :action="`${baseURL}/admin/submit/proxy`"
              :before-upload="beforeUpload"
              multiple
              style="width: 600px;"

          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到这里或 <em>点击批量上传文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传docx格式的实验报告
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </div>
    <el-button @click="onCancelSubmit" style="margin-top: 10px;width: 20%">取消</el-button>
    <el-button type="primary" @click="onSubmit" style="margin-top: 10px;width: 75%">提交并批改</el-button>

  </el-dialog>
  </el-card>
</template>

<style scoped>
@keyframes rotateAnimation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.rotate-animation {
  animation: rotateAnimation 0.5s linear infinite;
}
.el-icon--upload{
  font-size: 60px;
  color: #3399ff;
}
</style>
