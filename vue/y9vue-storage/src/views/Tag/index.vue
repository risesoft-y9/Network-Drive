<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
        <div class="toolbar">
            <div class="toolbar-left">
                <el-button-group>
                <el-button v-if="tagManager"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    v-on:click="addTag"
                    plain
                >
                    <i class="ri-folder-add-line"></i>{{ $t('标签') }}
                </el-button>
                <el-button v-if="tagManager"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    :disabled="notCurrentSelectedOwner"
                    class="global-btn-second"
                    v-on:click="deleteSelect"
                    plain
                >
                    <i class="ri-delete-bin-line"></i> {{ $t('删除') }}
                </el-button>

                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="refresh"
                    plain
                    ><i class="ri-refresh-line"></i>{{ $t('刷新') }}</el-button
                >
                <el-button v-if="tagManager"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    :disabled="notCurrentSelectedOwner"
                    class="global-btn-second"
                    v-on:click="viewLog"
                    plain
                >
                    <i class="ri-timeline-view"></i> {{ $t('操作日志') }}
                </el-button>
                </el-button-group>
            </div>
            <div class="toolbar-right">
                <el-form :inline="true" @submit.native.prevent>
                    <el-form-item :label="$t('标签名称')">
                        <el-input
                            v-model="searchKey"
                            :placeholder="$t('输入标签名称搜索')"
                            class="search-input global-btn-second"
                            clearable
                        >
                        <template #append>
                                <el-button
                                    slot="append"
                                    :size="fontSizeObj.buttonSize"
                                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                                    @click="loadList"
                                    ><i class="ri-search-line global-btn-second"></i>
                                </el-button>
                            </template>
                        </el-input>
                    </el-form-item>
                </el-form>
            </div>
        </div>
        </template>
        <el-form class="formClass" ref="fileForm" :model="formData" :rules="rules">
            <y9Table
                ref="multipleTable"
                :config="y9TableConfig"
                v-loading="loading"
                :element-loading-text="loadingTitle"
                element-loading-spinner="el-icon-loading"
                element-loading-background="rgba(0, 0, 0, 0.8)"
                @on-change="handleSelectionChange"
                @row-click="selectRow"
                @on-curr-page-change="onCurrPageChange"
                @on-page-size-change="onPageSizeChange"
            >
                <template #name="{ row, column, index }">
                    <span>{{ row.tagName }}</span>
                    </template>
                    <template #type="{ row, column, index }">
                        {{ row.tagType == 'systemTag' ? $t('系统标签') : $t('自定义标签') }}
                    </template>
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
                    <template #opt="{ row, column, index }">
                        <el-button v-if="tagManager"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-third"
                            @click="editTag(row, index)"
                        >
                            <i class="ri-edit-box-line"></i>{{ $t('编辑') }}
                        </el-button>
                        <span v-else class="line">--</span>
                    </template>
            </y9Table>
        </el-form>
        <y9Dialog v-model:config="dialogConfig">
            <!-- 自定义标签组件 -->
            <AddTag 
                v-if="dialogConfig.type == 'Tag'" 
                ref="tagRef" 
                :fileList="multipleSelection"
                :tagData="tagData"
                @success="handleCustomTagSuccess"
            />
            <OperateLog 
                v-if="dialogConfig.type == 'OperateLog'" 
                ref="operateLogRef" 
            />
        </y9Dialog>
    </y9Card>

</template>

<script lang="ts" setup>
    import { ref, defineProps, onMounted, watch, computed, reactive, toRefs, nextTick, inject } from 'vue';
    import type { ElMessage, ElMessageBox } from 'element-plus';
    import AddTag from '@/components/storage/Tag/addTag.vue';
    import OperateLog from '@/views/Tag/operateLog.vue';
    import FileTagApi from '@/api/storage/fileTag';
    import settings from '@/settings';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { useI18n } from 'vue-i18n';
    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const rules = reactive<FormRules>({
        name: { required: true, message: t('请输入标签名称查询'), trigger: 'blur' }
    });
    
    const data = reactive({
        multipleTable: '',
        loadingTitle: '正在加载中......',
        showHeader: false,
        fileForm: '',
        isEmptyData: false,
        editIndex: '',
        formData: { id: '', tagName: '' ,tagColor: '',tagType: 'systemTag'},
        multipleSelection: [],
        loading: false,
        y9TableConfig: {
            //headerBackground: true,
            border: 0,
            height: window.innerHeight - 310,
            columns: [
                {
                    type: 'selection',
                    width: '55',
                    selectable: (row) => {
                        if (!row.filePassword) {
                            return true;
                        }
                    }
                },
                { title: computed(() => t('序号')), type: 'index', width: '50', align: 'center' },
                { title: computed(() => t('标签名称')), key: 'tagName', align: 'center', width: 'auto',sortable: true, slot: 'name' },
                { title: computed(() => t('标签类型')), key: 'tagType', align: 'center', width: 'auto',sortable: true, slot: 'type' },
                { title: computed(() => t('标签颜色')), key: 'tagColor', align: 'center', width: '120', slot: 'color' },
                { title: computed(() => t('创建人')), key: "createName", align: "center", width: '130', sortable: true },
                { title: computed(() => t('创建时间')), key: 'createTime', align: 'center', width: '240',sortable: true },
                { title: computed(() => t('操作')), key: 'opt', width: '180', slot: 'opt' }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: 20,
                total: 0
            },
        },
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    let formRef = tagRef.value.formRef;
                    if (!formRef) return;
                     formRef.validate().then((valid) => {
                        if (valid) {
                            let formData = tagRef.value.formData;
                            formData.tagType = 'systemTag';
                            formData.listType = 'tagManage';
                            FileTagApi.saveFileTag(formData).then((res) => {
                                let msg = t('标签添加成功');
                                ElMessage({ type: res.success ? 'success' : 'error', message: msg, offset: 65 });
                                loadList();
                            });
                            resolve();
                        } else {
                            reject(new Error('请填写完整标签信息'));
                        }
                    });
                });
            },
            visibleChange: (visible) => {
                if (!visible) {
                }
            }
        },
        nameSign:'',
        searchKey: '',
        tagRef: null,
        tagData: {},
        tagManager: false,
    });

    let {
        multipleTable,
        y9TableConfig,
        dialogConfig,
        loadingTitle,
        fileForm,
        isEmptyData,
        editIndex,
        formData,
        multipleSelection,
        loading,
        showHeader,
        nameSign,
        searchKey,
        tagRef,
        tagData,
        tagManager
    } = toRefs(data);

    onMounted(() => {
        tagManager.value = JSON.parse(sessionStorage.getItem('tagManager'));
        console.log('tagManager:',tagManager);
        loadList();
    });
    
    watch(
        () => searchKey.value,
        (val) => {
            loadList();
        },
        {
            deep: true,
            immediate: true
        }
    );

    function refresh() {
        searchKey.value = '';
        loadList();
    }

    function selectRow(row, column, event) {
        if (row.id != '') {
            multipleTable.value.elTableRef.toggleRowSelection(row);
        } else {
            multipleTable.value.elTableRef.clearSelection();
        }
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
    }

    function loadList() {
        loading.value = true;
        let page = y9TableConfig.value.pageConfig.currentPage;
        let rows = y9TableConfig.value.pageConfig.pageSize;
        FileTagApi.getTagList(
            searchKey.value,
            page,
            rows
        ).then((res) => {
            loading.value = false;
            y9TableConfig.value.tableData = res.rows;
            y9TableConfig.value.pageConfig.total = res.total;
        });
    }

    function viewLog(){
        Object.assign(dialogConfig.value, {
            show: true,
            width: '80%',
            title: computed(() => t('操作日志')),
            type: 'OperateLog',
            showFooter: false
        });
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        y9TableConfig.value.pageConfig.currentPage = currPage;
        loadList();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        y9TableConfig.value.pageConfig.pageSize = pageSize;
        loadList();
    }

    function deleteSelect() {
        var idArr = multipleSelection.value.map((item) => item.id);
        logicDelete(idArr);
    }

    function logicDelete(ids) {
        FileTagApi.checkIsUsed(ids).then(res => { 
            if(res.success){
                if(res.data){
                     const usedTagNames = res.data.map((tag: any) => tag.tagName).join('、');
                     ElMessageBox.confirm(
                        `<p style="line-height: 1.5;">以下标签正在被文件使用：</p>
                        <p style="color: #F56C6C; font-weight: bold; margin: 10px 0;">${usedTagNames}</p>
                        <p>删除后，相关文件将不再关联这些标签。</p>
                        <p>确认要继续删除吗？</p>`,
                        t('警告'),
                        {
                            confirmButtonText: t('确认删除'),
                            cancelButtonText: t('取消'),
                            type: 'warning',
                            dangerouslyUseHTMLString: true,
                            customClass: 'tag-delete-confirm-box' // 可选：自定义样式类名
                        }
                    ).then(() => {
                        // 确认删除，调用真正的删除接口
                        performDelete(ids);
                    }).catch(() => {
                        ElMessage({ type: 'info', message: t('已取消操作'), offset: 65 });
                    });
                } else{
                    ElMessageBox.confirm(t('确认要删除选中的标签吗？'), t('提示'), {
                        confirmButtonText: t('确定'),
                        cancelButtonText: t('取消'),
                        type: 'info'
                    }).then(() => {
                        performDelete(ids);
                    }).catch(() => {
                        ElMessage({ type: 'info', message: t('已取消操作'), offset: 65 });
                    });
                }
            }
        });
    }

    function performDelete(ids: string[]) {
        FileTagApi.deleteFileTag(ids, 'tagManage').then((res) => {
            if (res.success) {
                ElMessage({ type: 'success', message: t('删除成功') });
                loadList();
            } else {
                ElMessage({ type: 'error', message: res.msg || t('删除失败') });
            }
        });
    }

    function addTag() {
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('新增标签')),
            type: 'Tag',
            okText: computed(() => t('确定')),
            cancelText: computed(() => t('取消')),
            showFooter: true
        });
    }

    function editTag(row) {
        tagData.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('编辑标签')),
            type: 'Tag',
            okText: computed(() => t('确定')),
            cancelText: computed(() => t('取消')),
            showFooter: true
        });
    }

</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';

    .formClass .el-form-item--default {
        margin-bottom: 0px;
    }

    .formClass .el-form-item {
        margin-bottom: 0px;
    }

    .fileName {
        position: relative;
        overflow: hidden;
        text-overflow: ellipsis;
        vertical-align: middle;
        white-space: nowrap;
        cursor: pointer;
    }

    .formClass {
        :deep(.el-form-item__error) {
            color: var(--el-color-danger);
            font-size: 12px;
            line-height: 1;
            padding-top: 2px;
            position: relative;
            top: 0%;
            left: 2%;
        }

        :deep(.el-table) {
            .el-table__body {
                .el-table__row:hover {
                    td {
                        border-left: 0px;
                        border-right: 0px;
                        background-color: var(--el-color-primary-light-9);
                    }
                }

                .global-btn-main i {
                    color: var(--el-color-white);
                }
            }
        }
    }

    .optButtonCss i {
        color: var(--el-color-primary);
        font-size: 20px;
        margin-left: 0.5vw;
    }

    :deep(.y9-dialog-content) {
        padding: 15px !important;
    }

    :deep(.y9-dialog-header-title) {
        font-size: v-bind('fontSizeObj.mediumFontSize') !important;
    }

    :deep(.el-form-item) {
        display: inline-flex;
        vertical-align: middle;
        margin-right: 0px;
        margin-bottom: 0px;
    }

    :deep(.aplayer-fixed) {
        position: relative !important;
        padding: 0px 360px;
    }

    :deep(.aplayer-body) {
        position: relative !important;
    }

    :deep(.el-table__cell .cell) {
        padding-left: 0px;
    }

    :deep(.el-table-column--selection .cell) {
        padding-left: 12px;
        padding-right: 12px;
    }

    :deep(.el-table__inner-wrapper::before) {
        height: 0px;
    }

    :deep(.y9-card-content){
        padding: 0px 15px 15px 15px !important;
    }

    .star {
        font-size: 22px;
        color: #d6dde9 !important;
    }

    .tree-div {
        height: 335px;
        overflow-y: auto;
        padding: 10px;
        border: 1px solid #f2f2f2;
    }

    .toolbar:after {
        clear: both;
        content: '';
        display: block;
    }

    .toolbar-left {
        float: left;
        display: flex;
        align-items: center;
    }

    .toolbar-right {
        float: right;

        :deep(.el-button) {
            height: 30px;
            line-height: 0;
            min-width: 0px;
            padding: 8px 15px;
        }
        :deep(.el-form-item__label) {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }

    .search-input {
        width: 12vw;
        margin-right: 10px;
        font-size: v-bind('fontSizeObj.baseFontSize');
    }

    .upload-div {
        display: inline-block;
        margin-right: 10px;
    }

    .toolbar .el-upload {
        display: inline-block;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        outline: none;
    }

    .nav {
        font-size: v-bind('fontSizeObj.baseFontSize');
        padding: 5px 0 11px 0;
    }

    .back {
        display: inline-block;
    }

    .back span:first-child {
        color: var(--el-color-primary);
        cursor: pointer;
    }

    .back .divider {
        margin-left: 10px;
        margin-right: 10px;
    }

    .location {
        display: inline-block;
        padding-left: 0.6vw;
    }

    .location span {
        color: var(--el-color-primary);
        cursor: pointer;
        margin-right: 5px;
    }

    .location span:after {
        content: ' >';
    }

    .location span:last-child {
        color: black;
    }

    :deep(.el-button) {
        min-width: 32px;
        font-size: 12px;
        height: $btnHeight;
        line-height: $btnHeight;
        box-shadow: $boxShadow;
        padding: 0.3vw;

        i {
            margin-right: 0px !important;
        }
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
</style>
