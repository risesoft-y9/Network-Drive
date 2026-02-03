<template>
    <div class="monitor-container">
        <!-- 实时监控图表 -->
        <div class="chart-container">
            <!-- API调用趋势 -->
            <el-card class="chart-card" shadow="hover">
                <template #header>
                    <div class="chart-header">
                        <span>{{ $t('接口调用监控') }}</span>
                        <el-button type="primary" size="small" @click="getDailyApiCallCount">
                            <i class="ri-refresh-line"></i> {{ $t('刷新') }}
                        </el-button>
                    </div>
                </template>
                <div ref="apiCallChart" class="chart"></div>
            </el-card>
        </div>
        
        <!-- 实时调用记录 -->
        <el-card class="call-record-card" shadow="hover">
            <template #header>
                <div class="chart-header">
                    <span>{{ $t('接口调用记录') }}</span>
                    <el-button type="primary" size="small" @click="getCallRecords">
                        <i class="ri-refresh-line"></i> {{ $t('刷新') }}
                    </el-button>
                </div>
            </template>
            <el-table :data="callRecords" style="width: 100%" :row-class-name="tableRowClassName">
                <el-table-column prop="createTime" label="调用时间" width="180"></el-table-column>
                <el-table-column prop="requestUrl" label="API路径"></el-table-column>
                <el-table-column prop="serverIp" label="服务器IP" width="150"></el-table-column>
                <el-table-column prop="apiType" label="接口类型" width="100"></el-table-column>
                <el-table-column prop="result" label="状态" width="100">
                    <template #default="scope">
                        <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'">
                            {{ scope.row.result === 'success' ? $t('成功') : $t('失败') }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="hostIp" label="调用IP" width="150"></el-table-column>
                <el-table-column prop="appName" label="调用者" width="150"></el-table-column>
            </el-table>
            <div class="pagination-container">
                <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="currentPage"
                    :page-sizes="[10, 20, 50, 100]"
                    :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="callRecordsTotal"
                >
                </el-pagination>
            </div>
        </el-card>

        <el-card class="call-record-card" shadow="hover">
            <template #header>
                <div class="chart-header">
                    <span>{{ $t('文件下载记录') }}</span>
                    <el-button type="primary" size="small" @click="getDownloadRecords">
                        <i class="ri-refresh-line"></i> {{ $t('刷新') }}
                    </el-button>
                </div>
            </template>
            <el-table :data="downloadRecords" style="width: 100%" :row-class-name="tableRowClassName">
                <el-table-column prop="createTime" label="下载时间" width="180"></el-table-column>
                <el-table-column prop="assetsName" label="数据资产"></el-table-column>
                <el-table-column prop="fileName" label="下载文件"></el-table-column>
                <el-table-column prop="downloader" label="下载人" width="150"></el-table-column>
                <el-table-column prop="result" label="下载结果" width="100">
                    <template #default="scope">
                        <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'">
                            {{ scope.row.result === 'success' ? $t('成功') : $t('失败') }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-container">
                <el-pagination
                    @size-change="handleSizeChangeDown"
                    @current-change="handleCurrentChangeDown"
                    :current-page="currentPageDown"
                    :page-sizes="[10, 20, 50, 100]"
                    :page-size="pageSizeDown"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="downloadRecordsTotal"
                >
                </el-pagination>
            </div>
        </el-card>

        <div class="chart-container">
            <!-- 数据流向监控 -->
            <el-card class="chart-card" shadow="hover">
                <template #header>
                    <div class="chart-header">
                        <span>{{ $t('数据流向监控') }}</span>
                        <el-button type="primary" size="small" @click="getDataSourceDataCount">
                            <i class="ri-refresh-line"></i> {{ $t('刷新') }}
                        </el-button>
                    </div>
                </template>
                <div ref="dataFlowChart" class="chart"></div>
            </el-card>
        </div>

        <!-- 库表推送记录 -->
        <el-card class="call-record-card" shadow="hover">
            <template #header>
                <div class="chart-header">
                    <span>{{ $t('库表推送记录') }}</span>
                    <el-button type="primary" size="small" @click="getPushRecords">
                        <i class="ri-refresh-line"></i> {{ $t('刷新') }}
                    </el-button>
                </div>
            </template>
            <el-table :data="pushRecords" style="width: 100%" :row-class-name="tableRowClassName">
                <el-table-column prop="startTime" label="推送时间" width="180"></el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
                <el-table-column prop="assetsName" label="推送方" width="220"></el-table-column>
                <el-table-column prop="subscribeName" label="接收方" width="220"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                        <el-tag type="success" v-if="scope.row.status === 1">成功</el-tag>
                        <el-tag type="danger" v-else-if="scope.row.status === 2">失败</el-tag>
                        <el-tag type="info" v-else>处理中</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="message" label="推送日志" min-width="200" show-overflow-tooltip>
                    <template #default="scope">
                        <div class="message-cell">{{ scope.row.message }}</div>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-container">
                <el-pagination
                    @size-change="handleSizeChangePush"
                    @current-change="handleCurrentChangePush"
                    :current-page="currentPagePush"
                    :page-sizes="[10, 20, 50, 100]"
                    :page-size="pageSizePush"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pushRecordsTotal"
                >
                </el-pagination>
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
import { findApiCallCount, findDataSourceDataCount, findDownloadLog, getDataFlowLog } from '@/api/home';
import { searchLogPage } from '@/api/apiService';

// 分页信息
const currentPage = ref(1);
const pageSize = ref(10);
const callRecordsTotal = ref(100);

// 调用记录
const callRecords = ref([]);
async function getCallRecords() {
    let params = {
        page: currentPage.value,
        size: pageSize.value,
        appName: ''
    };
    let res = await searchLogPage(params);
    if (res.code == 0) {
        // 对返回的接口数据进行赋值与处理
        callRecords.value = res.rows;
        callRecordsTotal.value = res.total;
    }
}

// 下载记录
const downloadRecords = ref([]);
const currentPageDown = ref(1);
const pageSizeDown = ref(10);
const downloadRecordsTotal = ref(100);
async function getDownloadRecords() {
    let params = {
        page: currentPageDown.value,
        size: pageSizeDown.value
    };
    let res = await findDownloadLog(params);
    if (res.code == 0) {
        // 对返回的接口数据进行赋值与处理
        downloadRecords.value = res.rows;
        downloadRecordsTotal.value = res.total;
    }
}

// 库表推送记录
const pushRecords = ref([]);
const currentPagePush = ref(1);
const pageSizePush = ref(10);
const pushRecordsTotal = ref(100);
async function getPushRecords() {
    let params = {
        page: currentPagePush.value,
        size: pageSizePush.value
    };
    let res = await getDataFlowLog(params);
    if (res.code == 0) {
        // 对返回的接口数据进行赋值与处理
        pushRecords.value = res.rows;
        pushRecordsTotal.value = res.total;
    }
}

// 图表引用
const apiCallChart = ref<HTMLElement | null>(null);
const dataFlowChart = ref<HTMLElement | null>(null);

// 表格行样式
const tableRowClassName = ({ row }: any) => {
    return row.status === 'error' ? 'error-row' : '';
};

// 分页处理
const handleSizeChange = (size: number) => {
    pageSize.value = size;
    getCallRecords();
};

const handleCurrentChange = (current: number) => {
    currentPage.value = current;
    getCallRecords();
};

// 分页处理
const handleSizeChangeDown = (size: number) => {
    pageSizeDown.value = size;
    getDownloadRecords();
};

const handleCurrentChangeDown = (current: number) => {
    currentPageDown.value = current;
    getDownloadRecords();
};

// 分页处理
const handleSizeChangePush = (size: number) => {
    pageSizePush.value = size;
    getPushRecords();
};

const handleCurrentChangePush = (current: number) => {
    currentPagePush.value = current;
    getPushRecords();
};

// 初始化图表
onMounted(() => {
    // 初始化API调用图表
    getDailyApiCallCount();
    getCallRecords();
    getDownloadRecords();
    getDataSourceDataCount();
    getPushRecords();
});

function getDailyApiCallCount() {
    findApiCallCount().then(res => {
        if (res.success) {
            const data = res.data || [];
            // 从list里取出对应值
            let dateList = data.map(item => item.date);
            let successList = data.map(item => item.successCount);
            let failList = data.map(item => item.errorCount);
            initApiCallChart(dateList, successList, failList);
        }
    })
}

function initApiCallChart(dateList, successList, failList) {
    if (apiCallChart.value) {
        const chart = echarts.init(apiCallChart.value);
        const option = {
            tooltip: {
                trigger: 'axis'
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                top: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: dateList
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '成功调用',
                    type: 'line',
                    data: successList,
                    symbolSize: 8,
                    symbol: 'circle',
                    itemStyle: {
                        color: '#67c23a'
                    }
                },
                {
                    name: '失败调用',
                    type: 'line',
                    data: failList,
                    symbolSize: 8,
                    symbol: 'circle',
                    itemStyle: {
                        color: '#f56c6c'
                    }
                }
            ]
        };
        chart.setOption(option);
        
        // 响应式调整
        window.addEventListener('resize', () => {
            chart.resize();
        });
    }
}

function getDataSourceDataCount() {
    findDataSourceDataCount().then(res => {
        if (res.success) {
            const data = res.data || {};
            let sourceNames = data.sourceNames || [];
            let dataCounts = data.dataCounts || [];
            let todayDataCounts = data.todayDataCounts || [];
            initDataFlowChart(sourceNames, dataCounts, todayDataCounts);
        }
    })
}

// 初始化数据流图表
function initDataFlowChart(sourceNames, dataCounts, todayDataCounts) {
    if (dataFlowChart.value) {
        const chart = echarts.init(dataFlowChart.value);
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                data: ['今日流入', '数据总量']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: sourceNames
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '数据总量',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                        focus: 'series'
                    },
                    data: dataCounts,
                    itemStyle: {
                        color: '#409eff'
                    }
                },
                {
                    name: '今日流入',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                        focus: 'series'
                    },
                    data: todayDataCounts,
                    itemStyle: {
                        color: '#67c23a'
                    }
                }
            ]
        };
        chart.setOption(option);
        
        // 响应式调整
        window.addEventListener('resize', () => {
            chart.resize();
        });
    }
}
</script>

<style lang="scss" scoped>
.monitor-container {
    background-color: #f5f7fa;
    min-height: 100vh;
}

.chart-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin-bottom: 30px;
}

.chart-card {
    .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .chart {
        height: 300px;
    }
    
    .chart-row {
        display: flex;
        gap: 20px;
        
        .chart-half {
            flex: 1;
            height: 300px;
        }
    }
}

.call-record-card {
    margin-bottom: 30px;
    
    .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
}

.data-flow-card {
    .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .chart {
        height: 400px;
    }
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

:deep(.error-row) {
    background-color: #fef0f0;
}

.message-cell {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
}

:deep(.el-table__body-wrapper .el-table__body .el-table__row:hover .message-cell) {
    white-space: pre-wrap;
    word-wrap: break-word;
    overflow: visible;
}

@media (max-width: 768px) {
    .monitor-container {
        padding: 10px;
    }
    
    .chart-card {
        .chart-row {
            flex-direction: column;
            
            .chart-half {
                height: 250px;
            }
        }
    }
}
</style>