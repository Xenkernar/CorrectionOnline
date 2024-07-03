<script setup lang="ts">
import * as echarts from 'echarts'
import {onMounted, reactive, ref} from "vue";
import request from "@/utils/request.js";
import {useRouter} from "vue-router";
interface Point {
  value: number
  name: string
}
const languages = reactive([])
const selectedLanguage = ref(null)
const sections = reactive([])
const selectedSection = ref(null)
const gradients = [
  {start: '#00f2fe', end: '#4facfe'},
  {start: '#d4fc79', end: '#96e6a1'},
  {start: '#fecfef', end: '#ff9a9e'},
]
const averagePie = ref()
const averageLine = ref()
const onTimeRatePie = ref()
const onTimeRateLine = ref()
const passRatePie = ref()
const passRateLine = ref()
onMounted(() => {
  initChart()
  getLanguage()
  getSections()
})
const getLanguage = () => {
  request.get('/labReport/languages').then(
      res => {
        languages.push(...res.data.languages)
      }
  )
}
const getSections = () => {
  request.get('/section/all').then(
      res => {
        sections.push(...res.data.sections.map(item => item.section))
      }
  )
}
const averageData = reactive<Point[]>([])
const getAverage = async () => {
  await request.get('/admin/grade/average', {
    params: {
      language: selectedLanguage.value,
      section: selectedSection.value
    }
  }).then(res => {
    averageData.splice(0, averageData.length)
    Object.keys(res.data.average).forEach(key => {
      averageData.push({
        value: res.data.average[key],
        name: key
      })
    })
  })
}
const onTimeData = reactive<Point[]>([])
const getOnTimeRate = async () => {
  await request.get('/admin/labResult/onTimeRate',{
    params: {
      language: selectedLanguage.value,
      section: selectedSection.value
    }
  }).then(res => {
    onTimeData.splice(0, onTimeData.length)
    Object.keys(res.data.onTimeRate).forEach(key => {
      onTimeData.push({
        value: res.data.onTimeRate[key],
        name: key
      })
    })
  })
}
const passData = reactive<Point[]>([])
const getPassRate =async () => {
  await request.get('/admin/labResult/passRate',{
    params: {
      language: selectedLanguage.value,
      section: selectedSection.value
    }
  }).then(res => {
    passData.splice(0, passData.length)
    Object.keys(res.data.passRate).forEach(key => {
      passData.push({
        value: res.data.passRate[key],
        name: key
      })
    })
  })
}

const genRandomChart = (data,pieChart,lineChart, gradientsIndex) => {
  let option1 = {
    tooltip: {
      trigger: 'item',
      formatter: '实验Id: {b}<br/>值: {c}'  // 配置显示的内容格式
    },
    series: [
      {
        type: 'pie',
        data: data.map(item => {
          return {
            value: item.value,
            name: item.name,
          }
        }),
        roseType: 'area'
      }
    ]
  };

  let option2 = {
    xAxis: {
      type: 'category',
      data: data.map(item => item.name)
    },
    yAxis: {
      type: 'value'
    },
    tooltip: {
      trigger: 'axis'
    },
    series: [
      {
        type: 'line',
        color: gradients[gradientsIndex].start,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: gradients[gradientsIndex].start   // 开始颜色
            }, {
              offset: 1, color: gradients[gradientsIndex].end   // 结束颜色
            }],
            global: false
          }
        },
        data: data.map(item => item.value)
      }
    ]
  };
  pieChart.setOption(option1);
  lineChart.setOption(option2);
}
const initChart = async () => {
  await getAverage()
  await getOnTimeRate()
  await getPassRate()
  const averagePieChart = echarts.init(averagePie.value)
  const averageLineChart = echarts.init(averageLine.value)
  const onTimeRatePieChart = echarts.init(onTimeRatePie.value)
  const onTimeRateLineChart = echarts.init(onTimeRateLine.value)
  const passRatePieChart = echarts.init(passRatePie.value)
  const passRateLineChart = echarts.init(passRateLine.value)
  // 生成包含指定对象的数组
  genRandomChart(averageData, averagePieChart, averageLineChart, 0)
  genRandomChart(onTimeData, onTimeRatePieChart, onTimeRateLineChart, 1)
  genRandomChart(passData, passRatePieChart, passRateLineChart, 2)
}


</script>

<template>
  <el-card shadow="hover" style="text-align: center;width: 65%;height: 92%" >
    <template #header>
      <span style="color: #0085ff; font-size: 20px;">各项数据统计</span>
    </template>
    <el-row>
      <el-col :span="4">
        <el-row>
          <el-select
              v-model="selectedLanguage"
              placeholder="所有语言"
              size="large"
              style="width: 190px"
              @change="initChart"
          >
            <el-option
                v-for="item in languages"
                :key="item"
                :label="item"
                :value="item"

            />
          </el-select>
        </el-row>
        <el-row style="margin-top: 10px">
          <el-select
              v-model="selectedSection"
              placeholder="所有班级"
              size="large"
              style="width: 190px"
              @change="initChart"
          >
            <el-option
                v-for="item in sections"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-row>
      </el-col>


      <el-col :span="20">
        <el-card shadow="hover" style="text-align: left;width: 100%;height: 30%" >
          <template #header>
            <span style="color: #0085ff; font-size: 15px;">平均分</span>
          </template>
          <el-row>
            <el-col :span="8"  >
              <div ref="averagePie" style="width: 300px;height: 200px;"/>
            </el-col>
            <el-col :span="16" >
              <div ref="averageLine" style="width:  700px;height:250px;"/>
            </el-col>
          </el-row>
        </el-card>
        <el-card shadow="hover" style="text-align: left;width: 100%;height: 30%" >
          <template #header>
            <span style="color: #0085ff; font-size: 15px;">正确率</span>
          </template>
          <el-row>
            <el-col :span="8"  >
              <div ref="passRatePie" style="width: 300px;height: 200px;"/>
            </el-col>
            <el-col :span="16" >
              <div ref="passRateLine" style="width:  700px;height:250px;"/>
            </el-col>
          </el-row>
        </el-card>
        <el-card shadow="hover" style="text-align: left;width: 100%;height: 30%" >
          <template #header>
            <span style="color: #0085ff; font-size: 15px;">准时率</span>
          </template>
          <el-row>
            <el-col :span="8"  >
              <div ref="onTimeRatePie" style="width: 300px;height: 200px;"/>
            </el-col>
            <el-col :span="16" >
              <div ref="onTimeRateLine" style="width:  700px;height:250px;"/>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </el-card>

</template>

<style scoped>

</style>