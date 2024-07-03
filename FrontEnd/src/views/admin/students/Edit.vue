<script setup lang="ts" >
import {onMounted, reactive, ref} from 'vue'
import {ElInput, ElMessage, ElSelect, ElTable} from 'element-plus'
import request from "@/utils/request.js";
import {User} from "@/entities";
import { nextTick} from 'vue'


const users = reactive([]) as User[]
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const multipleSelection = ref<User[]>([])
const filtersState = reactive({});
const dialogsVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(true)
const sectionFilters = reactive([])

const getUsers = async () => {
  loading.value = true
  const params = new URLSearchParams()
  Object.keys(filtersState).forEach((key) => {
    filtersState[key].forEach((item) => {
      params.append(key+'s', item)
    })
  })
  params.append('page', (currentPage.value-1).toString())
  params.append('size', pageSize.value.toString())

  await request.get('/admin/user',{params: params}).then(
      res => {
        users.splice(0,users.length)
        Object.values(res.data.users).forEach((item) => {
          const user = {
            id: item.id,
            username:  item.username,
            password:  item.password,
            section: item.section,
            userRole: item.userRole
          } as User;
          users.push(user)
        })
        total.value = res.data.total
        loading.value = false
      }
  )
}
const getFilters = async () => {
  await request.get('/section/all').then(
      res => {
        sectionFilters.push(...res.data.sections.map((item: string) => ({text: item.section, value: item.section})))
      }
  )
}
onMounted(() => {
  getUsers()
  getFilters()
})

const handleSelectionChange = (val: User[]) => {
  multipleSelection.value = val
}

const filterSection = (value: string, row: User) => {
  return row.section === value
}

const handleFilterChange = (filters) => {
  const keys = Object.keys(filters)
  // 更新全局过滤器状态
  if (keys && keys.length > 0) {
    filtersState[keys[0]] = filters[keys[0]];
  } else {
    delete filtersState[keys[0]]; // 重置某个过滤器
  }
  getUsers()
}
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getUsers()
}
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getUsers()
}
const currentId = ref('')

const usernameInput = ref('')
const usernameInputVisible = ref(false)
const usernameInputRef = ref<InstanceType<typeof ElInput>>()
const showUsernameInput = (id) => {
  currentId.value = id
  usernameInputVisible.value = true
  nextTick(() => {
    usernameInputRef.value!.input!.focus()
  })
}
const handleUsernameInputBlur = () => {
  usernameInputVisible.value = false
  currentId.value = ''
}
const handleUsernameInputConfirm = () => {
  if (usernameInput.value) {
    request.post(
        "/admin/user/username",
        {id: currentId.value, username: usernameInput.value},
        {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    ).then(
        res => {
          if (res.success) {
            ElMessage.success('修改用户名成功')
            getUsers()
          } else {
            ElMessage.error(res.message)
          }
        }
      )
  }
  usernameInputVisible.value = false
  usernameInput.value = ''
}

const passwordInput = ref('')
const passwordInputVisible = ref(false)
const passwordInputRef = ref<InstanceType<typeof ElInput>>()
const showPasswordInput = (id) => {
  currentId.value = id
  passwordInputVisible.value = true
  nextTick(() => {
    passwordInputRef.value!.input!.focus()
  })
}
const handlePasswordInputBlur = () => {
  passwordInputVisible.value = false
  currentId.value = ''
}
const handlePasswordInputConfirm = () => {
  if (passwordInput.value) {
    request.post(
        "/admin/user/password",
        {id: currentId.value, password: passwordInput.value},
        {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    ).then(
        res => {
          if (res.success) {
            ElMessage.success('修改密码成功')
            getUsers()
          } else {
            ElMessage.error(res.message)
          }
        }
    )
  }
  passwordInputVisible.value = false
  passwordInput.value = ''
}


const deleteUser = (id) =>{
  request.delete("/admin/user",{params: {id: id}}).then(
      res => {
        if (res.success) {
          ElMessage.success('删除成功')
          getUsers()
        } else {
          ElMessage.error(res.message)
        }
      }
  )
}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%;" max-height="600">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">用户列表</span>
    </template>
  <el-table
      v-loading="loading"
      ref="multipleTableRef"
      :data="users"
      :default-sort="{ prop: 'id', order: 'descending' }"
      style="width: 100%"
      max-height="600"
      @selection-change="handleSelectionChange"
      @filter-change="handleFilterChange"
  >
    <el-table-column prop="id" label="ID" sortable min-width="180" align="center" fixed="left"/>
    <el-table-column
        prop="section"
        column-key="section"
        label="班级"
        width="100"
        :filters="sectionFilters"
        :filter-method="filterSection"
        filter-placement="bottom-end"
        align="center"
    />
    <el-table-column prop="username" label="用户名" sortable min-width="180" align="center" />
    <el-table-column fixed="right" label="操作" align="center" min-width="230">
      <template #default="scope">
        <el-input
            v-if="usernameInputVisible && scope.row.id === currentId"
            ref="usernameInputRef"
            v-model="usernameInput"
            placeholder="请输入新的用户名"
            class="w-20"
            size="default"
            @keyup.enter="handleUsernameInputConfirm"
            @blur="handleUsernameInputBlur"
        />
        <el-input
            v-if="passwordInputVisible && scope.row.id === currentId"
            ref="passwordInputRef"
            v-model="passwordInput"
            placeholder="请输入新的密码"
            class="w-20"
            size="default"
            show-password
            @keyup.enter="handlePasswordInputConfirm"
            @blur="handlePasswordInputBlur"
        />
        <el-button v-if="!((usernameInputVisible || passwordInputVisible) && scope.row.id === currentId)" link  type="primary" @click="showPasswordInput(scope.row.id)">修改密码</el-button>
        <el-button v-if="!((usernameInputVisible || passwordInputVisible) && scope.row.id === currentId)" link  type="primary" @click="showUsernameInput(scope.row.id)">修改用户名</el-button>
        <el-button v-if="!((usernameInputVisible || passwordInputVisible) && scope.row.id === currentId)" link  type="primary" @click="deleteUser(scope.row.id)">注销</el-button>
      </template>
    </el-table-column>
  </el-table>
  <template #footer>
    <el-row>
      <div style="margin-top: 10px;float: left">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 40]"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-row>
  </template>
  </el-card>

</template>

<style scoped>

</style>
