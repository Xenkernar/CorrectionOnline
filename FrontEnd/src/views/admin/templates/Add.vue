<script setup lang="ts" >

import {reactive, ref, unref} from "vue";
import request from "@/utils/request.js";
import {baseURL} from "@/utils/request";
import {ElLoading, ElMessage, ElMessageBox, type UploadUserFile} from "element-plus";
import {UploadFilled} from "@element-plus/icons-vue";

const file = ref<UploadUserFile[]>()
const templateId = ref('')

const beforeUpload =(selectedFile) =>{
  const postfix=selectedFile.name.substring(selectedFile.name.lastIndexOf('.')+1)
  const pass = postfix === 'docx'
  if(!pass) {
    ElMessageBox.alert('请上传docx格式的实验报告', '提示', {
      confirmButtonText: '确定'
    })
    file.value.pop()
  }
}

const submit = async () => {
  const body = new FormData()
  body.append('file', file.value[0].raw)
  body.append('templateId', templateId.value)
  const loading = ElLoading.service({
    lock: true,
    text: '正在解析模板...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  await request.post('/admin/template', body).then(
      async res => {
        console.log(res)
        loading.close()
        if (res.success) {
          ElMessage.success('添加成功')
        } else {
          await ElMessageBox.alert('添加失败:' + res.message, '提示', {
            confirmButtonText: '确定'
          })
        }
      }
  )
}
</script>

<template>

  <el-card shadow="hover" style="text-align: center;width: 400px" >
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">添加一个模板</span>
    </template>
    <el-form  label-width="auto" style="max-width: 400px;">
      <el-form-item  class="custom-label-color">
        <el-upload
            v-model:file-list="file"
            drag
            :action="`${baseURL}`+'/admin/template'"
            :before-upload="beforeUpload"
            :limit="1"
            style="width: 600px;"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽模板文件到这里或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              请上传docx格式的模板文件
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-input v-model="templateId" placeholder="请输入模板名称"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit" style="width: 100%;">添加</el-button>
    </template>
  </el-card>
</template>

<style scoped>
.el-icon--upload{
  font-size: 60px;
  color: #3399ff;
}
</style>