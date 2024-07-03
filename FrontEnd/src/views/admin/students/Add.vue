<script setup lang="ts">

import {onMounted, reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox, ElSelect} from "element-plus";
import {Section} from "@/entities.js";
import validate from "@/utils/validate.js";

const sections = reactive([] as string[])
onMounted(async () =>{
  await request.get('/section/all').then(
      res => {
        console.log(res)
        res.data.sections.forEach((value: Section) => {
          sections.push(value.section)
        })
      }
  )
})
const form = reactive({
  id: '',
  username: '',
  password: '',
  confirmed: '',
  section: ''
})

const submit = () => {
  const errors = validate(form);
  if (errors) {
    ElMessageBox.alert(errors, '提示', {
      confirmButtonText: '确定'
    })
    return
  }
  const formData = new FormData()
  formData.append('id', form.id)
  formData.append('username', form.username)
  formData.append('password', form.password)
  formData.append('section', form.section)
  request.post('/admin/user', form).then(res => {
    if (res.success) {
      ElMessage({
        message: '添加成功',
        type: 'success'
      })
      form.id = ''
      form.username = ''
      form.confirmed = ''
      form.password = ''
      form.section = ''
    }
    else{
      ElMessageBox.alert(res.message, '提示', {
        confirmButtonText: '确定'
      })
    }
  })
}
</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 400px" >
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">添加一个用户</span>
    </template>
    <el-form  label-width="auto" style="max-width: 400px">
      <el-form-item >
        <el-input v-model="form.id" placeholder="请输入学号"></el-input>
      </el-form-item>
      <el-form-item >
        <el-input v-model="form.password" placeholder="请输入密码" show-password></el-input>
      </el-form-item>
      <el-form-item >
        <el-input v-model="form.confirmed" placeholder="请确认密码" show-password></el-input>
      </el-form-item>
      <el-form-item >
        <el-input v-model="form.username" placeholder="请输入用户名" show-password></el-input>
      </el-form-item>
      <el-form-item >
        <el-select
            fit-input-width
            v-model="form.section" placeholder="选择班级">
          <el-option
              v-for="item in sections"
              :key="item"
              :label="item"
              :value="item"
          />
        </el-select>
      </el-form-item>

    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit" style="width: 100%;">添加</el-button>
    </template>
  </el-card>
</template>

<style scoped>

</style>