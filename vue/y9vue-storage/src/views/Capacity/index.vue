<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
            <div class="toolbar">
            <div class="toolbar-left">
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="loadList"
                >
                    <i class="ri-refresh-line"></i>{{ $t('刷新') }}
                </el-button>
            </div>

            <div class="toolbar-right">
                <el-form :inline="true" @submit.native.prevent>
                    <el-form-item :label="$t('人员名称')">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入名称搜索')"
                            class="search-input global-btn-second"
                            clearable
                            @keyup.enter.native="loadList"
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
        <y9Table :config="tableConfig" @on-curr-page-change="onCurrPageChange" @on-page-size-change="onPageSizeChange">
            <template #capacitySlot="{ row, column, index }">
                <el-form ref="capacityForm" :model="formData" class="formClass">
                    <el-form-item v-if="editIndex === index" prop="name">
                        <el-input
                            ref="nameSign"
                            v-model="formData.capacitySize"
                            clearable
                            style="width: 200px; margin-left: 15px"
                            type="number"
                        />
                    </el-form-item>
                    <span v-else>{{ row.capacitySize }}</span>
                </el-form>
            </template>
            <template #opt="{ row, column, index }">
                <div v-if="editIndex === index">
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        plain
                        style="margin-left: 14px"
                        class="global-btn-second"
                        @click="saveData(capacityForm)"
                        ><i class="ri-check-line"></i>{{ $t('确认') }}
                    </el-button>
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        plain
                        class="global-btn-second"
                        @click="cancalData(capacityForm)"
                        ><i class="ri-close-line"></i>{{ $t('取消') }}
                    </el-button>
                </div>
                <div v-else>
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        class="global-btn-third"
                        @click="edit(row, index)"
                    >
                        <i class="ri-settings-line"></i>{{ $t('配置') }}
                    </el-button>
                </div>
            </template>
        </y9Table>
    </y9Card>
</template>

<script lang="ts" setup>
    import { onMounted, computed, reactive, toRefs, inject, ref } from 'vue';
    import CapacityApi from '@/api/storage/capacity';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';

    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 260 - 50);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 260 - 50;
        })();
    };
    const data = reactive({
        capacityForm: '',
        formData: { id: '', capacitySize: '' },
        editIndex: -1,
        userName: '',
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: '60' },
                { title: computed(() => t('所属人')), key: 'capacityOwnerName', width: 'auto' },
                {
                    title: computed(() => t('总存储空间')),
                    key: 'capacitySize',
                    width: '200',
                    sortable: true,
                    slot: 'capacitySlot'
                },
                { title: computed(() => t('剩余存储空间')), key: 'remainingLength', sortable: true, width: '150' },
                { title: computed(() => t('创建时间')), key: 'createTime', sortable: true, width: '180' },
                { title: computed(() => t('修改时间')), key: 'updateTime', sortable: true, width: '180' },
                { title: computed(() => t('操作')), key: 'opt', width: '180', slot: 'opt' }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: 20,
                total: 0
            },
            border: 0,
            height: tableHeight.value
        }
    });

    let { capacityForm, formData, editIndex, userName, tableConfig } = toRefs(data);

    onMounted(() => {
        loadList();
    });

    function loadList() {
        let page = tableConfig.value.pageConfig.currentPage;
        let rows = tableConfig.value.pageConfig.pageSize;
        CapacityApi.getCapacityList(userName.value, page, rows).then((res) => {
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        });
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        tableConfig.value.pageConfig.currentPage = currPage;
        loadList();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        tableConfig.value.pageConfig.pageSize = pageSize;
        loadList();
    }

    async function edit(row, index) {
        editIndex.value = index;
        formData.value.id = row.id;
        let res = await CapacityApi.getCapacityInfo(row.id);
        formData.value.capacitySize = res.data.capacitySize;
    }

    function cancalData(fileForm) {
        editIndex.value = -1;
        formData.value.id = '';
        formData.value.capacitySize = '';
        fileForm.resetFields();
    }

    function saveData(form) {
        if (!form) return;
        form.validate((valid) => {
            if (valid) {
                console.log(formData.value);
                CapacityApi.updateCapacity(formData.value).then((res) => {
                    ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
                    editIndex.value = -1;
                    loadList();
                });
            }
        });
    }
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';

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

        :deep(.el-button) {
            height: 30px;
            line-height: 0;
            min-width: 0px;
            box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
            padding: 8px 15px;
        }
    }

    .formClass .el-form-item {
        margin-bottom: 0px;

        :deep(.el-button) {
            min-width: 40px;
        }
    }

    .search-input {
        width: 250px;
        margin-right: 10px;
    }

    :deep(.el-form-item) {
        display: inline-flex;
        vertical-align: middle;
        margin-right: 0px;
        margin-bottom: 0px;
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

:deep(.el-input-group__append, .el-input-group__prepend) {
    padding: 0 20px 0px 0px !important;
}
</style>
