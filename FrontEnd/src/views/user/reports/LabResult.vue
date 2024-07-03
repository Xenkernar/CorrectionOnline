<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import request from "@/utils/request.js";
import {LabResult, QuestionResult} from "@/entities"
import {SuccessFilled, WarnTriangleFilled} from "@element-plus/icons-vue";
import {ElTable} from "element-plus";

const getMyResults = async () => {
  loading.value = true
  await request.get('/user/labResult/all').then(
      res => {
        results.push(...res.data.results)
        loading.value = false
      }
  )
}
onMounted(() => {
  getMyResults()
})
const loading = ref(true)
const results = reactive([] as LabResult[])
const details = reactive([] as Array<QuestionResult>);
const dialogsVisible = ref(false)
const mapDetail = (results) => {
  details.splice(0,details.length)
  Object.values(results).forEach((item) => {
    details.push(item)
  })
  dialogsVisible.value = true
}
const isPass = (results) => {
  let res = true
  Object.values(results).forEach((item) => {
    res &= item.correct
  })
  return res
}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%;">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">实验结果</span>
    </template>
  <el-table
      v-loading="loading"
      :data="results"
      :default-sort="{ prop: 'submittedTime', order: 'descending' }"
      style="width: 100%"
  >
    <el-table-column prop="submittedTime" label="提交时间" sortable min-width="180" align="center" />
    <el-table-column prop="language" label="语言" min-width="80" align="center" />
    <el-table-column prop="labId" label="实验ID" sortable min-width="100" align="center" />
    <el-table-column prop="reportName" label="文件名" min-width="300" align="center"/>
    <el-table-column  label="是否通过" min-width="100" align="center">
      <template #default="scope">
        <div style="margin-top: 5px">
          <el-icon v-if="isPass(scope.row.questionResults)" color="#00BFFF" size="20"><SuccessFilled /></el-icon>
          <el-icon v-else color="#F18F01" size="20"><WarnTriangleFilled /></el-icon>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="score" label="得分" min-width="100" align="center" />
    <el-table-column fixed="right" label="操作"  min-width="100" align="center">
      <template #default="scope">
        <el-button link  type="primary" @click="mapDetail(scope.row.questionResults)">查看详情</el-button>
        <el-dialog
            :title="`《 ${scope.row.reportName} 》的执行结果`"
            v-model="dialogsVisible"
            width="700px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :destroy-on-close="true"
            :show-close="false"
        >
          <el-table
              :data="details"
              style="width: auto"
          >
            <el-table-column label="题号" width="100" align="center" >
              <template #default="scope">
                <span>{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="是否通过" width="100" align="center" >
              <template #default="scope">
                <div style="margin-top: 5px">
                  <el-icon v-if="details[scope.$index].correct" color="#00BFFF" size="20"><SuccessFilled /></el-icon>
                  <el-icon v-else color="#F18F01" size="20"><WarnTriangleFilled /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="详情" width="460" align="center" >
              <template #default="scope">
                <span>{{ details[scope.$index].detail }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" @click="dialogsVisible = false">确认</el-button>
        </el-dialog>
      </template>
    </el-table-column>
  </el-table>
  </el-card>
</template>

<style scoped>

</style>