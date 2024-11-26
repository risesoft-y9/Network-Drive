<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-18 17:13:58
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-26 12:00:37
 * @FilePath: \vue\y9vue-archives\src\views\dictionary\metadata\manage.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <y9Card :showHeader="false">
        <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
            <template v-for="item in categoryList">
                <el-tab-pane :label="item.name" :name="item.mark"></el-tab-pane>
            </template>
        </el-tabs>
        <div style="margin-bottom: 15px">
            <el-row>
                <el-col :span="20">
                    <template
                        v-if="
                            activeName != 'WS' &&
                            activeName != 'ZP' &&
                            activeName != 'LX' &&
                            activeName != 'LY'
                        "
                    >
                        <el-button
                            type="primary"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            @click="categoryTable"
                            ><i class="ri-database-line"></i>门类表管理
                        </el-button>
                    </template>
                    <el-button
                        type="primary"
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        @click="numberRules"
                        ><i class="ri-list-ordered-2"></i>档号规则
                    </el-button>
                    <el-button
                        type="primary"
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        @click="resetData"
                        ><i class="ri-arrow-go-forward-line"></i>重置
                    </el-button>
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        @click="reloadTable"
                        ><i class="ri-refresh-line"></i>刷新
                    </el-button>
                    <el-button 
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            @click="openOrder"
                            ><i class="ri-sort-asc"></i>排序
                        </el-button>
                </el-col>
            </el-row>
        </div>
        <y9Table
            :config="tableConfig"
            @on-current-change="handleCurrentChange"
            @on-curr-page-change="onCurrPageChange"
            @on-page-size-change="onPageSizeChange"
        >
            <template #opt_button="{ row, column, index }">
                <i class="ri-edit-line" style="margin-right: 15px" title="编辑" @click="editField(row)">{{
                    $t('编辑')
                }}</i>
            </template>
        </y9Table>
        <y9Dialog v-model:config="dialogConfig">
            <editMetadata v-if="dialogConfig.type == 'editMetadata'" ref="editMetadataRef" :metadata="metadata" />
            <newOrModifyTable
                v-if="dialogConfig.type == 'newOrModifyTable'"
                ref="newOrModifyTableRef"
                :categoryData="categoryData"
                :reloadTable="reloadTable"
                :dialogConfig_parent="dialogConfig"
            />
            <orderList v-if="dialogConfig.type == 'orderList'" ref="orderListRef" :metadata="metadata" :categoryMark="activeName"/>
            <NumberRules v-if="dialogConfig.type == 'numberRules'" ref="numberRulesRef" :categoryMark="activeName" :dialogConfig_parent="dialogConfig"/>
        </y9Dialog>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, reactive, inject, onMounted } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import editMetadata from './editMetadata.vue';
    import newOrModifyTable from './table/newOrModifyTable.vue';
    import orderList from './orderList.vue';
    import NumberRules from './numberRules.vue';
    import { getMetadataList, saveMetadataConfig, saveOrder, resetConfig } from '@/api/archives/metadata';
    import { getAllCategory, getCategoryList } from '@/api/archives/category';
    import { render } from 'vue';
    import { or } from '@vueuse/core';

    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    let total = ref(0);
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 375);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 375;
        })();
    };
    const data = reactive({
        numberRulesRef: '',
        editMetadataRef: '',
        orderListRef:'',
        metadata: {},
        activeName: 'WS',
        categoryList: [],
        tableConfig: {
            columns: [
                {
                    title: '序号',
                    type: 'index',
                    width: '60'
                },
                {
                    title: '元数据标识',
                    key: 'columnName'
                },
                {
                    title: '元数据名称',
                    key: 'disPlayName'
                },
                {
                    title: '数据类型',
                    key: 'dataType',
                    width: '100',
                    render: (row) => {
                        let type = '';
                        switch (row.dataType) {
                            case 'String':
                                type = '字符型';
                                break;
                            case 'Integer':
                                type = '数字型';
                                break;
                            case 'Date':
                                type = '日期型';
                                break;
                            case 'dictionary':
                                type = '字典型';
                                break;
                            default:
                                type = row.dataType;
                                break;
                        }
                        return type;
                    }
                },
                {
                    title: '来源',
                    key: 'fieldOrigin',
                    width: '120'
                },
                {
                    title: '列表显示',
                    key: 'isListShow',
                    width: '110',
                    sortable: true,
                    render: (row) => {
                        return h(
                            'span',
                            {
                                style: {
                                    color: row.isListShow == 1 ?'green':'red',
                                    fontWeight: 600
                                }
                            },
                            row.isListShow == 1 ? '是' : '否'
                        );
                    }
                },
                {
                    title: '是否可排序',
                    key: 'isOrder',
                    width: '120',
                    sortable: true,
                    render: (row) => {
                        return h(
                            'span',
                            {
                                style: {
                                    color: row.isOrder == 1 ?'green':'red',
                                    fontWeight: 600
                                }
                            },
                            row.isOrder == 1 ? '是' : '否'
                        );
                    }
                },
                {
                    title: '是否著录',
                    key: 'isRecord',
                    width: '110',
                    sortable: true,
                    render: (row) => {
                        return h(
                            'span',
                            {
                                style: {
                                    color: row.isRecord == 1 ?'green':'red',
                                    fontWeight: 600
                                }
                            },
                            row.isRecord == 1 ? '是' : '否'
                        );
                    }
                },
                {
                    title: '著录必填',
                    key: 'isRecordNull',
                    width: '110',
                    sortable: true,
                    render: (row) => {
                        return h(
                            'span',
                            {
                                style: {
                                    color: row.isRecordNull == 1 ?'green':'red',
                                    fontWeight: 600
                                }
                            },
                            row.isRecordNull == 1 ? '是' : '否'
                        );
                    }
                },
                {
                    title: '修改人',
                    key: 'userName',
                    width: '120'
                },
                {
                    title: '修改时间',
                    key: 'updateTime',
                    width: '170'
                },
                {
                    title: '操作',
                    width: '150',
                    slot: 'opt_button'
                }
            ],
            border: false,
            tableData: [],
            pageConfig: {
                // 分页配置，false隐藏分页
                currentPage: 1, //当前页数，支持 v-model 双向绑定
                pageSize: 15, //每页显示条目个数，支持 v-model 双向绑定
                total: 0 //总条目数
            },
            height: tableHeight.value
        },
        //弹窗配置
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    if (dialogConfig.value.type == 'editMetadata') {
                        const fieldInstance = editMetadataRef.value.fieldFormRef;
                        fieldInstance.validate(async (valid) => {
                            if (valid) {
                                let res = { success: false, msg: '' };
                                let formData = editMetadataRef.value.metadataForm;
                                res = await saveMetadataConfig(formData);
                                if (res == undefined) {
                                    reject();
                                    return;
                                }
                                ElNotification({
                                    title: res.success ? '成功' : '失败',
                                    message: res.msg,
                                    type: res.success ? 'success' : 'error',
                                    duration: 2000,
                                    offset: 80
                                });
                                if (res.success) {
                                    reloadTable();
                                }
                                resolve();
                            } else {
                                ElMessage({
                                    type: 'error',
                                    message: '验证不通过，请检查',
                                    offset: 65
                                });
                                reject();
                            }
                        });
                    }
                });
            },
            visibleChange: (visible) => {
                // console.log('visible',visible)
            }
        },
        currentRow: null,
        categoryData: { categoryMark: '', categoryName: '' }
    });
    let { activeName, categoryList, tableConfig, metadata,numberRulesRef, editMetadataRef,orderListRef, dialogConfig, currentRow, categoryData } =
        toRefs(data);

    onMounted(() => {
        getCategoryList();
        reloadTable();
        categoryData.value.categoryMark = 'document';
        categoryData.value.categoryName = '文书';
    });

    function getCategoryList() {
        getAllCategory().then((res) => {
            categoryList.value = res.data;
        });
    }

    async function reloadTable() {
        const loading = ElLoading.service({ lock: true, text: '正在加载中', background: 'rgba(0, 0, 0, 0.3)' });
        let page = tableConfig.value.pageConfig.currentPage;
        let rows = tableConfig.value.pageConfig.pageSize;
        let res = await getMetadataList(activeName.value, page, rows);
        if (res.success) {
            loading.close();
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        }
    }

    function handleCurrentChange(val) {
        currentRow.value = val;
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        tableConfig.value.pageConfig.currentPage = currPage;
        reloadTable();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        tableConfig.value.pageConfig.pageSize = pageSize;
        reloadTable();
    }

    function handleClick(tab) {
        activeName.value = tab.props.name;
        console.log(tab.props.label);
        console.log(tab.props.name);
        categoryData.value.categoryMark = tab.props.name;
        categoryData.value.categoryName = tab.props.label;
        reloadTable();
    }

    function categoryTable() {
        Object.assign(dialogConfig.value, {
            show: true,
            width: '55%',
            type: 'newOrModifyTable',
            title: '门类表管理',
            showFooter: false,
            margin: '5vh auto'
        });
    }

    function numberRules(){
        Object.assign(dialogConfig.value, {
            show: true,
            width: '35%',
            type: 'numberRules',
            title: '档号生成规则',
            showFooter: false,
        });
    }

    function openOrder(){
        Object.assign(dialogConfig.value, {
            show: true,
            width: '75%',
            type: 'orderList',
            title: '元数据列表字段排序',
            showFooter: false,
            margin: '5vh auto'
        });
    }

    async function resetData() {
        ElMessageBox.confirm('你确定要重置元数据配置数据吗？重置后，将初始化默认配置，请谨慎操作！', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
            .then(async () => {
                let result = { success: false, msg: '' };
                const loading = ElLoading.service({ lock: true, text: '正在处理中', background: 'rgba(0, 0, 0, 0.3)' });
                result = await resetConfig(activeName.value);
                if (result.success) {
                    loading.close();
                    reloadTable();
                }
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消操作',
                    offset: 65
                });
            });
    }

    function editField(row) {
        metadata.value = row;
        console.log('row', row);

        Object.assign(dialogConfig.value, {
            show: true,
            width: '38%',
            type: 'editMetadata',
            title: '编辑元数据',
            cancelText: '取消'
        });
    }
  
</script>
<style lang="scss">
    .newOrModifyTable .el-dialog__body {
        padding-bottom: 10px;
    }

    .newOrModifyTable .el-divider--horizontal {
        margin: 25px 0;
    }
    .y9-dialog-overlay .y9-dialog .y9-dialog-body .y9-dialog-content[data-v-0a89aa65] {
        padding: 10px;
    }
</style>
<style lang="scss" scoped>
    .layui-table {
        width: 100%;
        border-collapse: collapse;
        border-spacing: 0;

        td {
            position: revert;
            padding: 5px 10px;
            min-height: 32px;
            line-height: 32px;
            font-size: 14px;
            border-width: 1px;
            border-style: solid;
            border-color: #e6e6e6;
            display: table-cell;
            vertical-align: inherit;
        }

        .lefttd {
            background: #f5f7fa;
            text-align: center;
            // margin-right: 4px;
            width: 14%;
        }

        .rightd {
            display: flex;
            flex-wrap: wrap;
            word-break: break-all;
            white-space: pre-wrap;
        }
    }
</style>
