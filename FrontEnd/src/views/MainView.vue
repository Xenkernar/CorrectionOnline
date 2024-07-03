<script setup lang="ts">
import {
  Setting,
  Promotion,
  Discount,
  School,
  Avatar,
  SetUp,
  Search,
  Connection,
  DocumentAdd,
  DocumentCopy,
  Checked,
  FolderChecked, Grid, List, DataAnalysis, Tickets
} from '@element-plus/icons-vue'
import {RouterView, useRouter} from 'vue-router'
import { useUserStore} from "@/stores/user.js";
import {ref, nextTick, reactive, onMounted} from "vue";
import {ElDialog, ElMessageBox,ElMessage} from "element-plus";
import request from "@/utils/request";
import validate from "@/utils/validate.js";

const isAdmin = ref(useUserStore().role === 'ROLE_ADMIN')

const router = useRouter()
const onLogout = () => {
  useUserStore().logout()
  router.push('/login')
}
const changePwdVisible = ref(false)
const pwdForm = ref({
  oldPassword: '',
  password: '',
  confirmed: ''
})
const changePassword = async () => {
  const errors = validate(pwdForm.value)
  if (errors) {
    ElMessageBox.alert(errors, '提示', {
      confirmButtonText: '确定'
    })
    return
  }

  const extUser = {
    id: useUserStore().id,
    originPassword: pwdForm.value.oldPassword,
    password: pwdForm.value.password,
    username: '',
    code: ''
  }
  const json = JSON.stringify(extUser)
  const blob = new Blob([json], {
    type: 'application/json',
  });
  const body = new FormData()
  body.append('extUser', blob)
  await request.post("/password", body).then(
      res => {
        if (res.success) {
          ElMessageBox.alert('修改成功，请重新登录', '提示', {
            confirmButtonText: '确定',
            callback: () => {
              changePwdVisible.value = false
              useUserStore().logout()
              router.push('/login')
            }
          })
        } else {
          ElMessageBox.alert(res.message, '提示', {
            confirmButtonText: '确定'
          })
        }
      }
  )
}
const futureUsername = ref('')
const inputUsernameVisible = ref(false)
const InputUsernameRef = ref<InstanceType<typeof ElInput>>()
const showInputUsername = () => {
  inputUsernameVisible.value = true
  nextTick(() => {
    InputUsernameRef.value!.input!.focus()
  })
}

const handleInputUsernameConfirm = () => {
  if (inputUsernameVisible.value) {
    const username = new FormData()
    username.append('username', futureUsername.value)
    request.post("/username",futureUsername.value).then(
        res => {
          if (res.success) {
            ElMessage.success('修改用户名成功')
            useUserStore().setName(futureUsername.value)
            futureUsername.value = ''
            inputUsernameVisible.value = false
          } else {
            ElMessage.error(res.message)
          }
        }
    )
  }


}
const handleInputUsernameBlur = () => {
  inputUsernameVisible.value = false
  futureUsername.value = ''
}

const resetAll = () => {
  ElMessageBox.confirm('确定要重置系统吗?' +
      '这将删除所有的用户(管理员除外)、班级、实验报告、实验结果!', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.post('/admin/resetAll').then(
        res => {
          if (res.success) {
            ElMessage.success('重置成功')
          } else {
            ElMessage.error(res.message)
          }
        }
    )
  }).catch(() => {})
}
</script>

<template>
<!--  style="position:absolute;left:0;right:0;top:0;bottom:0;overflow:hidden;"-->
  <el-container style="background-color: #1E1E1E;position:absolute;left:0;right:0;top:0;bottom:0;overflow:hidden; ">
    <el-header background-color="#545c64">
      <el-icon size="35" color="#0085ff"><promotion/></el-icon>
      <span style="color: #0085ff; margin-left: 20px; margin-top: 50px; font-size: 40px;">XenKer</span>

      <el-dropdown style="float: right">
        <div class="setupStyle" style="margin-top: 8px; font-size: 40px; color: #0085ff;">
          <el-icon><setting/></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu >
            <el-dropdown-item @click="onLogout">退出登录</el-dropdown-item>
            <el-dropdown-item @click="changePwdVisible=true">修改密码</el-dropdown-item>
            <el-dropdown-item v-if="useUserStore().role === 'ROLE_ADMIN'" @click="resetAll">重置系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <span style="color: #0085ff; font-size: 40px;float: right; margin-right: 5px;">
        <el-button v-if="!inputUsernameVisible" link type="primary" @click="showInputUsername" style="font-size: 40px">
          {{useUserStore().name}}
        </el-button>
        <el-input
            v-if="inputUsernameVisible"
            ref="InputUsernameRef"
            v-model="futureUsername"
            class="w-20"
            size="large"
            @keyup.enter="handleInputUsernameConfirm"
            @blur="handleInputUsernameBlur"
            placeholder="请输入新用户名"
        />
      </span>
    </el-header>
    <el-dialog
        title="修改密码"
        v-model="changePwdVisible"
        width="30%"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :destroy-on-close="true"
        :show-close="false"
    >
      <el-form label-width="auto">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmed" show-password></el-input>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="changePassword">确认修改</el-button>
      <el-button @click="changePwdVisible = false">取消</el-button>
    </el-dialog>
    <el-container>
      <el-aside style="overflow: hidden; min-height: 100vh; background-color: #1E1E1E; width: 200px;">
        <el-menu
            active-text-color="#e1ffff"
            background-color="#1E1E1E"
            class="el-menu-vertical-demo"
            :default-active="$route.path"
            :router="true"
            text-color="#0085ff"
            @open=""
            @close=""
        >
          <el-sub-menu index="1" v-if="!isAdmin">
            <template #title>
              <el-icon><discount/></el-icon>
              <span style="font-size: 18px">实验报告</span>
            </template>
            <el-menu-item index="/user/report/submit">
              <el-icon><set-up /></el-icon>
              <span style="font-size: 15px">提交报告</span>
            </el-menu-item>
            <el-menu-item index="/user/report/result">
              <el-icon><set-up /></el-icon>
              <span style="font-size: 15px">批改结果</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="2" v-if="isAdmin">
            <template #title>
              <el-icon><discount /></el-icon>
              <span style="font-size: 18px">实验管理</span>
            </template>
            <el-menu-item index="/admin/reports/publish">
              <el-icon><DocumentAdd /></el-icon>
              <span style="font-size: 15px">发布新实验</span>
            </el-menu-item>
            <el-menu-item index="/admin/reports/published">
              <el-icon><FolderChecked /></el-icon>
              <span style="font-size: 15px">查看已发布</span>
            </el-menu-item>
            <el-menu-item index="/admin/reports/submitted">
              <el-icon><Checked /></el-icon>
              <span style="font-size: 15px">查看提交结果</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="3" v-if="isAdmin">
            <template #title>
              <el-icon><school /></el-icon>
              <span style="font-size: 18px">班级管理</span>
            </template>
            <el-menu-item index="/admin/sections/add">
              <el-icon><DocumentAdd /></el-icon>
              <span style="font-size: 15px">添加班级</span>
            </el-menu-item>
            <el-menu-item index="/admin/sections/edit">
              <el-icon><search /></el-icon>
              <span style="font-size: 15px">所有班级</span>
            </el-menu-item>
          </el-sub-menu>


          <el-sub-menu index="4" v-if="isAdmin">
            <template #title>
              <el-icon><avatar /></el-icon>
              <span style="font-size: 18px">用户管理</span>
            </template>
            <el-menu-item index="/admin/students/add">
              <el-icon><DocumentAdd /></el-icon>
              <span style="font-size: 15px">添加用户</span>
            </el-menu-item>
            <el-menu-item index="/admin/students/edit">
              <el-icon><search /></el-icon>
              <span style="font-size: 15px">所有用户</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="5" v-if="isAdmin">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span style="font-size: 18px">我的模板</span>
            </template>
            <el-menu-item index="/admin/templates/add">
              <el-icon><DocumentAdd /></el-icon>
              <span style="font-size: 15px">添加模板</span>
            </el-menu-item>
            <el-menu-item index="/admin/templates/edit">
              <el-icon><search /></el-icon>
              <span style="font-size: 15px">所有模板</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/admin/statistics" v-if="isAdmin">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span style="font-size: 18px">数据统计</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main style="background-color: #bfc7d1">
<!--      <el-main style="background-color: #0F1C2E">-->
        <RouterView/>
      </el-main>
    </el-container>
  </el-container>

</template>

<style scoped>
.el-menu{
  border-right: none !important;
}


:deep(.el-tooltip__trigger:focus-visible) {
  outline: 0;
}

</style>