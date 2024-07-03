<script setup lang="ts" >

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const form = reactive({
  section: '',
  code: ''
})

const genCode = () => {
  form.code = Math.random().toString(36).substring(2, 20)
}

const submit = () => {
  const formData = new FormData()
  formData.append('section', form.section)
  formData.append('code', form.code)
  request.post('/section', form).then(res => {
    if (res.success) {
      ElMessage({
        message: '添加成功',
        type: 'success'
      })
      form.code = ''
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
      <span style="color: #0085ff; font-size: 20px;">添加一个班级</span>
    </template>
    <el-form  label-width="auto" style="max-width: 400px;">
      <el-form-item >
        <el-input v-model="form.section" placeholder="请输出班级名称"></el-input>
      </el-form-item>
      <el-form-item >
        <el-col :span="17">
          <el-input v-model="form.code" placeholder="请输入班级邀请码"></el-input>
        </el-col>
        <el-container :span="1"></el-container>
        <el-col :span="6">
          <el-button type="primary" @click="genCode" style="width: 100%;">随机生成</el-button>
        </el-col>
      </el-form-item>
    </el-form>
    <template #footer>
    <el-button type="primary" @click="submit" style="width: 100%;">添加</el-button>
    </template>
  </el-card>
</template>

<style scoped>

</style>