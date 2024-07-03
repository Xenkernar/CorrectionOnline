<script setup lang="ts">

import {reactive} from "vue";
import request from "@/utils/request.js";
import {useRouter} from "vue-router";
import {useUserStore} from "@/stores/user.js";
import {ElMessageBox} from "element-plus";

const userStore = useUserStore()
const router = useRouter()

const form = reactive({
  id: '',
  password: ''
})

const onSubmit = () => {
  const user = new FormData()
  user.append('id', form.id)
  user.append('password', form.password)
  request.post('/login',user).then(
      res => {
          if (res.success) {
            userStore.login({
              id: form.id,
              name: res.data.name,
              token: res.data.token,
              role: res.data.role,
              section: res.data.section
            })
            res.data.role === 'ROLE_USER' ? router.push('/user') : router.push('/admin')
          }else{
            ElMessageBox.alert(res.message, '提示', {
              confirmButtonText: '确定'
            })
          }
      }
  )
}

</script>


<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="login-header">
        <span>登录</span>
      </div>
      <el-form label-position="top" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.id" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input
              type="password"
              v-model="form.password"
              placeholder="请输入密码"
              autocomplete="off"
              show-password
          ></el-input>
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="onSubmit">登录</el-button>
        </div>
        <div class="register-link">
          <el-button link type="primary" @click="router.push('/register')">还没有账号？去注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: url('@/assets/loginBg.jpg') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
  border-radius: 10px;
  opacity: 0.8
}

.login-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #0085ef;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.register-link {
  display: flex;
  justify-content: flex-end;
}

.el-input{
  --el-input-text-color:#0085ef;
  --el-input-border-color:#cee8ff;
  --el-input-placeholder-color: #acc2ef;

}
</style>