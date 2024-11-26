<template>
  <y9Card :showHeader="false" class="recordList">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-form :inline="true" @submit.native.prevent v-model="searchForm">
          <el-form-item :label="$t('档号')" style="width: 150px">
            <el-input
                v-model="searchForm.archiveNo"
                :placeholder="$t('输入档号搜索')"
                class="search-input global-btn-second"
                clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('题名')">
            <el-input
                v-model="searchForm.title"
                :placeholder="$t('输入题名搜索')"
                class="search-input global-btn-second"
                clearable
            >
            </el-input>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar-right">
        <el-button
            :size="fontSizeObj.buttonSize"
            :style="{ fontSize: fontSizeObj.baseFontSize }"
            class="global-btn-main"
            @click="search"
        ><i class="ri-search-line"></i>{{ $t('查询') }}
        </el-button
        >
        <el-button
            :size="fontSizeObj.buttonSize"
            :style="{ fontSize: fontSizeObj.baseFontSize }"
            class="global-btn-second"
            @click="reset"
        ><i class="ri-refresh-line"></i>{{ $t('重置') }}
        </el-button
        >
        <el-button
            :size="fontSizeObj.buttonSize"
            :style="{ fontSize: fontSizeObj.baseFontSize }"
            class="global-btn-second"
            @click="openHignSearch"
        ><i class="ri-search-line"></i>{{ $t('高级搜索') }}
        </el-button
        >
      </div>
    </div>
    <el-row :gutter="30" style="padding: 0px 10px 10px 0px">
      <el-col :span="3">
        <el-dropdown ref="dropdownIsShowRef" :hide-on-click="false" :max-height="dropdown_height">
                    <span class="el-dropdown-link">
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-main"
                        ><i class="ri-eye-line"></i>{{ $t('显示/隐藏列') }}&nbsp;&nbsp;<i
                            class="ri-arrow-down-s-line"
                        ></i
                        ></el-button>
                    </span>
          <template #dropdown>
            <el-dropdown-menu>
              <!-- <el-dropdown-item>
                  <el-checkbox
                      v-model="checkAll"
                      :indeterminate="isIndeterminate"
                      @change="handleCheckAllChange"
                  >
                      全选/取消
                  </el-checkbox>
              </el-dropdown-item> -->
              <template v-for="(item,index) in metadataFieldList" :key="item.id">
                <el-dropdown-item style="overflow-y: auto;">
                  <el-checkbox
                      v-model="fieldChecked[index]"
                      :checked="item.isListShow"
                      false-value="0"
                      label="Option 1"
                      size="large"
                      true-value="1"
                      @change="(val) =>selectShowOrHide(val, item.id)"
                  >{{ $t(item.disPlayName) }}
                  </el-checkbox
                  >
                </el-dropdown-item>
              </template>
              <el-dropdown-item>
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    @click="saveChecked"
                ><i class="ri-save-line"></i>{{ $t('保存') }}
                </el-button
                >
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col :span="2">
        <el-button
            :size="fontSizeObj.buttonSize"
            :style="{ fontSize: fontSizeObj.baseFontSize }"
            class="global-btn-main"
            style="width: 90px"
            @click="addRecord"
        ><i class="ri-file-add-line"></i>{{ $t('新增') }}
        </el-button>
      </el-col>
      <el-col :span="6">
        <el-dropdown :hide-on-click="false">
                    <span class="el-dropdown-link">
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-main"
                        ><i class="ri-list-settings-line"></i>{{ $t('批量操作') }}&nbsp;&nbsp;<i
                            class="ri-arrow-down-s-line"
                        ></i
                        ></el-button>
                    </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item style="width: 100px;">
                    <span
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        @click="deleteSelect"
                    ><i class="ri-delete-bin-2-line"></i>{{ $t('批量删除') }}</span>
              </el-dropdown-item>
              <el-dropdown-item style="width: 100px;">
                    <span
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        @click="batchArchiving"
                    ><i class="ri-clockwise-2-line"></i>{{ $t('批量归档') }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col :span="6">
        <div class="grid-content ep-bg-purple"/>
      </el-col>
    </el-row>
    <el-drawer
        v-model="drawer"
        :append-to-body="false"
        :modal="false"
        :with-header="false"
        class="drawerSearch"
        direction="ttb"
        size="30%"
        title=""
    >
      <div style="margin: 8px;">
        <el-form ref="searchForm" :inline="true" :inline-message="true" :status-icon="true" label-position="right"
                 label-width="120px">
          <el-form-item v-for="(item,index) in searchConf"
                        :key="index"
                        :label="(item.labelName == undefined || item.labelName == '' || item.labelName == null) ? item.disPlayName : item.labelName">
            <template v-if="item.inputBoxType == 'input' || item.inputBoxType == 'textarea'">
              <el-input v-model="searchValue[index]" clearable></el-input>
            </template>
            <template v-if="item.inputBoxType == 'select'">
              <el-select v-model="searchValue[index]" :placeholder="`请选择` + ((item.labelName == undefined || item.labelName == '' || item.labelName == null) ? item.disPlayName : item.labelName)"
                         clearable
                         style="width: 175px">
                <el-option
                    v-for="(option,index) in item.optionClass"
                    :key="index"
                    :label="option.label == undefined ? option.value : option.label"
                    :value="option.value"
                />
              </el-select>
            </template>
            <template v-if="item.queryType == 'date'">
              <el-date-picker
                  v-model="item.value"
                  :range-separator="$t('至')"
                  clearable
                  type="daterange"
                  unlink-panels
                  value-format="YYYY-MM-DD"
              />
            </template>
            <template v-if="item.inputBoxType == 'dateTime'">
              <el-date-picker
                  v-model="item.value"
                  :range-separator="$t('至')"
                  clearable
                  type="datetimerange"
                  unlink-panels
                  value-format="YYYY-MM-DD hh:mm:ss"
              />
            </template>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div style="text-align: center;margin: 15px 10px 10px;">
                        <span slot="footer" class="dialog-footer">
                        <el-button size="small" type="primary" @click="hignSearch">查询</el-button>
                        <el-button size="small" type="primary" @click="resetHignSearch">重置</el-button>
                        <el-button size="small" @click="drawer = false">取消</el-button>
                        </span>
        </div>
      </template>
    </el-drawer>
    <y9Table
        :config="dataTableConfig"
        @select="handleSelect"
        @on-curr-page-change="onCurrPageChange"
        @on-page-size-change="onPageSizeChange"
        @select-all="handleSelect"
    >
      <template #hasFile="{ row, column, index }">
        <i v-if="row.hasFile" class="ri-attachment-2"></i>
        <span v-else>无</span>
      </template>
      <template #optButton="{ row, column, index }">
        <span style="font-weight: 600" @click="editRecord(row)"><i class="ri-edit-line"></i>编辑</span>
        <span style="font-weight: 600;margin-left: 10px;" @click="fileManage(row)"><i
            class="ri-folder-settings-line"></i>文件管理</span>
      </template>
    </y9Table>
    <y9Dialog v-model:config="dialogConfig">
      <detail v-if="dialogConfig.type == 'addRecord'" ref="detailRef" :formData="formData"
              :metadataFieldList="metadataFieldList"/>
              <Uploader
                v-if="dialogConfig.type == 'Uploader'" :archivesId="archivesId"
            />
    </y9Dialog>
  </y9Card>
</template>

<script lang="ts" setup>
import {$deepAssignObject} from '@/utils/object.ts';
import {useSettingStore} from '@/store/modules/settingStore';
import {getMetadataFieldList, saveListFiledShow} from '@/api/archives/metadata';
import {deleteData, getArchivesList, saveFormData,recordArchiving} from '@/api/archives/archives';
import {getOptionValueList} from '@/api/archives/dictionaryOption';
import detail from '@/views/common/detail.vue';

const props = defineProps({
  currTreeNodeInfo: {
    //当前tree节点信息
    type: Object,
    default: () => {
      return {};
    }
  },
  parentCatalog: String
});
const {t} = useI18n();
const fontSizeObj: any = inject('sizeObjInfo') || {};
let total = ref(0);
//调整表格高度适应屏幕
const tableHeight = ref(useSettingStore().getWindowHeight - 362);

window.onresize = () => {
  return (() => {
    tableHeight.value = useSettingStore().getWindowHeight - 362;
  })();
};
const dropdown_height = ref(useSettingStore().getWindowHeight - 400);
const data = reactive({
  searchForm: {},
  detailRef: '',
  formData: {},
  selectData: [],
  searchConf: [],
  searchValue: [],
  optType: '',
  drawer: false,
  //当前节点信息
  currInfo: props.currTreeNodeInfo,
  dataTableConfig: {
    columns: [],
    tableData: [],
    pageConfig: {
      // 分页配置，false隐藏分页
      currentPage: 1, //当前页数，支持 v-model 双向绑定
      pageSize: 20, //每页显示条目个数，支持 v-model 双向绑定
      total: total.value //总条目数
    },
    height: tableHeight.value
  },
  //弹窗配置
  dialogConfig: {
    show: false,
    title: '',
    onOkLoading: true,
    onOk: (newConfig) => {
      return new Promise(async (resolve, reject) => {
        const fieldInstance = detailRef.value.detailFormRef;
        fieldInstance.validate(async (valid) => {
          if (valid) {
            let res = {success: false, msg: ''};
            let isAllEmpty = detailRef.value.checkIfAllEmpty();
            if (isAllEmpty) {
              ElNotification({
                title: '失败',
                message: "请输入或者填写内容",
                type: 'error',
                duration: 2000,
                offset: 80
              });
              reject();
              return;
            }
            let formData = detailRef.value.detailForm;
            res = await saveFormData(optType.value, props.currTreeNodeInfo.id, JSON.stringify(formData));
            if (res == undefined) {
              reject();
              return;
            }
            ElNotification({
              title: res.success ? '成功' : '失败',
              message: res.msg,
              type: res.success ? 'success' : 'error',
              duration: 2000,
              offset: 80
            });
            if (res.success) {
              getRecordList();
            }
            resolve();
          } else {
            ElMessage({
              type: 'error',
              message: '验证不通过，请检查',
              offset: 65
            });
            reject();
          }
        });
      });
    },
    visibleChange: (visible) => {
      // console.log('visible',visible)
    }
  },
  metadataFieldList: [],//元数据字段列表
  fieldChecked: [],
  selectField: [],
  dropdownIsShowRef: '',
  columnNameAndValues: '',
  archivesId:'',
});

let {
  searchForm,
  detailRef,
  formData,
  selectData,
  searchConf,
  searchValue,
  optType,
  drawer,
  currInfo,
  dataTableConfig,
  dialogConfig,
  metadataFieldList,
  fieldChecked,
  selectField,
  dropdownIsShowRef,
  columnNameAndValues,
  archivesId,
} = toRefs(data);

watch(
    () => props.currTreeNodeInfo,
    (newVal, oldVal) => {
      currInfo.value = $deepAssignObject(currInfo.value, newVal);
      //getMetadataField();
      getRecordList();
    },
    {deep: true}
);

onMounted(() => {
    getMetadataField();
});

async function getMetadataField() {
  dataTableConfig.value.columns = [];
  dataTableConfig.value.columns = [{
    type: "selection",
    width: 60,
  },{
      title: computed(() => t('附件')),
      key: 'hasFile',
      width: '50',
      align: 'center',
      slot: 'hasFile'
    }];
  let res = await getMetadataFieldList(props.currTreeNodeInfo.id);
  if (res.success) {
    metadataFieldList.value = res.data;
    for (let item of metadataFieldList.value) {
      //if (item.isListShow == 1) {
        dataTableConfig.value.columns.push({
          title: computed(() => t(item.disPlayName)),
          key: item.columnName,
          width: item.disPlayWidth,
          sortable: item.isOrder == 1 ? true : false,
          align: item.disPlayAlign
        });
      //}
      if (item.openSearch == 1) {
        if (item.inputBoxType == 'radio' || item.inputBoxType == 'checkbox' || item.inputBoxType == 'select') {
          if (item.optionClass) {
            getOptionValueList(item.optionClass).then((res) => {
              if (res.success) {
                let data = res.data;
                let option = []; //选项
                for (let obj of data) {
                  let optionObj = {};
                  optionObj.value = obj.code;
                  optionObj.label = obj.name;
                  option.push(optionObj);
                }
                item.optionClass = option;
              }
            });
          }
        }
        searchConf.value.push(item);
      }
    }

    dataTableConfig.value.columns.push({
      title: computed(() => t('操作')),
      key: 'optButton',
      width: '180',
      align: 'center',
      fixed: 'right',
      slot: 'optButton'
    });
    getRecordList();
  }
}

function selectShowOrHide(val, id) {
  if (selectField.value.length == 0) {
    selectField.value.push({id: id, isShow: val});
  } else {
    let has = false;
    for (let index = 0; index < selectField.value.length; index++) {
      if (selectField.value[index].id == id) {
        selectField.value[index].isShow = val;
        has = true;
      }
    }
    if (!has) {
      selectField.value.push({id: id, isShow: val});
    }
  }
}

function handleSelect(id, data) {
  selectData.value = id;
}

async function getRecordList() {
  dataTableConfig.value.tableData = [];
  let page = dataTableConfig.value.pageConfig.currentPage;
  let rows = dataTableConfig.value.pageConfig.pageSize;
  let res = await getArchivesList(
      props.currTreeNodeInfo.id,
      columnNameAndValues.value,0,
      page,
      rows
  );
  if (res.success) {
    metadataFieldList.value.forEach((item) => {
      if (item.dataType == 'Date' || item.re_inputBoxType == 'date' || item.re_inputBoxType == 'dateTime') {
        res.rows.forEach((row) => {
          row[item.columnName] = convertTimestamp(row[item.columnName], item.re_inputBoxType);
        });
      }
    })
    console.log('rowaaa', res.rows);
    dataTableConfig.value.tableData = res.rows;
    dataTableConfig.value.pageConfig.total = res.total;
  }
}

function search(){
    columnNameAndValues.value = '';
    if(searchForm.value.archiveNo){
        columnNameAndValues.value = 'archiveNo:'+searchForm.value.archiveNo;
    }
    if(searchForm.value.title){
        columnNameAndValues.value = 'title:'+searchForm.value.title;
    }
    dataTableConfig.value.pageConfig.currentPage = 1;
    getRecordList();
}

function reset() {
    columnNameAndValues.value = '';
    searchForm.value = {};
    dataTableConfig.value.pageConfig.currentPage = 1;
    getRecordList();
}

function hignSearch() {
  let jsonValue = [];
  searchValue.value.forEach((item, index) => {
    if (item) {
      jsonValue[index] = `${searchConf.value[index].columnName}:${item}`;
    }
  });
  columnNameAndValues.value = jsonValue.join(";");
  drawer.value = false;
  dataTableConfig.value.pageConfig.currentPage = 1;
  getRecordList();
}

function resetHignSearch() {
  searchValue.value = [];
  columnNameAndValues.value = '';
  drawer.value = false;
  dataTableConfig.value.pageConfig.currentPage = 1;
  getRecordList();
}

function openHignSearch() {
  drawer.value = true;
}

function convertTimestamp(timestamp, timeType) {
  if (timestamp) {
    const date = new Date(Number(timestamp));
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    if (timeType == 'date') {
      return `${year}-${month}-${day}`;
    } else {
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
}

//当前页改变时触发
function onCurrPageChange(currPage) {
  dataTableConfig.value.pageConfig.currentPage = currPage;
  getRecordList();
}

//每页条数改变时触发
function onPageSizeChange(pageSize) {
  dataTableConfig.value.pageConfig.pageSize = pageSize;
  getRecordList();
}

function addRecord() {
  optType.value = 'add';
  Object.assign(dialogConfig.value, {
    show: true,
    width: '50%',
    type: 'addRecord',
    title: '新增文件',
    showFooter: true,
    margin: '2vh auto'
  });
}

function editRecord(row) {
  formData.value = row;
  optType.value = 'edit';
  Object.assign(dialogConfig.value, {
    show: true,
    width: '50%',
    type: 'addRecord',
    title: '修改文件',
    showFooter: true,
    margin: '2vh auto'
  });
}

function fileManage(row) {
  archivesId.value = row.archivesId;
  Object.assign(dialogConfig.value, {
    show: true,
    width: '40%',
    type: 'Uploader',
    title: '上传文件',
    showFooter: true,
  });
}

async function deleteSelect() {
  if (selectData.value.length == 0) {
    ElNotification({
      title: '操作提示',
      message: '请勾选要删除的数据',
      type: 'error',
      duration: 2000,
      offset: 80
    });
    return;
  }
  ElMessageBox.confirm('你确定要删除的选中的数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
      .then(async () => {
        let result = {success: false, msg: ''};
        let ids = [];
        for (let obj of selectData.value) {
          ids.push(obj.archivesId);
        }
        result = await deleteData(props.currTreeNodeInfo.id, ids.join(','));
        ElNotification({
          title: result.success ? '成功' : '失败',
          message: result.msg,
          type: result.success ? 'success' : 'error',
          duration: 2000,
          offset: 80
        });
        if (result.success) {
          getRecordList();
        }
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消删除',
          offset: 65
        });
      });
}

async function batchArchiving(){
    if (selectData.value.length == 0) {
        ElNotification({
        title: '操作提示',
        message: '请勾选要归档的数据',
        type: 'error',
        duration: 2000,
        offset: 80
        });
        return;
    }

    let result = {success: false, msg: ''};
    let ids = [];
    for (let obj of selectData.value) {
        ids.push(obj.archivesId);
    }
    result = await recordArchiving( ids.join(','));
    ElNotification({
        title: result.success ? '成功' : '失败',
        message: result.msg,
        type: result.success ? 'success' : 'error',
        duration: 2000,
        offset: 80
    });
    if (result.success) {
        getRecordList();
    }
}

async function saveChecked() {
  console.log('jsonStr', JSON.stringify(selectField.value));
  let res = await saveListFiledShow(JSON.stringify(selectField.value));
  ElNotification({
    title: res.success ? '成功' : '失败',
    message: res.msg,
    type: res.success ? 'success' : 'error',
    duration: 2000,
    offset: 80
  });
  if (res.success) {
    dropdownIsShowRef.value.handleClose();
    getMetadataField();
  }
}

</script>

<style lang="scss" scoped>
@import '../../../theme/global.scss';

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
  }
}

.drawerSearch .el-input {
  display: inline-block;
  width: 200px;
}

.recordList {
  :deep(.el-drawer.ttb,.el-drawer.btt) {
    top: 255px !important;
    //width: calc(100% - 815px) !important;
    width: calc(100vw - 40.2vw) !important;
    left: calc(100vw - (100vw - 37.8vw)) !important;
    right: 0;
    border-top: solid !important;
    border-top-color: var(--el-color-primary) !important;
  }

}

</style>
