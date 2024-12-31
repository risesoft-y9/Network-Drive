<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-27 14:12:30
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-28 15:04:59
 * @FilePath: \vue\y9vue-dataAssets\src\views\collect\beforehand\archivesTesting.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <el-container class="testing-container">
      <el-aside width="260px" style="text-align: center;">
        <el-progress type="dashboard" :percentage="percentage" width="240" style="margin-top: 20px;">
            <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">{{ percentageTitle }}</span>
            </template>
        </el-progress>
        <el-tag type="primary" class="tag-text"><p>检测完成</p>
            <span>本次检测耗时0.12秒，共检测1条档案，成功1条，失败0条，通过率100%</span>
        </el-tag>
        <div class="report"><el-link type="primary"><i class="ri-eye-line"></i>查看检测报告</el-link></div>
      </el-aside>
      <el-main>
        <y9Table :config="tableConfig">
            <template #opt="{row,column,index}">
                <i class="ri-loader-2-line">检测中...</i>
                <i class="ri-close-line error">检测失败</i>
                <i class="ri-check-line success">检测通过</i>
            </template>
        </y9Table>
      </el-main>
    </el-container>
    
</template>
<script lang="ts" setup>
import {ref, reactive, toRefs, watch, onMounted, nextTick, inject} from 'vue';
import { getSelectArchivesList } from '@/api/dataAssets/dataAssets';

const props = defineProps({
    archivesIdArr: Array
    });
const data = reactive({
    percentage: 0,
    percentageTitle: '正在检测中...',
    tableConfig: {
        height: '400px',
        //showHeader:false,
        columns: [
            {
                title: '题名',
                key: 'title',
                align: 'left',
            },
            {
                title: '状态',
                key: 'opt',
                align: 'center',
                width: 150,
                slot: 'opt',
            },
        ],
        border: false,
        tableData: [],
        pageConfig: false
    },
    //弹窗配置
    dialogConfig: {
        show: false,
        title: '',
        onOkLoading: true,
        onOk: (newConfig) => {
        return new Promise(async (resolve, reject) => {
        })
        }
    }
});

let {percentage,percentageTitle,tableConfig,dialogConfig} = toRefs(data);

onMounted(() => {
    checkProgress();
    loadTable();
});

async function loadTable(){
    let res = await getSelectArchivesList(props.archivesIdArr.join(','));
    tableConfig.value.tableData = res.data;
}
// 模拟进度条更新的函数
function checkProgress() {
    percentage.value = 0;
    percentageTitle.value = '正在检测中...';
    // 模拟请求开始
  const interval = setInterval(() => {
    let progress = 0;
    const loadingInterval = setInterval(() => {
      progress += 10; // 每次增加10%
      percentage.value = progress;

      if (progress >= 100) {
        clearInterval(loadingInterval);
        clearInterval(interval);
      }
    }, 100); // 每100毫秒更新一次进度
    
  }, 200); // 每200毫秒开始一次模拟请求

  percentageTitle.value = "检测完成";
}

function openReport() {
    // 打开报告
    Object.assign(dialogConfig.value, {
        show: true,
        width: '50%',
        type: 'reportDetail',
        title: '检测报告',
        showFooter: false,
        margin: '2vh auto'
    });
}

</script>
<style lang="scss" scoped>
.testing-container{
    .percentage-value {
        display: block;
        margin-top: 10px;
        font-size: 28px;
    }
    .percentage-label {
        display: block;
        margin-top: 10px;
        font-size: 14px;
    }
    .tag-text{
        width: 260px;
        height: 120px;
        flex-wrap: wrap;
        word-break: break-all;
        white-space: pre-wrap;
    }
    p{
        font-size: 18px;
        font-weight: 600;
        text-align: left;
    }
    i{
        font-size: 18px;
    }
    .error{
        color: #F56C6C !important;
    }
    .success{
        color: #67C23A !important;
    }
    span{
        font-size: 14px;
    }
    .report{
        text-align: center;
        margin-top: 20px;
    }
}
</style>