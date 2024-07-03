<script setup lang="ts" >
import {onMounted, reactive, ref} from 'vue'
import {ElTable} from 'element-plus'
import {LabResult, QuestionResult} from "@/entities";
import request from "@/utils/request.js";
import {SuccessFilled, WarnTriangleFilled} from "@element-plus/icons-vue";

const results = reactive([]) as LabResult[]
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const multipleSelection = ref<LabResult[]>([])
const details = reactive([] as Array<QuestionResult>);
const filtersState = reactive({});
const dialogsVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loadingResults = ref(true)
const sectionFilters = reactive([])
const labIdFilters = reactive([])
const languageFilters = reactive([])
const getResults = async () => {
  loadingResults.value = true
  const params = new URLSearchParams()
  Object.keys(filtersState).forEach((key) => {
    filtersState[key].forEach((item) => {
      params.append(key+'s', item)
    })
  })
  params.append('page', (currentPage.value-1).toString())
  params.append('size', pageSize.value.toString())

  await request.get('/labResults/multiFields',{params: params}).then(
      res => {
        results.splice(0,results.length)
        const arr = Object.values(res.data.results) as LabResult[]
        arr.forEach((item) => {
          results.push(item)
        })
        total.value = res.data.total
        loadingResults.value = false
      }
  )
}
const getFilters = async () => {
  await request.get('/section/all').then(
      res => {
        sectionFilters.push(...res.data.sections.map((item: string) => ({text: item.section, value: item.section})))
      }
  )
  await request.get('/labReport/languages').then(
      res => {
        languageFilters.push(...res.data.languages.map((item: string) => ({text: item, value: item})))
      }
  )
  //labIdFilters:1-20
  for (let i = 1; i <= 20; i++) {
    labIdFilters.push({text: i, value: i})
  }
}
onMounted(() => {
  getResults()
  getFilters()
})

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

const toggleSelection = (rows?: LabResult[]) => {
  if (rows && rows.length > 0) {
    multipleTableRef.value!.clearSelection()
    rows.forEach((row) => {
      multipleTableRef.value!.toggleRowSelection(row, undefined)
    })
  } else {
    multipleTableRef.value!.clearSelection()
  }
}
const reverseSelection = () => {
  //取当前所选的行的补集并取消当前所选的行
  const selectedRows = multipleSelection.value
  const unselectedRows = results.filter((row) => !selectedRows.includes(row))
  toggleSelection(unselectedRows)
}

const downloadSelection = async () => {
  const fileNames:string[] = multipleSelection.value.map((item) => item.reportName)
  const arrayAsString = fileNames.join(',');
  console.log(arrayAsString)
  await request.get(`/admin/labReport/urls?fileNames=${encodeURIComponent(arrayAsString)}`).then(
      res => {
        const urls = res.data.urls as string[]
        console.log(urls)
        urls.forEach((url, index) => {
          const iframe = document.createElement("iframe");
          iframe.style.display = "none"; // 防止影响页面
          // iframe.style.height = 0; // 防止影响页面
          iframe.src = url;
          document.body.appendChild(iframe); // 这一行必须，iframe挂在到dom树上才会发请求
          // 5分钟之后删除（onload方法对于下载链接不起作用，就先抠脚一下吧）
          setTimeout(()=>{
            iframe.remove();
          }, 5000);
        })
      }
  )
}
const handleSelectionChange = (val: LabResult[]) => {
  multipleSelection.value = val
}

const filterSection = (value: string, row: LabResult) => {
  return row.section === value
}

const filterLabId = (value: number, row: LabResult) => {
  return row.labId === value
}

const filterLanguage = (value: string, row: LabResult) => {
  return row.language === value
}


const handleFilterChange = (filters) => {
  const keys = Object.keys(filters)
  // 更新全局过滤器状态
  if (keys && keys.length > 0) {
    filtersState[keys[0]] = filters[keys[0]];
  } else {
    delete filtersState[keys[0]]; // 重置某个过滤器
  }
  getResults()
}
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getResults()
}
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getResults()
}

</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 100%" max-height="600">
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">提交结果列表</span>
    </template>
  <el-table
      v-loading="loadingResults"
      ref="multipleTableRef"
      :data="results"
      :default-sort="{ prop: 'submittedTime', order: 'descending' }"
      style="width: 100%"
      max-height="600"
      @selection-change="handleSelectionChange"
      @filter-change="handleFilterChange"
  >
    <el-table-column type="selection" min-width="30" />
    <el-table-column prop="submittedTime" label="提交时间" sortable min-width="180" align="center" />
    <el-table-column
        prop="section"
        column-key="section"
        label="班级"
        min-width="100"
        :filters="sectionFilters"
        :filter-method="filterSection"
        filter-placement="bottom-end"
        align="center"
    />
    <el-table-column
        prop="language"
        column-key="language"
        label="语言"
        min-width="100"
        :filters="languageFilters"
        :filter-method="filterLanguage"
        filter-placement="bottom-end"
        align="center"
    />
    <el-table-column
        prop="labId"
        column-key="labId"
        label="实验ID"
        min-width="100"
        :filters="labIdFilters"
        :filter-method="filterLabId"
        filter-placement="bottom-end"
        align="center"
    />
    <el-table-column prop="reportName" label="文件名" min-width="400" align="center"/>
    <el-table-column  label="是否通过" min-width="100" align="center">
      <template #default="scope">
        <div style="margin-top: 5px">
          <el-icon v-if="isPass(scope.row.questionResults)" color="#00BFFF" size="20"><SuccessFilled /></el-icon>
          <el-icon v-else color="#F18F01" size="20"><WarnTriangleFilled /></el-icon>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="score" label="得分" min-width="100" align="center" />
    <el-table-column fixed="right" label="操作" align="center" min-width="100">
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
          <el-button type="primary" @click="dialogsVisible = false" style="margin-top: 10px">确认</el-button>
        </el-dialog>
      </template>
    </el-table-column>
  </el-table>
    <template #footer>
      <el-row>
        <el-col :span="16">
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
        </el-col>
        <el-col :span="8">
          <div style="margin-top: 10px;float: right">
            <el-button @click="reverseSelection()">反选</el-button>
            <el-button @click="toggleSelection()">清除选择</el-button>
            <el-button @click="downloadSelection()">下载所选报告</el-button>
          </div>
        </el-col>
      </el-row>
    </template>
  </el-card>
</template>

<style scoped>

</style>
