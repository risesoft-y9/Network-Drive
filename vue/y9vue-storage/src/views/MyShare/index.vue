<template>
    <y9Card :showHeader="false">
        <div class="toolbar">
            <div class="toolbar-left">
                <el-button
                    :disabled="multipleSelection && multipleSelection.length === 0"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="cancelShare"
                    ><i class="ri-indeterminate-circle-line"></i> {{ $t('取消共享') }}
                </el-button>
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    type="primary"
                    @click="loadList"
                    ><i class="ri-refresh-line"></i>{{ $t('刷新') }}
                </el-button>
            </div>
            <div class="toolbar-right"> </div>
        </div>
        <el-table
            ref="multipleTable"
            v-loading="loading"
            :data="rows"
            :empty-text="$t('暂无文件')"
            :height="tableHeight"
            style="width: 100%"
            tooltip-effect="dark"
            @selection-change="handleSelectionChange"
            @row-click="toggleSelection"
        >
            <el-table-column type="selection" width="45"></el-table-column>
            <!-- <el-table-column type="index" label="序号" width="60"> </el-table-column> -->
            <el-table-column :label="$t('文件名')">
                <template #default="name">
                    <FileNameWithIcon :file-node="name.row.fileNode" />
                </template>
            </el-table-column>
            <el-table-column :label="$t('接收人')" prop="toOrgUnitNames" width="220"></el-table-column>
            <el-table-column :label="$t('大小')" prop="fileNode.fileSize" width="120">
                <template #default="size">
                    {{ size.row.fileNode.fileSize ? size.row.fileNode.fileSize : '-' }}
                </template>
            </el-table-column>
            <el-table-column :label="$t('共享日期')" prop="fileNode.updateTime" width="170"></el-table-column>
        </el-table>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, onMounted, reactive, toRefs, inject } from 'vue';
    import FileNodeShareApi from '@/api/storage/fileNodeShare';
    import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
    import { useI18n } from 'vue-i18n';

    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const data = reactive({
        multipleTable: '',
        rows: [],
        multipleSelection: [],
        loading: false,
        tableHeight: window.innerHeight - 240,
        tableScreenHeight: window.innerHeight
    });

    let { multipleTable, rows, multipleSelection, loading, tableHeight, tableScreenHeight } = toRefs(data);

    onMounted(() => {
        loadList();
        window.onresize = () => {
            return (() => {
                window.screenHeight = window.innerHeight;
                tableScreenHeight.value = window.screenHeight;
            })();
        };
    });

    function toggleSelection(rows) {
        if (rows) {
            multipleTable.value.toggleRowSelection(rows);
        } else {
            multipleTable.value.clearSelection();
        }
    }

    function handleSelectionChange(val) {
        multipleSelection.value = val;
    }

    function cancelShare() {
        ElMessageBox.confirm(t("您确定要对所选文件取消共享吗？取消共享的文件会移动至'全部文件'中"), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(() => {
                var IdArr = multipleSelection.value.map((item) => item.fileNode.id);
                FileNodeShareApi.cancelShare(IdArr).then(() => {
                    loadList();
                });
            })
            .catch(() => {
                ElMessage({ type: 'info', message: t('已取消操作'), offset: 65 });
            });
    }

    function loadList() {
        loading.value = true;
        FileNodeShareApi.list().then((res) => {
            loading.value = false;
            rows.value = res.data;
        });
    }
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';

    :deep(.el-table__cell .cell) {
        padding-left: 0px;
        font-size: v-bind('fontSizeObj.baseFontSize');
    }

    :deep(.el-table-column--selection .cell) {
        padding-left: 12px;
        padding-right: 12px;
    }

    :deep(.el-table__inner-wrapper::before) {
        height: 0px;
    }

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
    }

    :deep(.el-table__empty-text) {
        font-size: v-bind('fontSizeObj.baseFontSize');
    }
</style>
