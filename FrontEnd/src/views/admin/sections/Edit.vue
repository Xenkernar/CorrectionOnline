<script setup lang="ts">
import {onMounted, reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ref} from "vue";
interface Section {
  section: string,
  code: string
}
const data = reactive([] as Section[])
const sectionCounts = ref<Map<string,number>>(new Map())
const sectionMembers = ref<Array<{studentId: string }>>(new Array())
const memberDialogVisible = ref(false)
const getSectionCounts = async () => {
  await request.get('/section/counts').then(
      res => {
        sectionCounts.value = res.data.counts as Map<string,number>
      }
  )
}
const getSectionMembers = async (section:string) => {
  await request.get('/section/members',{params:{section:section}}).then(
      res => {
        sectionMembers.value = (res.data.members as Array<string>).map(item => ({studentId: item}))
        sectionMembers.value.sort()
      }
  )
}
const getSections = async () => {
  loading.value = true
  await request.get('/section/all').then(
      res => {
        data.push(...res.data.sections)
        loading.value = false
      }
  )
}
onMounted(() => {
  getSections()
  getSectionCounts()

})

const showMembers = async (section: string) => {
  await getSectionMembers(section)
  memberDialogVisible.value = true
}
const deleteSection = (section: string) => {
  ElMessageBox.confirm('确定移除该班级？移除后属于该班级的用户将被注销', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete("/section", {params: {section: section}}).then(
        res => {
          if (res.success) {
            ElMessage({
              message: '移除成功',
              type: 'success'
            })
            data.splice(data.findIndex(item => item.section === section), 1)
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
const editCode = (section: Section) => {
  ElMessageBox.prompt('请输入新的邀请码', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '邀请码不能为空'
  }).then(({value}) => {
    request.put("/section", {section: section.section, code: value}).then(
        res => {
          if (res.success) {
            section.code = value
            ElMessage({
              message: '更新成功',
              type: 'success'
            })
          }else {
            ElMessageBox.alert(res.message, '提示', {
              confirmButtonText: '确定'
            })
          }
        }
    )
  }).catch(() => {})
}


</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%;" max-height="700">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">班级列表</span>
    </template>
  <el-table
      v-loading="loading"
      :data="data"
      style="width: 100%"
      :default-sort="{ prop: 'section', order: 'descending' }"
      max-height="700"
  >
    <el-table-column prop="section" sortable label="班级" min-width="180" align="center" />
    <el-table-column min-width="180"  align="center">
      <template #default="scope">
        <span>{{ sectionCounts[scope.row.section] }}</span>
      </template>
      <template #header>
        <div>班级总人数</div>
      </template>

    </el-table-column>
    <el-table-column prop="code" label="邀请码" min-width="380" align="center" />
    <el-table-column fixed="right" label="操作" min-width="220" align="center">
      <template #default="scope">
        <el-button link type="primary" size="small" @click.prevent="deleteSection(scope.row.section)">移除</el-button>
        <el-button link type="primary" size="small" @click.prevent="editCode(scope.row)">编辑邀请码</el-button>
        <el-button link type="primary" size="small" @click.prevent="showMembers(scope.row.section)">班级成员</el-button>
        <el-dialog
            :title="`${scope.row.section}的成员列表`"
            v-model="memberDialogVisible"
            width="240px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :destroy-on-close="true"
            :show-close="false"
        >
          <el-table
              :data="sectionMembers"
              style="width: 100%"
              max-height="600"
          >
            <el-table-column fixed prop="studentId" label="学号"  align="center" />
          </el-table>
          <el-button type="primary" @click="memberDialogVisible = false" style="margin-top: 10px">确定</el-button>
        </el-dialog>
      </template>
    </el-table-column>
  </el-table>
  </el-card>
</template>

<style scoped>

</style>