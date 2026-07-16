<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
            <div class="toolbar">
            <div class="toolbar-left">
                <el-button
                    v-if="multipleSelection && multipleSelection.length >= 1"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    type="primary"
                    @click="cancelShare"
                    ><i class="ri-indeterminate-circle-line"></i> {{ $t('取消直链分享') }}
                </el-button>
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
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
            <!-- 提取码表头 -->
            <template #passwordHeader="{ column, index }">
                <div class="password-header">
                    <span>{{ $t('提取码') }}</span>
                    <i
                        :class="showAllPasswords ? 'ri-eye-line' : 'ri-eye-close-line'"
                        class="password-toggle-icon"
                        @click.stop="toggleAllPasswords"
                    ></i>
                </div>
            </template>
            <!-- 提取码单元格 -->
            <template #linkPassword="{ row, column, index }">
                <div class="password-cell" @click.stop="toggleSinglePassword(row)">
                    <span
                        :class="{ 'password-dimmed': !showAllPasswords && visiblePasswordId === row.id }"
                        class="password-cell-text"
                    >
                        {{ getPasswordDisplay(row) }}
                    </span>
                    <i
                        :class="isPasswordVisible(row) ? 'ri-eye-line' : 'ri-eye-close-line'"
                        class="password-cell-icon"
                    ></i>
                </div>
            </template>
            <template #name="{ row, column, index }">
                <el-row @mouseenter="titleHover(row.id)" @mouseleave="titleLeave(row.id)">
                        <el-col :span="20" class="fileName">
                            <FileNameWithIcon :file-node="row.fileNode" />
                        </el-col>
                        <el-col :span="4">
                            <div v-if="optButtonShow == row.id" class="optButtonCss">
                                <el-tooltip
                                        v-if="row.fileType != 0"
                                        class="box-item"
                                        effect="light"
                                        :content="$t('复制链接')"
                                        placement="top-start"
                                    >
                                        <i class="ri-links-fill" @click="copyLink(row)"></i>
                                    </el-tooltip>
                                    <el-tooltip
                                        class="box-item"
                                        effect="light"
                                        :content="$t('取消直链分享')"
                                        placement="top-start"
                                    >
                                        <i class="ri-link-unlink" @click="cancelLink(row.id)"></i>
                                    </el-tooltip>
                                    
                            </div>
                        </el-col>
                    </el-row>
            </template>
       </y9Table>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, onMounted, reactive, toRefs, inject, computed } from 'vue';
    import FileApi from '@/api/storage/file';
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
    // 提取码显示控制
    const showAllPasswords = ref(false);
    const visiblePasswordId = ref<any>(null);

    function toggleAllPasswords() {
        showAllPasswords.value = !showAllPasswords.value;
        visiblePasswordId.value = null;
    }

    function toggleSinglePassword(row: any) {
        if (showAllPasswords.value) return; // 全部显示时不响应单行点击
        if (visiblePasswordId.value === row.id) {
            visiblePasswordId.value = null;
        } else {
            visiblePasswordId.value = row.id;
        }
    }

    function getPasswordDisplay(row: any): string {
        if (showAllPasswords.value || visiblePasswordId.value === row.id) {
            return row.linkPassword || '';
        }
        return '******';
    }

    function isPasswordVisible(row: any): boolean {
        return showAllPasswords.value || visiblePasswordId.value === row.id;
    }

    const data = reactive({
        optButtonShow:'',
        buttonMore: false,
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
                { title: computed(() => t('文件名')), key: 'fileName', align: 'left', minWidth: '300',sortable: true ,slot: 'name'},
                { title: computed(() => t('下载码')), key: 'linkPassword', slot: 'linkPassword', align: 'left', width: '150', headerSlot: 'passwordHeader' },
                { title: computed(() => t('浏览次数')), key: 'browseCount', width: '100', sortable: true },
                { title: computed(() => t('有效期设置')), key: 'validPeriod', width: '150', sortable: true },
                { title: computed(() => t('到期时间')), key: 'expireTime', align: 'left',sortable: true, width: '200' },
                { title: computed(() => t('分享时间')), key: 'createTime', width: '200',sortable: true }
            ],
            tableData: []
        },
    });

    let { optButtonShow,buttonMore,multipleTable, multipleSelection, loading, loadingTitle, y9TableConfig } = toRefs(data);

    onMounted(() => {
        loadList();
    });

    function titleHover(id) {
        optButtonShow.value = id;
    }

    function titleLeave(id) {
        if (buttonMore.value) {
            optButtonShow.value = id;
        } else {
            optButtonShow.value = '';
        }
    }

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

    function copyLink(row){
        if (row.linkUrl) {
            let linkUrl = "下载链接：" + row.linkUrl + "\n下载码：" + row.linkPassword;
            navigator.clipboard.writeText(linkUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
        } else {
            ElMessage({ type: 'error', message: t('链接为空'), offset: 65 });
        }
    }

    function cancelLink() {
        ElMessageBox.confirm(t("取消直链分享后，该条分享记录将被删除，将无法再访问此直链分享链接。您确认要取消分享吗？"), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(() => {
                var IdArr = multipleSelection.value.map((item) => item.id);
                FileApi.cancelShareLink(IdArr).then((res) => {
                    ElMessage({ type: res.success ? 'success' : 'error', message: t(res.msg), offset: 65 });
                    if (res.success) {
                        loadList();
                    }
                });
            })
            .catch(() => {
                ElMessage({ type: 'info', message: t('已取消操作'), offset: 65 });
            });
    }

    function loadList() {
        loading.value = true;
        FileApi.myShareLinks().then((res) => {
            loading.value = false;
            const data = (res.data || []).map((item: any) => {
                if (item.linkUrl && !item.linkUrl.endsWith('/link/')) {
                    item.linkUrl = item.linkUrl + '/link/' + item.linkKey;
                }
                return item;
            });
            y9TableConfig.value.tableData = data;
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
    background: white;
    box-shadow: 0 0.1px 0.2px rgba(0, 0, 0, 0.1);
  
  .toolbar-left {
    float: left;
    display: flex;
    align-items: center;
    gap: 5px;
    padding-left: 15px;
    
    .el-button {
      transition: all 0.3s ease;
      padding: 10px 10px;
      
      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
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
      box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, 0.06);
      margin-left: 0px;
      
      &:hover {
        //box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      }
    } 
  }
}

:deep(.toolbar-right .el-dropdown) {
  outline: none !important;
  
  .el-button {
    outline: none !important;
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

.optButtonCss i {
        color: var(--el-color-primary);
        font-size: 20px;
        margin-left: 0.5vw;
    }

.password-header {
    display: flex;
    align-items: center;
    gap: 4px;

    .password-toggle-icon {
        cursor: pointer;
        font-size: 14px;
        color: #909399;
        transition: color 0.2s;

        &:hover {
            color: #586cb1;
        }
    }
}

.password-cell {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    user-select: none;

    .password-cell-text {
        &.password-dimmed {
            color: #b0b0b0;
        }
    }

    .password-cell-icon {
        font-size: 14px;
        color: #909399;
        transition: color 0.2s;

        &:hover {
            color: #586cb1;
        }
    }
}

</style>
