<template>
    <div class="tableDataCss">
    <y9Table
        :config="tableConfig"
        :filterConfig="filterConfig"
        @on-curr-page-change="onCurrPageChange"
        @on-page-size-change="onPageSizeChange"
    >
        <template #addBtn>
            <el-button class="global-btn-main" type="primary" @click="searchData"
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
        size="30%"
        title="">
        <y9Table
        :config="serachConfig">
            <template #columnValue="{row,column,index}">
                <el-input v-model="serachConfig.tableData[index].columnValue" clearable></el-input>
            </template>
    </y9Table>
    </el-drawer>
</div>
</template>
<script lang="ts" setup>
    import { reactive, ref } from 'vue';
    import type { ElLoading, ElMessage } from 'element-plus';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { remove, saveOrUpdate, getCategoryList } from '@/api/dataAssets/category';
    import { getTableColumns ,getTableData} from '@/api/dataSource/index';
import { column } from 'element-plus/es/components/table-v2/src/common';

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
    const tableHeight = ref(useSettingStore().getWindowHeight - 373);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 373;
        })();
    };
    
    const data = reactive({
        drawer: false,
        tableConfig: {
            columns: [],
            border: true,
            tableData: [],
            pageConfig: {
                // 分页配置，false隐藏分页
                currentPage: 1, //当前页数，支持 v-model 双向绑定
                pageSize: 15, //每页显示条目个数，支持 v-model 双向绑定
                total: 0 //总条目数
            },
            height: tableHeight.value
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
                }
            ],
            border: true,
            tableData: [],
            pageConfig: false,
            height: 400
        },
    });

    let { filterConfig, tableConfig, drawer,serachConfig } = toRefs(data);

    watch(
        () => props.currNode,
        (newVal, oldVal) => {
            getColumns();
        },
        {deep: true}
    );
    getColumns();
    async function getColumns(){
        let res = await getTableColumns(props.currNode.url, props.currNode.name);
        if(res.success){
            tableConfig.value.columns = [];
            
            for (let item of res.data) {
                tableConfig.value.columns.push({
                    title: computed(() => t(item.comment==null?item.columnName:item.comment)),
                    key: item.columnName,
                    width: "auto",
                    sortable: true,
                    align: "center"
                });
                serachConfig.value.tableData.push({columnName:item.columnName,columnValue:''});
            }
            getTableDataList();
        }
    }

    async function getTableDataList() {
        let page = tableConfig.value.pageConfig.currentPage;
        let rows = tableConfig.value.pageConfig.pageSize;
        let res = await getTableData(props.currNode.url, props.currNode.name,page,rows);
        tableConfig.value.tableData = res.rows;
        tableConfig.value.pageConfig.total = res.total;
    }

    function searchData(){
        drawer.value = true
        
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
            left: calc(100vw - (100vw - 44.4vw)) !important;
            right: 0;
            border-top: solid !important;
            border-top-color: var(--el-color-primary) !important;
        }
    }
</style>
