<template>
    <div class="tableDataCss">
        <el-tabs v-model="activeName" class="demo-tabs">
            <el-tab-pane label="基本属性" name="first">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="数据源名称" align="center">{{tableDetail.sourceName}}</el-descriptions-item>
                    <el-descriptions-item label="数据源类型" align="center">{{tableDetail.sourceType}}</el-descriptions-item>
                    <el-descriptions-item label="备注" align="center" :span="2">{{tableDetail.remark}}</el-descriptions-item>
                    <el-descriptions-item label="表名称" align="center">{{currNode.name}}</el-descriptions-item>
                    <el-descriptions-item label="中文名称" align="center">{{currNode.cname}}</el-descriptions-item>
                    <el-descriptions-item label="提供方" align="center">{{tableDetail.provider}}</el-descriptions-item>
                    <el-descriptions-item label="联系方式" align="center">{{tableDetail.contact}}</el-descriptions-item>
                    <el-descriptions-item label="数据量" align="center">{{tableDetail.rowCount}}</el-descriptions-item>
                    <el-descriptions-item label="字段数量" align="center">{{tableConfig.columns.length}}</el-descriptions-item>
                    <el-descriptions-item label="数据最新更新时间" align="center">{{tableDetail.maxUpdateTime}}</el-descriptions-item>
                </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="字段属性" name="second">
                <y9Table
                    :config="propsTableConfig"
                >
                </y9Table>
            </el-tab-pane>
            <el-tab-pane label="数据" name="third">
                <y9Table
                    :config="tableConfig"
                    :filterConfig="filterConfig"
                    @on-curr-page-change="onCurrPageChange"
                    @on-page-size-change="onPageSizeChange"
                >
                    <template #addBtn>
                        <el-button class="global-btn-main" type="primary" @click="openSearch"
                            ><i class="ri-search-line"></i>条件查询
                        </el-button>
                    </template>
                </y9Table>
                <el-drawer
                    v-model="drawer"
                    :append-to-body="false"
                    :modal="false"
                    :with-header="false"
                    class="drawerSearch"
                    direction="ttb"
                    size="60%"
                    title=""
                >
                    <p style="color: red;padding-bottom: 5px;">温馨提示：请先勾选要查询的字段，然后再字段的后输入查询的值。</p>
                    <y9Table
                        :config="serachConfig"
                        @select="handleSelect"
                    >
                        <template #columnValue="{row, column, index}">
                            <el-input v-model="row.columnValue" clearable></el-input>
                        </template>
                    </y9Table>
                    <template #footer>
                        <div style="flex: auto">
                            <el-button type="primary" @click="searchData">查询</el-button>
                            <el-button @click="closeDrawer">取消</el-button>
                        </div>
                    </template>
                </el-drawer>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>
<script lang="ts" setup>
    import { computed, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getTableColumns, getTableData, getTableDetail} from '@/api/dataSource/index';
    import { useI18n } from 'vue-i18n';

    const props = defineProps({
        currNode: {
            type: Object,
            default: () => {
                return {};
            }
        },
    });
    const {t} = useI18n();
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 395);
    const propsTableHeight = ref(useSettingStore().getWindowHeight - 323);
    window.onresize = () => {
        return (() => {
            propsTableHeight.value = useSettingStore().getWindowHeight - 323;
            tableHeight.value = useSettingStore().getWindowHeight - 395;
        })();
    };
    
    const data = reactive({
        activeName: 'first',
        drawer: false,
        propsTableConfig: {
            columns: [
                {
                    title: '序号',
                    type: "index",
                    width: "60",
                    align: "center"
                },
                {
                    title: '列名',
                    key: 'columnName',
                    width: "auto",
                    align: "center"
                },
                {
                    title: '类型',
                    key: 'typeName',
                    width: "150",
                    align: "center",
                },{
                    title: '长度',
                    key: 'dataLength',
                    width: "100",
                    align: "center",
                },{
                    title: '非空',
                    key: 'nullable',
                    width: "100",
                    align: "center",
                    render: (row, column, index) => {
                        if(row.nullable){
                            return '-';
                        }else{
                            return '是';
                        }
                    }
                },{
                    title: '主键',
                    key: 'primaryKey',
                    width: "100",
                    align: "center",
                    render: (row, column, index) => {
                        if(row.primaryKey){
                            return '是';
                        }else{
                            return '-';
                        }
                    }
                },
                {
                    title: '注释',
                    key:'comment',
                    width: "200",
                    align: "center"
                },
            ],
            border: true,
            tableData: [],
            pageConfig: false,
            height: propsTableHeight.value,

        },
        tableConfig: {
            columns: [{}],
            border: true,
            tableData: [],
            pageConfig: {
                // 分页配置，false隐藏分页
                currentPage: 1, //当前页数，支持 v-model 双向绑定
                pageSize: 15, //每页显示条目个数，支持 v-model 双向绑定
                total: 0 //总条目数
            },
            height: tableHeight.value,
        },
        filterConfig: {
            //过滤配置
            itemList: [
                {
                    type: 'slot',
                    span: 8,
                    slotName: 'addBtn'
                }
            ]
        },
        serachConfig: {
            columns: [
                {
                    type: "selection",
                    width: 60,
                },
                {
                    title: '查询字段',
                    key:'columnName',
                    width: "auto",
                    sortable: true,
                    align: "center"
                },{
                    title: '查询值',
                    key: 'columnValue',
                    width: "auto",
                    sortable: true,
                    align: "center",
                    slot: 'columnValue'
                },
            ],
            border: true,
            tableData: [{}],
            pageConfig: false,
            height: 400,
        },
        selectData: [],
        columnNameAndValues: ''
    });

    let { activeName,propsTableConfig,filterConfig, tableConfig, drawer,serachConfig ,selectData,columnNameAndValues} = toRefs(data);

    watch(
        () => props.currNode,
        (newVal, oldVal) => {
            getColumns();
        },
        {deep: true}
    );

    onMounted(() => {
        getTableInfo();
        getColumns();
    });

    const tableDetail = ref<any>({});
    function getTableInfo(){
        getTableDetail({sourceId: props.currNode.sourceId, tableName: props.currNode.name}).then(res => {
            if(res.success){
                tableDetail.value = res.data;
            }
        });
    }
    
    async function getColumns(){
        let res = await getTableColumns(props.currNode.sourceId, props.currNode.name);
        if(res.success){
            tableConfig.value.columns = [];
            serachConfig.value.tableData = [];
            for (let item of res.data) {
                tableConfig.value.columns.push({
                    title: computed(() => t(item.comment == null ? item.columnName : item.comment)),
                    key: item.columnName,
                    width: "auto",
                    sortable: true,
                    align: "center"
                });
                serachConfig.value.tableData.push({columnName: item.columnName, columnValue: ''});
            }
            propsTableConfig.value.tableData = res.data;
            getTableDataList();
        }
    }

    async function getTableDataList() {
        let page = tableConfig.value.pageConfig.currentPage;
        let rows = tableConfig.value.pageConfig.pageSize;
        let res = await getTableData(props.currNode.sourceId, props.currNode.name, columnNameAndValues.value, page, rows);
        tableConfig.value.tableData = res.rows;
        tableConfig.value.pageConfig.total = res.total;
    }

    function openSearch(){
        drawer.value = true;
    }

    function handleSelect(id, data) {
        selectData.value = id;
    }

    function searchData(){
        let jsonValue = [];
        selectData.value.forEach((item, index) => {
            if (item) {
                jsonValue[index] = `${item.columnName}:${item.columnValue}`;
            }
        });
        columnNameAndValues.value = jsonValue.join(";");
        //console.log('columnNameAndValues',columnNameAndValues.value);
        tableConfig.value.pageConfig.currentPage = 1;
        getTableDataList();
        drawer.value = false;
    }

    function closeDrawer(){
        drawer.value = false;
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        tableConfig.value.pageConfig.currentPage = currPage;
        getTableDataList();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        tableConfig.value.pageConfig.pageSize = pageSize;
        getTableDataList();
    }
</script>

<style lang="scss" scoped>
    .viewTypeTable .el-form-item--default {
        margin-bottom: 0px;
    }

    .viewTypeTable .el-form-item {
        margin-bottom: 0px;
    }

    .viewTypeTable .el-form-item__error {
        color: var(--el-color-danger);
        font-size: 12px;
        line-height: 1;
        padding-top: 2px;
        position: relative;
        top: 0%;
        left: 0;
    }

    .drawerSearch .el-input {
        display: inline-block;
        width: 200px;
    }
    .tableDataCss {
        :deep(.el-drawer.ttb,.el-drawer.btt) {
            top: 255px !important;
            //width: calc(100% - 815px) !important;
            width: calc(100vw - 46.8vw) !important;
            left: calc(100vw - 75vw) !important;
            right: 0;
            border-top: solid !important;
            border-top-color: var(--el-color-primary) !important;
        }
    }
</style>
