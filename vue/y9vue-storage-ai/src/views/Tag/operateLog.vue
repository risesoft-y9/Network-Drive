<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
            <div class="toolbar">
                <div class="toolbar-left">
                    <el-form class="formClass" :model="searchForm" :inline="true">
                        <el-form-item :label="$t('用户名称')">
                            <el-input
                                v-model="searchForm.userName"
                                :placeholder="$t('请输入用户名称')"
                                clearable
                                style="width: 150px"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('操作名称')">
                            <el-input
                                v-model="searchForm.operateName"
                                :placeholder="$t('请输入操作名称')"
                                clearable
                                style="width: 150px"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('操作类型')">
                            <el-select
                                v-model="searchForm.operateType"
                                :placeholder="$t('请选择')"
                                clearable
                                style="width: 120px"
                            >
                                <el-option label="全部" value="" />
                                <el-option label="增加" value="增加" />
                                <el-option label="修改" value="修改" />
                                <el-option label="删除" value="删除" />
                                <el-option label="更新" value="更新" />
                                <el-option label="查询" value="查询" />
                            </el-select>
                        </el-form-item>
                        <el-form-item :label="$t('时间范围')">
                            <el-date-picker
                                v-model="dateRange"
                                :shortcuts="shortcuts"
                                type="daterange"
                                :start-placeholder="$t('开始时间')"
                                :end-placeholder="$t('结束时间')"
                                value-format="YYYY-MM-DD"
                                style="width: 240px"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('列表类型')">
                            <el-select
                                v-model="searchForm.paramsJson"
                                :placeholder="$t('请选择')"
                                clearable
                                style="width: 120px"
                            >
                                <el-option label="全部" value="" />
                                <el-option label="我的文件" value="my" />
                                <el-option label="部门文件" value="dept" />
                                <el-option label="公共文件" value="public" />
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <el-button-group>
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-second"
                            @click="loadList"
                            plain
                        >
                            <i class="ri-search-line"></i>{{ $t('查询') }}
                        </el-button>
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-second"
                            @click="resetSearch"
                            plain
                        >
                            <i class="ri-reset-left-line"></i>{{ $t('重置') }}
                        </el-button>
                    </el-button-group>
                </div>
            </div>
        </template>
        
        
        
        <el-form class="formClass" ref="fileForm" :model="formData" :rules="rules">
            <y9Table
                ref="multipleTable"
                :config="y9TableConfig"
                v-loading="loading"
                :element-loading-text="loadingTitle"
                element-loading-spinner="el-icon-loading"
                element-loading-background="rgba(0, 0, 0, 0.8)"
                @on-change="handleSelectionChange"
                @row-click="selectRow"
                @on-curr-page-change="onCurrPageChange"
                @on-page-size-change="onPageSizeChange"
            >
                <template #userName="{ row }">
                    <span>{{ row.userName || '-' }}</span>
                </template>
                <template #userHostIp="{ row }">
                    <span>{{ row.userHostIp || '-' }}</span>
                </template>
                <template #success="{ row }">
                    <el-tag :type="row.success === '成功' ? 'success' : 'danger'">
                        {{ row.success }}
                    </el-tag>
                </template>
                <template #systemName="{ row }">
                    <span>{{ row.systemName || '-' }}</span>
                </template>
                <template #modularName="{ row }">
                    <span>{{ row.modularName || '-' }}</span>
                </template>
                <template #operateName="{ row }">
                    <span>{{ row.operateName || '-' }}</span>
                </template>
                <template #operateType="{ row }">
                    <el-tag size="small">{{ row.operateType || '-' }}</el-tag>
                </template>
                <template #logLevel="{ row }">
                    <el-tag 
                        :type="getLogLevelType(row.logLevel)" 
                        size="small"
                    >
                        {{ row.logLevel || '-' }}
                    </el-tag>
                </template>
                <template #createTime="{ row }">
                    <span>{{ row.createTime || '-' }}</span>
                </template>
            </y9Table>
        </el-form>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, onMounted, watch, computed, reactive, toRefs, inject } from 'vue';
    import type { FormRules } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { $filteredNullObj } from '@/utils/object';
    // 引入日志 API（需要根据实际项目调整）
    import LogApi from '@/api/storage/operateLog';

    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    
    const rules = reactive<FormRules>({
        name: { required: false }
    });

    const shortcuts = [
        {
            text: t('最近一周'),
            value: () => {
                const end = new Date();
                const start = new Date();
                start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                return [start, end];
            }
        },
        {
            text: t('最近一个月'),
            value: () => {
                const end = new Date();
                const start = new Date();
                start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                return [start, end];
            }
        },
        {
            text: t('最近三个月'),
            value: () => {
                const end = new Date();
                const start = new Date();
                start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                return [start, end];
            }
        }
    ];
    
    const data = reactive({
        multipleTable: '',
        loadingTitle: '正在加载中......',
        fileForm: '',
        formData: { id: '' },
        multipleSelection: [],
        loading: false,
        y9TableConfig: {
            border: 0,
            height: window.innerHeight - 380,
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: '60', align: 'center' },
                { title: computed(() => t('用户名称')), key: 'userName', align: 'center', width: '90', slot: 'userName' },
                { title: computed(() => t('客户端 IP')), key: 'userHostIp', align: 'center', width: '140', slot: 'userHostIp' },
                { title: computed(() => t('操作状态')), key: 'success', align: 'center', width: '80', slot: 'success' },
                { title: computed(() => t('系统名称')), key: 'systemName', align: 'center', width: '150', slot: 'systemName' },
                { title: computed(() => t('模块名称')), key: 'modularName', align: 'center', width: '280', slot: 'modularName' },
                { title: computed(() => t('操作名称')), key: 'operateName', align: 'center', width: 'auto', slot: 'operateName' },
                { title: computed(() => t('操作类型')), key: 'operateType', align: 'center', width: '90', slot: 'operateType' },
                { title: computed(() => t('日志级别')), key: 'logLevel', align: 'center', width: '80', slot: 'logLevel' },
                { title: computed(() => t('操作时间')), key: 'logTime', align: 'center', width: '180', sortable: true }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: 20,
                total: 0
            },
        },
        searchForm: {
            userName: '',
            userHostIp: '',
            success: '',
            systemName: 'storage',
            modularName: '',
            operateName: '',
            operateType: '',
            logLevel: '',
            startTime: '',
            endTime: ''
        },
        dateRange: [],
    });

    let {
        multipleTable,
        y9TableConfig,
        loadingTitle,
        fileForm,
        formData,
        multipleSelection,
        loading,
        searchForm,
        dateRange,
    } = toRefs(data);

    onMounted(() => {
        loadList();
    });

    // 监听时间范围变化
    watch(
        () => dateRange.value,
        (val) => {
            if (val && val.length === 2) {
                searchForm.value.startTime = val[0];
                searchForm.value.endTime = val[1];
            } else {
                searchForm.value.startTime = '';
                searchForm.value.endTime = '';
            }
        }
    );

    function selectRow(row, column, event) {
        if (row.id != '') {
            multipleTable.value.elTableRef.toggleRowSelection(row);
        } else {
            multipleTable.value.elTableRef.clearSelection();
        }
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
    }

    function loadList() {
        loading.value = true;
        let page = y9TableConfig.value.pageConfig.currentPage;
        let rows = y9TableConfig.value.pageConfig.pageSize;
        
        LogApi.getOperateLogList($filteredNullObj(searchForm.value), page, rows).then((res) => {
            loading.value = false;
            y9TableConfig.value.tableData = res.rows;
            y9TableConfig.value.pageConfig.total = res.total;
        }).catch(() => {
            loading.value = false;
        });
    }

    function resetSearch() {
        searchForm.value = {
            userName: '',
            userHostIp: '',
            success: '',
            systemName: 'storage',
            modularName: '',
            operateName: '',
            operateType: '',
            logLevel: '',
            startTime: '',
            endTime: ''
        };
        dateRange.value = [];
        y9TableConfig.value.pageConfig.currentPage = 1;
        loadList();
    }

    // 当前页改变时触发
    function onCurrPageChange(currPage) {
        y9TableConfig.value.pageConfig.currentPage = currPage;
        loadList();
    }

    // 每页条数改变时触发
    function onPageSizeChange(pageSize) {
        y9TableConfig.value.pageConfig.pageSize = pageSize;
        loadList();
    }

    // 获取日志级别标签类型
    function getLogLevelType(level: string) {
        const typeMap: Record<string, any> = {
            'INFO': 'info',
            'WARN': 'warning',
            'ERROR': 'danger',
            'DEBUG': ''
        };
        return typeMap[level] || '';
    }
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';

    .formClass .el-form-item--default {
        margin-bottom: 0px;
    }

    .formClass .el-form-item {
        margin-bottom: 0px;
    }

    .formClass {
        :deep(.el-form-item__error) {
            color: var(--el-color-danger);
            font-size: 12px;
            line-height: 1;
            padding-top: 2px;
            position: relative;
            top: 0%;
            left: 2%;
        }

        :deep(.el-table) {
            .el-table__body {
                .el-table__row:hover {
                    td {
                        border-left: 0px;
                        border-right: 0px;
                        background-color: var(--el-color-primary-light-9);
                    }
                }
            }
        }
    }

    :deep(.y9-dialog-content) {
        padding: 15px !important;
    }

    :deep(.y9-dialog-header-title) {
        font-size: v-bind('fontSizeObj.mediumFontSize') !important;
    }

    :deep(.el-form-item) {
        display: inline-flex;
        vertical-align: middle;
        margin-right: 10px !important;
        margin-bottom: 0px !important;
    }

    :deep(.el-table__cell .cell) {
        padding-left: 0px;
    }

    :deep(.el-table-column--selection .cell) {
        padding-left: 12px;
        padding-right: 12px;
    }

    :deep(.y9-card-content) {
        padding: 0px 15px 15px 15px !important;
    }

    .toolbar:after {
        clear: both;
        content: '';
        display: block;
    }

    .toolbar-left {
        float: left;
        display: flex;
        align-items: center;
    }

    .toolbar-right {
        float: right;

        :deep(.el-button) {
            height: 30px;
            line-height: 0;
            min-width: 0px;
            padding: 8px 15px;
        }

        :deep(.el-form-item__label) {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }

    :deep(.el-button) {
        min-width: 32px;
        font-size: 12px;
        height: $btnHeight;
        line-height: $btnHeight;
        box-shadow: $boxShadow;
        padding: 0.3vw;

        i {
            margin-right: 0px !important;
        }
    }

    .toolbar {
        padding: 15px 0px;
        background: linear-gradient(to bottom, #f5f7fa, rgb(246 251 255));
        box-shadow: 0 0.1px 0.2px rgba(0, 0, 0, 0.1);

        .toolbar-left {
            float: left;
            display: flex;
            align-items: center;
            gap: 5px;
            padding-left: 15px;

            .el-button {
                transition: all 0.3s ease;
                border-radius: 6px;
                border: none !important;
                border: 1px solid transparent;
                padding: 10px 10px;

                &:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
                    border-color: #0d5bb8;
                }

                &:not(:last-child) {
                    border-right: 1px solid #d0d7e7 !important;
                }

                &.global-btn-second {
                    background: #fff !important;
                    border: 1px solid #dcdfe6;
                    color: #606266 !important;

                    &:hover {
                        background: #f5f9ff;
                    }
                }
            }

            .el-button-group {
                border-radius: 6px;
                overflow: hidden;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

                .el-button {
                    border-radius: 0;
                    margin-right: 0;
                    border-left: 1px solid #dcdfe6;

                    &:first-child {
                        border-left: none;
                    }
                }
            }
        }
    }
</style>