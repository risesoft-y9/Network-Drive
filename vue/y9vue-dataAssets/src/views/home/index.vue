<template>
    <div class="home-container">
        <!-- 数据统计卡片 -->
        <div class="stats-card-container">
            <el-card class="stats-card" shadow="hover">
                <div class="stats-card-content">
                    <div class="stats-icon data-source">
                        <i class="ri-database-2-line"></i>
                    </div>
                    <div class="stats-info">
                        <h3 class="stats-number">{{ dataStats.totalAssetCount }}</h3>
                        <p class="stats-label">{{ $t('资产数量') }}</p>
                    </div>
                </div>
            </el-card>
            
            <el-card class="stats-card" shadow="hover">
                <div class="stats-card-content">
                    <div class="stats-icon table-count">
                        <i class="ri-table-line"></i>
                    </div>
                    <div class="stats-info">
                        <h3 class="stats-number">{{ dataStats.tableCount }}</h3>
                        <p class="stats-label">{{ $t('数据表数量') }}</p>
                    </div>
                </div>
            </el-card>
            
            <el-card class="stats-card" shadow="hover">
                <div class="stats-card-content">
                    <div class="stats-icon api-count">
                        <i class="ri-code-box-line"></i>
                    </div>
                    <div class="stats-info">
                        <h3 class="stats-number">{{ dataStats.apiCount }}</h3>
                        <p class="stats-label">{{ $t('API接口数量') }}</p>
                    </div>
                </div>
            </el-card>
            
            <el-card class="stats-card" shadow="hover">
                <div class="stats-card-content">
                    <div class="stats-icon subscription-count">
                        <i class="ri-rss-line"></i>
                    </div>
                    <div class="stats-info">
                        <h3 class="stats-number">{{ dataStats.subscriptionCount }}</h3>
                        <p class="stats-label">{{ $t('数据订阅数量') }}</p>
                    </div>
                </div>
            </el-card>
        </div>
        
        <!-- 图表展示区域 -->
        <div class="chart-container">
            <!-- 数据分布图表 -->
            <el-card class="chart-card" shadow="hover">
                <template #header>
                    <div class="chart-header">
                        <span>{{ $t('数据分布') }}</span>
                    </div>
                </template>
                <div class="chart-row">
                    <div ref="apiTrendChart" class="chart-half"></div>
                    <div ref="apiTypeChart" class="chart-half"></div>
                </div>
            </el-card>
        </div>
        
        <!-- 最近活动 -->
        <el-card class="activity-card" shadow="hover">
            <template #header>
                <div class="chart-header">
                    <span>{{ $t('最近活动') }}</span>
                </div>
            </template>
            <el-table :data="recentActivities" style="width: 100%">
                <el-table-column prop="createDate" label="时间" width="180"></el-table-column>
                <el-table-column prop="operationType" label="类型" width="120"></el-table-column>
                <el-table-column prop="content" label="内容"></el-table-column>
                <el-table-column prop="operator" label="操作人" width="120"></el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue';
import * as echarts from 'echarts';
import { findHomeStatistics, searchRecentPage } from '@/api/home';

// 数据统计
const dataStats = reactive({
    totalAssetCount: 0,
    dataSourceCount: 24,
    tableCount: 156,
    apiCount: 89,
    subscriptionCount: 123
});

// 最近活动
let recentActivities = ref([]);

// 图表引用
const apiTrendChart = ref<HTMLElement | null>(null);
const apiTypeChart = ref<HTMLElement | null>(null);

// 初始化图表
onMounted(() => {
    initHomeStatistics();
    
    // 初始化最近活动
    fetchRecentActivities();
});

/**
 * 初始化首页统计数据
 */
function initHomeStatistics() {
    findHomeStatistics().then(res => {
        if (res.success) {
            dataStats.totalAssetCount = res.data.totalAssetCount;
            dataStats.dataSourceCount = res.data.totalDataSourceCount;
            dataStats.tableCount = res.data.totalTableCount;
            dataStats.apiCount = res.data.totalApiCount;
            dataStats.subscriptionCount = res.data.totalSubscribeCount;
            const xData = res.data.dailyApiCallCount.map(item => item.date);
            const yData = res.data.dailyApiCallCount.map(item => item.count);
            initApiTrendChart(xData, yData);
            initApiTypeChart(res.data.dataTypeCount);
        }
    });
}

function fetchRecentActivities() {
    searchRecentPage({
        page: 1,
        size: 15
    }).then(res => {
        if (res.success) {
            recentActivities.value = res.rows;
        }
    });
}

// 初始化API调用趋势图表
function initApiTrendChart(xData, yData) {
    if (apiTrendChart.value) {
        const chart = echarts.init(apiTrendChart.value);
        const option = {
            tooltip: {
                trigger: 'axis'
            },
            title: {
                text: 'API调用趋势'
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: xData
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'API调用次数',
                    type: 'line',
                    stack: 'Total',
                    data: yData,
                    areaStyle: {}
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

// 初始化API类型分布图表
function initApiTypeChart(data) {
    if (apiTypeChart.value) {
        const chart = echarts.init(apiTypeChart.value);
        const option = {
            tooltip: {
                trigger: 'item'
            },
            title: {
                text: '数据类型分布'
            },
            legend: {
                orient: 'vertical',
                left: 'right'
            },
            series: [
                {
                    name: '数据类型',
                    type: 'pie',
                    radius: '60%',
                    data: data,
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
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
.home-container {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: 100vh;
}

.stats-card-container {
    display: flex;
    gap: 20px;
    margin-bottom: 30px;
    
    @media (max-width: 1200px) {
        flex-wrap: wrap;
    }
    
    .stats-card {
        flex: 1;
        min-width: 200px;
        
        .stats-card-content {
            display: flex;
            align-items: center;
            gap: 20px;
        }
        
        .stats-icon {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: #fff;
            
            &.data-source {
                background-color: #409eff;
            }
            
            &.table-count {
                background-color: #67c23a;
            }
            
            &.api-count {
                background-color: #e6a23c;
            }
            
            &.subscription-count {
                background-color: #f56c6c;
            }
        }
        
        .stats-info {
            flex: 1;
            
            .stats-number {
                font-size: 24px;
                font-weight: bold;
                color: #303133;
                margin: 0 0 5px 0;
            }
            
            .stats-label {
                font-size: 14px;
                color: #606266;
                margin: 0;
            }
        }
    }
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

.activity-card {
    .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
}

.date-range-select {
    width: 120px;
}

@media (max-width: 768px) {
    .home-container {
        padding: 10px;
    }
    
    .stats-card-container {
        flex-direction: column;
        
        .stats-card {
            width: 100%;
        }
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