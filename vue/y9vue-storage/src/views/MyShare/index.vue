<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
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
        </template>
        <y9Table
            ref="multipleTable"
            :config="y9TableConfig"
            v-loading="loading"
            :element-loading-text="loadingTitle"
            :empty-text="$t('暂无数据')"
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
            @on-change="handleSelectionChange"
            @row-click="selectRow"
            @select-all="selectAll"
            >
            <template #name="{ row, column, index }">
                <div class="file-name-cell">
                    <FileNameWithIcon :file-node="row.fileNode" />
                </div>
            </template>
            <template #fileSize="{ row, column, index }">
                <span>{{ row.fileNode.fileSize }}</span>
            </template>
            <template #updateTime="{ row, column, index }">
                <span>{{ row.fileNode.updateTime }}</span>
            </template>
       </y9Table>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, onMounted, reactive, toRefs, inject, computed } from 'vue';
    import FileNodeShareApi from '@/api/storage/fileNodeShare';
    import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';

    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 240);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 240;
        })();
    };
    const data = reactive({
        multipleTable: '',
        multipleSelection: [],
        loading: false,
        loadingTitle: t('加载中...'),
        y9TableConfig: {
            border: 0,
            height: tableHeight.value,
            pageConfig: false,
            columns: [
                {
                    type: 'selection',
                    width: '55',
                },
                { title: computed(() => t('文件名')), key: 'name', align: 'left', minWidth: '500',sortable: true, slot: 'name' },
                { title: computed(() => t('接收人')), key: 'toOrgUnitNames', align: 'left',sortable: true, width: '120' },
                { title: computed(() => t('大小')), key: 'fileSize', width: '100', sortable: true,slot: 'fileSize' },
                { title: computed(() => t('共享日期')), key: 'updateTime', width: '200',sortable: true, slot: 'updateTime' }
            ],
            tableData: []
        },
    });

    let { multipleTable, multipleSelection, loading, loadingTitle, y9TableConfig } = toRefs(data);

    onMounted(() => {
        loadList();
    });

    function selectRow(row, column, event) {
        if (row.id != '') {
            if (!row.filePassword) {
                multipleTable.value.elTableRef.toggleRowSelection(row);
            }
        } else {
            multipleTable.value.elTableRef.clearSelection();
        }
    }

    function selectAll(selection) {
        console.log('selectAll-multipleSelection.value', multipleSelection.value);
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
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
            y9TableConfig.value.tableData = res.data;
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

    :deep(.y9-card-content){
        padding: 0px 15px !important;
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
      }

      &:not(:last-child) {
          border-right: 1px solid #d0d7e7 !important;
        }
      
      &.global-btn-main {
        border-color: #1a73e8;
        
        &:hover {
          border-color: #0d5bb8;
        }
      }
      
      &.global-btn-second {
        background: #fff;
        border: 1px solid #dcdfe6;
        color: #606266;
        
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
  
  .toolbar-right {
    float: right;
    display: flex;
    align-items: center;
    gap: 10px;
    padding-right: 15px;


    
    .el-button {
      transition: all 0.3s ease;
      border-radius: 6px;
      border: none;
      box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, 0.06);
      margin-left: 0px;
      
      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      }
    } 
  }
}

:deep(.toolbar-right .el-dropdown) {
  outline: none !important;
  
  .el-button {
    outline: none !important;
    border: none;
    box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, 0.06);
    
    &:focus,
    &:focus-visible,
    &:active,
    &:hover {
      outline: none !important;
      border-color: #dcdfe6 !important;
      box-shadow: none !important;
    }
    
    &:hover {
      border-color: #586cb1 !important;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
    }
  }
  
  &:focus,
  &:focus-within,
  &:focus-visible,
  &:hover {
    outline: none !important;
  }
}

.file-name-cell {
    display: flex;
    align-items: center;
    width: 100%;
    overflow: hidden;
}
</style>
