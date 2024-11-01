<template>
    <y9Card :showHeader="false">
        <div class="toolbar">
            <div class="toolbar-left">
                <el-form :inline="true" @submit.native.prevent>
                    <el-form-item :label="$t('文种')" style="width: 180px">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入文种搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item :label="$t('公文种类')" style="width: 210px">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入公文种类搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item :label="$t('题名')">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入题名搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item :label="$t('档号')" style="width: 180px">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入档号搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                </el-form>
            </div>

            <div class="toolbar-right">
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    @click="loadList"
                    ><i class="ri-search-line"></i>{{ $t('查询') }}</el-button
                >
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="loadList"
                    ><i class="ri-refresh-line"></i>{{ $t('重置') }}</el-button
                >
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="loadList"
                    ><i class="ri-search-line"></i>{{ $t('高级搜索') }}</el-button
                >
            </div>
        </div>
        <el-row :gutter="20" style="padding: 10px">
            <el-col :span="3">
                <el-dropdown :hide-on-click="false">
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-eye-line"></i>{{ $t('显示/隐藏列') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <el-checkbox
                                    v-model="checkAll"
                                    :indeterminate="isIndeterminate"
                                    @change="handleCheckAllChange"
                                >
                                    全选/取消
                                </el-checkbox>
                            </el-dropdown-item>
                            <el-dropdown-item>
                                <el-checkbox
                                    v-model="checked1"
                                    label="Option 1"
                                    size="large"
                                    @click.native="changeOrder($event, 'FILE_NAME')"
                                    >{{ $t('文件名') }}</el-checkbox
                                >
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-col>
            <el-col :span="2">
                <el-dropdown>
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-file-add-line"></i>{{ $t('新增') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <el-checkbox
                                    v-model="checked1"
                                    label="Option 1"
                                    size="large"
                                    @click.native="changeOrder($event, 'FILE_NAME')"
                                    >{{ $t('文件名') }}</el-checkbox
                                >
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-col>
            <el-col :span="6">
                <el-dropdown>
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-eye-line"></i>{{ $t('批量操作') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <el-checkbox
                                    v-model="checked1"
                                    label="Option 1"
                                    size="large"
                                    @click.native="changeOrder($event, 'FILE_NAME')"
                                    >{{ $t('文件名') }}</el-checkbox
                                >
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-col>

            <el-col :span="6"><div class="grid-content ep-bg-purple" /></el-col>
        </el-row>
        <y9Table
            :config="dataTableConfig"
            @on-curr-page-change="onCurrPageChange"
            @on-page-size-change="onPageSizeChange"
        >
            <template v-if="maxVersion != selectVersion" #opt_button="{ row, column, index }">
                <span style="font-weight: 600" @click="dataMove(row)"><i class="ri-add-line"></i>迁移</span>
            </template>
        </y9Table>
    </y9Card>
</template>

<script lang="ts" setup>
    import { $deepAssignObject } from '@/utils/object.ts';
    import { useSettingStore } from '@/store/modules/settingStore';

    const props = defineProps({
        currTreeNodeInfo: {
            //当前tree节点信息
            type: Object,
            default: () => {
                return {};
            }
        }
    });
    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    let total = ref(0);
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 370);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 370;
        })();
    };
    const data = reactive({
        //当前节点信息
        currInfo: props.currTreeNodeInfo,
        dataTableConfig: {
            columns: [
                {
                    title: '序号',
                    type: 'index',
                    width: '60'
                },
                {
                    title: '文件编号',
                    key: 'number',
                    width: '180'
                },
                {
                    title: '标题',
                    key: 'title'
                },
                {
                    title: '拟稿人',
                    key: 'startorName',
                    width: '120'
                },
                {
                    title: '开始时间',
                    key: 'startTime',
                    width: '180'
                },
                {
                    title: '当前办理人',
                    key: 'assigneeNames',
                    width: '120'
                },
                {
                    title: '操作',
                    width: '200',
                    fixed: 'right',
                    slot: 'opt_button'
                }
            ],
            tableData: [],
            pageConfig: {
                // 分页配置，false隐藏分页
                currentPage: 1, //当前页数，支持 v-model 双向绑定
                pageSize: 20, //每页显示条目个数，支持 v-model 双向绑定
                total: total.value //总条目数
            },
            height: tableHeight.value
        }
    });

    let { currInfo, dataTableConfig } = toRefs(data);

    watch(
        () => props.currTreeNodeInfo,
        (newVal, oldVal) => {
            currInfo.value = $deepAssignObject(currInfo.value, newVal);
            getProcessDataList();
        },
        { deep: true }
    );

    onMounted(() => {
        getProcessDataList();
    });

    async function getProcessDataList() {
        //     dataTableConfig.value.tableData = [];
        //     let page = dataTableConfig.value.pageConfig.currentPage;
        //     let rows = dataTableConfig.value.pageConfig.pageSize;
        //     let res = await getProcessInstanceList(
        //         props.currTreeNodeInfo.processDefinitionId,
        //         props.currTreeNodeInfo.id,
        //         page,
        //         rows
        //     );
        //     if (res.success) {
        //         dataTableConfig.value.tableData = res.rows;
        //         dataTableConfig.value.pageConfig.total = res.total;
        //     }
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        dataTableConfig.value.pageConfig.currentPage = currPage;
        getProcessDataList();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        dataTableConfig.value.pageConfig.pageSize = pageSize;
        getProcessDataList();
    }

    function dataMove(row) {
        ElMessageBox.confirm('你确定要迁移这条数据吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
            .then(async () => {
                let processInstanceId = '';
                if (row != undefined) {
                    processInstanceId = row.processInstanceId;
                }
                const loading = ElLoading.service({ lock: true, text: '正在处理中', background: 'rgba(0, 0, 0, 0.3)' });
                // dataTransfer(props.currTreeNodeInfo.processDefinitionId, processInstanceId).then((res) => {
                //     loading.close();
                //     if (res.success) {
                //         ElNotification({
                //             title: '操作提示',
                //             message: res.msg,
                //             type: 'success',
                //             duration: 2000,
                //             offset: 80
                //         });
                //         getStartNodeList();
                //     } else {
                //         ElNotification({
                //             title: '操作提示',
                //             message: res.msg,
                //             type: 'error',
                //             duration: 2000,
                //             offset: 80
                //         });
                //     }
                // });
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消迁移',
                    offset: 65
                });
            });
    }
</script>

<style lang="scss" scoped>
    @import '../../../theme/global.scss';
    .toolbar:after {
        clear: both;
        content: '';
        display: block;
    }

    .toolbar-left {
        float: left;
    }

    .toolbar-right {
        float: right;
        :deep(.el-button) {
            height: 30px;
            line-height: 0;
            min-width: 0px;
            box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
        }
    }
</style>
