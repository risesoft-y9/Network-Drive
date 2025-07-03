<template>
    <y9Table :config="tableConfig">
        <template #deleteBtn="{ row, column, index }">
            <i
                :title="$t('点击删除')"
                class="ri-delete-bin-2-line"
                style="font-size: 18px"
                @click="deleteRole(row)"
            ></i>
        </template>
    </y9Table>
</template>

<script lang="ts" setup>
    import { ref, defineProps, onMounted, watch, computed, reactive, toRefs } from 'vue';
    import { ElMessage, ElMessageBox } from 'element-plus';
    import FileApi from '@/api/storage/file';
    import FileShareApi from '@/api/storage/fileNodeShare';
    import { useI18n } from 'vue-i18n';

    const { t } = useI18n();
    const props = defineProps({
        fileId: String,
        recordType: String
    });

    const data = reactive({
        tableConfig: {
            columns: [],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: 20,
                total: 0
            },
            border: 0,
            height: 600
        }
    });

    let { tableConfig } = toRefs(data);

    watch(
        () => props.fileId,
        (newVal) => {
            reloadTable();
        }
    );

    onMounted(() => {
        reloadTable();
    });

    async function reloadTable() {
        tableConfig.value.columns = [];
        let page = tableConfig.value.pageConfig.currentPage;
        let rows = tableConfig.value.pageConfig.pageSize;
        let res;
        if (props.recordType == 'downloadRecord') {
            tableConfig.value.columns.push(
                { title: computed(() => t('序号')), type: 'index', width: '60' },
                { title: computed(() => t('下载人')), key: 'downLoadUserName', width: 'auto' },
                { title: computed(() => t('下载时间')), key: 'downLoadTime', width: '180' }
            );
            res = await FileApi.getDownloadRecord(props.fileId, page, rows);
        } else {
            tableConfig.value.columns.push(
                // { title: '', type: 'selection', width: '50', fixed: 'left' },
                { title: computed(() => t('序号')), type: 'index', width: '60' },
                { title: computed(() => t('名称')), key: 'toOrgUnitName', width: 'auto' },
                { title: computed(() => t('操作')), key: 'opt', width: '180', slot: 'deleteBtn' }
            );
            res = await FileShareApi.getFilePublicRecord(props.fileId, page, rows);
        }

        if (res.success) {
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        }
    }

    function deleteRole(row) {
        ElMessageBox.confirm(t('是否删除附件?'), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(() => {
                let ids = [];
                ids.push(row.id);
                FileShareApi.deletePublic(ids).then((res) => {
                    if (res.success) {
                        ElMessage({ type: 'success', message: res.msg, offset: 65 });
                        reloadTable();
                    } else {
                        ElMessage({ type: 'error', message: res.msg, offset: 65 });
                    }
                });
            })
            .catch(() => {
                ElMessage({ type: 'info', message: t('已取消删除'), offset: 65 });
            });
    }
</script>

<style>
    .el-main-table {
        padding: 0px;
    }
</style>
