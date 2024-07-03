<script setup lang="ts">
import {onMounted, reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ref} from "vue";
const data = reactive([] as {templateName:string}[])

const getTemplates = async () => {
  loading.value = true
  await request.get('/admin/template/all').then(
      res => {
        res.data.templates.forEach(item => {
          data.push({templateName: item})
        })
        loading.value = false
        console.log(data)
      }
  )
}
onMounted(() => {
  getTemplates()
})

const deleteTemplate = (template: string) => {
  ElMessageBox.confirm('确定移除该模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete("/admin/template", {params: {templateId: template}}).then(
        res => {
          if (res.success) {
            ElMessage({
              message: '移除成功',
              type: 'success'
            })
            data.splice(data.findIndex(item => item.templateName === template), 1)
          }else {
            ElMessageBox.alert(res.message, '提示', {
              confirmButtonText: '确定'
            })
          }
        }
    )

  }).catch(() => {})
}
const loading = ref(true)


</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%;" max-height="700">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">模板列表</span>
    </template>
    <el-table
        v-loading="loading"
        :data="data"
        style="width: 100%"
        :default-sort="{ prop: 'templateName', order: 'descending' }"
        max-height="700"
    >
      <el-table-column prop="templateName" sortable label="模板名称" min-width="180" align="center" />

      <el-table-column fixed="right" label="操作" min-width="220" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click.prevent="deleteTemplate(scope.row.templateName)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>

</style>