<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-12-02 15:55:05
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-04-17 11:47:28
 * @FilePath: \y9-vue\y9vue-storage\src\components\storage\Tag\index.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <div class="tagTable">
    <y9Table
        ref="tagTable"
        
        :config="tagTableConfig"
        :filterConfig="filterConfig"
        v-loading="loading"
        :element-loading-text="loadingTitle"
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
        @on-change="handleSelectionChange"
        @row-click="selectRow"
        @on-curr-page-change="onCurrPageChange"
        @on-page-size-change="onPageSizeChange"
    >
        <template #color="{ row, column, index }">
            <el-tag
                :style="{
                    backgroundColor: row.tagColor + '33',
                    color: row.tagColor
                }"
                class="preview-tag"
            >
            {{ row.tagName || $t('标签名称') }}
        </el-tag>
        </template>
    </y9Table>
    </div>
</template>
<script lang="ts" setup>
import { reactive,ref,onMounted,onUnmounted,nextTick,computed,toRefs } from 'vue';
import { useI18n } from 'vue-i18n';
import FileTagApi from '@/api/storage/fileTag';
const { t } = useI18n();

const data = reactive({
    tagTable: null,
    tagTableConfig: {
        //headerBackground: true,
        border: 0,
        //height: '400',
        columns: [
            {
                type: 'selection',
                width: '55'
            },
            { title: computed(() => t('标签名称')), key: 'tagName', align: 'left', width: 'auto',sortable: true },
            { title: computed(() => t('标签颜色')), key: 'tagColor', align: 'center', width: 'auto',slot: 'color' },
        ],
        tableData: [],
        pageConfig: false
    },
    loading: false,
    loadingTitle: computed(() => t('加载中...')),
    tagSelect: [],
    searchKey: '',
});

let {
    tagTable,
    tagTableConfig,
    loading,
    loadingTitle,
    tagSelect,
    searchKey
} = toRefs(data);

let filterConfig = ref({
		itemList: [{
				type: "input",
				key: "name",
				span: 24,
                props:{
                    placeholder:'请输入标签名称查询'
                }
			},
		
		],
		filtersValueCallBack:(filters) => {
			console.log("过滤值",filters)
            searchKey.value = filters.name;
            loadList();
		}
	})

onMounted(() => {
    loadList();
});

function handleSelectionChange(id, data) {
    tagSelect.value = data;
}

function loadList() {
    loading.value = true;
    let page = tagTableConfig.value.pageConfig.currentPage;
    let rows = tagTableConfig.value.pageConfig.pageSize;
    FileTagApi.getAllTagList(
        searchKey.value,
    ).then((res) => {
        loading.value = false;
        tagTableConfig.value.tableData = res.data;
        //tagTableConfig.value.pageConfig.total = res.total;
    });
}

//当前页改变时触发
function onCurrPageChange(currPage) {
    tagTableConfig.value.pageConfig.currentPage = currPage;
    loadList();
}

//每页条数改变时触发
function onPageSizeChange(pageSize) {
    tagTableConfig.value.pageConfig.pageSize = pageSize;
    loadList();
}

function selectRow(row, column, event) {
        if (row.id != '') {
            if (!row.filePassword) {
                tagTable.value.elTableRef.toggleRowSelection(row);
            }
        } else {
            tagTable.value.elTableRef.clearSelection();
        }
}

defineExpose({
    tagSelect,
})
</script>
<style lang="scss" scoped>
.tagTable {
    :deep(.el-scrollbar) {
        height: 400px !important;
    }
}
</style>