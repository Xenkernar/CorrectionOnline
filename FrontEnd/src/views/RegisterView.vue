<script setup>
import { reactive } from 'vue'
import request from "@/utils/request";
import { useRouter } from "vue-router";
import {ElMessageBox} from "element-plus";
import validate from "@/utils/validate.js";

const router = useRouter()
// do not use same name with ref
const form = reactive({
  id: '',
  username: '',
  password: '',
  confirmed: '',
  invitationCode: ''
})

const onSubmit = () => {
  const errors = validate(form)
  if (errors) {
    ElMessageBox.alert(errors, '提示', {
      confirmButtonText: '确定'
    })
    return
  }
  const user = {
    id: form.id,
    username: form.username,
    password: form.password,
    code: form.invitationCode
  }
  request.post('/user', user).then(
      res => {
        if (res.success) {
          ElMessageBox.alert('注册成功', '提示', {
            confirmButtonText: '确定',
            callback: action => {
              router.push('/login')
            }
          })
        } else {
          ElMessageBox.alert(res.message, '提示', {
            confirmButtonText: '确定'
          })
        }
      }
  ).catch(
      err => console.log(err)
  )
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="login-header">
        <span>注册</span>
      </div>
      <el-form label-position="top" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.id" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
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
        <el-form-item>
          <el-input
              type="password"
              v-model="form.confirmed"
              placeholder="请确认密码"
              autocomplete="off"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.invitationCode" placeholder="请输入班级邀请码"></el-input>
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="onSubmit">注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: url('@/assets/registryBg.jpg') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 600px;
  border-radius: 10px;
  opacity: 0.95
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
}
.el-input{
  --el-input-text-color:#0085ef;
  --el-input-border-color:#cee8ff;
  --el-input-placeholder-color: #acc2ef;
  --el-input-focus-border-color: #0085ff;

}
</style>