<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-18 17:13:58
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-12-04 14:00:04
 * @FilePath: \vue\y9vue-dataAssets\src\views\dictionary\metadata\manage.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
        <div style="margin-bottom: 15px">
            <el-row>
                <el-col :span="24">
                    <el-button-group>
                        <el-button
                            type="primary"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            @click="moveUp"
                            ><i class="ri-arrow-up-line"></i>上移
                        </el-button>
                        <el-button
                            type="primary"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            @click="moveDown"
                            ><i class="ri-arrow-down-line"></i>下移
                        </el-button>
                        <el-button
                            type="primary"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            @click="saveViewOrder"
                            ><span>保存</span></el-button
                        >
                    </el-button-group>
                    </el-col>
            </el-row>
        </div>
        <y9Table
            :config="tableConfig"
            @on-current-change="handleCurrentChange"
        >
        </y9Table>
</template>

<script lang="ts" setup>
    import { ref, reactive, inject, onMounted } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getMetadataOrderList, saveOrder } from '@/api/dataAssets/metadata';
    import { render } from 'vue';

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

    const props = defineProps({
        metadata: {
            type: Object,
            default: () => {
                return {};
            }
        },
        categoryMark: String
    });
    const data = reactive({
        metadata: {},
        activeName: 'document',
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
                        return row.isListShow == 1 ? '是' : '否';
                    }
                },
                {
                    title: '是否可排序',
                    key: 'isOrder',
                    width: '120',
                    sortable: true,
                    render: (row) => {
                        return row.isOrder == 1 ? '是' : '否';
                    }
                },
                {
                    title: '是否著录',
                    key: 'isRecord',
                    width: '110',
                    sortable: true,
                    render: (row) => {
                        return row.isRecord == 1 ? '是' : '否';
                    }
                },
                {
                    title: '著录必填',
                    key: 'isRecordRequired',
                    width: '110',
                    sortable: true,
                    render: (row) => {
                        return row.isRecordRequired == 1 ? '是' : '否';
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
            ],
            border: false,
            tableData: [],
            pageConfig: false,
            height: tableHeight.value
        },
        currentRow: null,
        categoryData: { categoryMark: '', categoryName: '' }
    });
    let {  tableConfig,  currentRow } =
        toRefs(data);

    onMounted(() => {
        reloadTable();
        
    });

    async function reloadTable() {
        const loading = ElLoading.service({ lock: true, text: '正在加载中', background: 'rgba(0, 0, 0, 0.3)' });
        let res = await getMetadataOrderList(props.categoryMark);
        if (res.success) {
            loading.close();
            tableConfig.value.tableData = res.data;
        }
    }

    function handleCurrentChange(val) {
        currentRow.value = val;
    }

    const moveUp = () => {
        //上移
        if (currentRow.value.length == 0) {
            ElNotification({
                title: '操作提示',
                message: '请点击选中一条数据',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return;
        }

        let index = 0;
        for (let i = 0; i < tableConfig.value.tableData.length; i++) {
            if (currentRow.value.id == tableConfig.value.tableData[i].id) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            let upRow = tableConfig.value.tableData[index - 1];
            let currRow = tableConfig.value.tableData[index];
            let tabIndex = upRow.tabIndex;
            upRow.tabIndex = currRow.tabIndex;
            currRow.tabIndex = tabIndex;
            tableConfig.value.tableData.splice(index - 1, 1);
            tableConfig.value.tableData.splice(index, 0, upRow);
        } else {
            ElNotification({
                title: '操作提示',
                message: '已经是第一条，不可上移',
                type: 'error',
                duration: 2000,
                offset: 80
            });
        }
    };

    const moveDown = () => {
        //下移
        if (currentRow.value.length == 0) {
            ElNotification({ title: '操作提示', message: '请选择数据', type: 'error', duration: 2000, offset: 80 });
            return;
        }

        let index = 0;
        for (let i = 0; i < tableConfig.value.tableData.length; i++) {
            if (currentRow.value.id == tableConfig.value.tableData[i].id) {
                index = i;
                break;
            }
        }
        if (index + 1 == tableConfig.value.tableData.length) {
            ElNotification({
                title: '操作提示',
                message: '已经是最后一条，不可下移',
                type: 'error',
                duration: 2000,
                offset: 80
            });
        } else {
            let downRow = tableConfig.value.tableData[index + 1];
            let currRow = tableConfig.value.tableData[index];
            let tabIndex = downRow.tabIndex;
            downRow.tabIndex = currRow.tabIndex;
            currRow.tabIndex = tabIndex;
            tableConfig.value.tableData.splice(index + 1, 1);
            tableConfig.value.tableData.splice(index, 0, downRow);
        }
    };

    function saveViewOrder() {
        let ids = [];
        for (let item of tableConfig.value.tableData) {
            ids.push(item.id + ':' + item.tabIndex);
        }
        const loading = ElLoading.service({ lock: true, text: '正在处理中', background: 'rgba(0, 0, 0, 0.3)' });
        saveOrder(ids.toString()).then((res) => {
            loading.close();
            if (res.success) {
                ElNotification({ title: '操作提示', message: res.msg, type: 'success', duration: 2000, offset: 80 });
                reloadTable();
            } else {
                ElNotification({ title: '操作提示', message: res.msg, type: 'error', duration: 2000, offset: 80 });
            }
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
