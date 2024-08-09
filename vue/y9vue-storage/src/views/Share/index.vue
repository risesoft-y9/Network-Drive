<template>
  <y9Card :showHeader="false">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button :size="fontSizeObj.buttonSize"
                   :style="{ fontSize: fontSizeObj.baseFontSize }" v-if="multipleSelection.length" class="global-btn-second" v-on:click="download" plain>
          <i class="ri-download-2-line"></i>{{ $t('下载') }}
        </el-button>
        <el-button :size="fontSizeObj.buttonSize"
                   :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-second" @click="loadList" plain><i class="ri-refresh-line"></i>{{ $t('刷新') }}</el-button>
      </div>
      <div class="toolbar-right">
        <el-form :inline="true" @submit.native.prevent>
          <el-form-item :label="$t('文件名称')">
            <el-input v-model="searchKey" :placeholder="$t('输入文件名搜索')" class="search-input global-btn-second" clearable>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-button :size="fontSizeObj.buttonSize"
                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-second"><i class="ri-arrow-up-down-line"></i>{{ $t('排序') }}</el-button>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-radio v-model="orderProp" label="FILE_NAME" @click.native="changeOrder($event, 'FILE_NAME')">{{ $t('文件名') }}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-radio v-model="orderProp" label="FILE_SIZE" @click.native="changeOrder($event, 'FILE_SIZE')">{{ $t('文件大小') }}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-radio v-model="orderProp" label="CREATE_TIME"
                      @click.native="changeOrder($event, 'CREATE_TIME')">
                      {{ $t('共享时间') }}
                    </el-radio>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="nav">
      <div class="location">
        <label class="backOne" v-if="backSign" @click="backSuperior">{{ backSign }}<label>|</label></label>
        <span v-for="file in recursiveToRootFileNodeList" v-bind:key="file.id" v-bind:title="file.name"
          @click="subList(file)">
          {{ $t(file.name) }}
        </span>
      </div>
    </div>
    <el-form class="formClass" ref="fileForm" :model="formData" :rules="rules">
      <el-progress v-if="uploadLoading" type="line" :percentage="percentage" :stroke-width="18" class="progress"
        status="success" :text-inside="true" :show-text="true"></el-progress>
      <y9Table ref="multipleTable" :config="y9TableConfig" v-loading="loading" :element-loading-text="loadingTitle"
        element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)"
        @on-change="handleSelectionChange" @row-click="selectRow" @select-all="selectAll">
        <template #name="{ row, column, index }">
          <el-form-item prop="name" v-if="editIndex === index">
            <FileNameWithIcon :file-node="row" :opt-type="optSign" />
            <el-input style="width:500px;margin-left:15px;" ref="nameSign" v-model="formData.name"
              @keyup.enter.native="saveData(fileForm)" clearable />
            <el-button :size="fontSizeObj.buttonSize"
                       :style="{ fontSize: fontSizeObj.baseFontSize }" style="margin-left: 14px;" type="primary" @click="saveData(fileForm)" class="global-btn-main"><i
                class="ri-check-line"></i></el-button>
            <el-button :size="fontSizeObj.buttonSize"
                       :style="{ fontSize: fontSizeObj.baseFontSize }" @click="cancalData(fileForm)" class="global-btn-second" plain><i
                class="ri-close-line"></i></el-button>
          </el-form-item>
          <el-row v-else @mouseenter="titleHover(row.id)" @mouseleave="titleLeave(row.id)">
            <el-col :span="21" class="fileName">
              <FileNameWithIcon :file-node="row" @folderClick="subList" @fileClick="openFile(row)" />
            </el-col>
            <el-col :span="3">
              <div class="optButtonCss" v-if="optButtonShow == row.id">
                <template v-if="row.filePassword == '' || row.filePassword == null">
                  <el-tooltip class="box-item" effect="light" :content="$t('重命名')" placement="top-start">
                    <i class="ri-edit-2-line" @click="renameListBtn(row)"></i>
                  </el-tooltip>
                  <el-tooltip class="box-item" effect="light" :content="$t('删除')" placement="top-start">
                    <i class="ri-delete-bin-line" @click="deleteOne(row)"></i>
                  </el-tooltip>
                  <el-tooltip class="box-item" effect="light" :content="$t('下载')" placement="top-start">
                    <i class="ri-download-2-line" @click="downloadFile(row.id)"></i>
                  </el-tooltip>
                </template>
              </div>
            </el-col>
          </el-row>
        </template>
        <template #collect="{ row, column, index }">
          <i v-if="row.collect" class="ri-star-fill star" style="color: #fdd458 !important;" @click="setCollect(row)"></i>
          <i v-else class="ri-star-line star" @click="setCollect(row)"></i>
        </template>
        <template #fileSize="{ row, column, index }">
          {{ row.fileSize ? row.fileSize : '-' }}
        </template>
        <template #place="{ row, column, index }">
          <div v-if="row.filePassword != null && row.filePassword != ''">
            <el-link v-if="row.parentFileNode" @click="openDecrypt(row)">
              {{ row.parentFileNode.name }}
            </el-link>
            <el-link v-else @click="openDecrypt(row)">{{ $t('全部文件') }}</el-link>
          </div>
          <div v-else>
            <router-link v-if="row.parentFileNode"
              v-bind:to="{ path: '/my/fileList/all', query: { parentId: row.parentId, listType: listType } }">
              {{ row.parentFileNode.name }}
            </router-link>
            <router-link v-else to="/my/fileList/all">{{ $t('全部文件') }}</router-link>
          </div>
        </template>
      </y9Table>
    </el-form>
    <y9Dialog v-model:config="dialogConfig">
      <!-- 视频播放器 -->
      <VideoPlayer v-if="dialogConfig.type == 'video'" :video_url="fileUrl" :poster="poster" />
      <!-- 视频播放器 -->
      <!-- txt,java,js,java,vue,css,xml文件预览-->
      <TextViewer v-if="dialogConfig.type == 'txt'" :fileId="fileId" :fileUrl="fileUrl" :fileName="fileName" />
      <!--音频文件预览-->
      <AudioPlayer v-if="dialogConfig.type == 'mp3'" :audioArray="audioArray" />
      <!--直链-->
      <FileLink v-if="dialogConfig.type == 'link'" :fileObject="fileObject" />
      <!--文件夹加密-->
      <FolderPwd v-if="dialogConfig.type == 'FolderPwd'" :reloadTable="loadList" :dialogObj="dialogConfig"
        :fileObject="fileObject" />
      <!--文件夹解密-->
      <DecryptPwd v-if="dialogConfig.type == 'DecryptPwd'" @openFolder="openFolder" :reloadTable="loadList"
        :dialogObj="dialogConfig" :fileObject="fileObject" />
    </y9Dialog>
  </y9Card>
</template>

<script lang="ts" setup>
import { ref, defineProps, onMounted, watch, computed, reactive, toRefs, nextTick } from 'vue';
import type { ElMessage, ElMessageBox, UploadInstance } from 'element-plus';
import FileApi from '@/api/storage/file';
import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
import FileLink from '@/components/file/FileLink.vue';
import TextViewer from '@/components/file/TextViewer.vue';
import AudioPlayer from '@/components/file/AudioPlayer.vue';
import FileNodeShareApi from '@/api/storage/fileNodeShare';
import FolderPwd from '@/components/storage/Folder/index.vue';
import DecryptPwd from '@/components/storage/Folder/decrypt.vue';
import y9_storage from "@/utils/storage";
import settings from "@/settings";
import { useRoute, useRouter } from 'vue-router'
import { useStorageStore } from "@/store/modules/storageStore";
import { useSettingStore } from '@/store/modules/settingStore';
import axios from 'axios';
import posterImg from '@/assets/images/bg.jpg';
import { api as viewerApi } from "v-viewer";
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo')||{};
const router = useRouter()
// 获取当前路由
const currentrRute = useRoute()
const props = defineProps({
  parentId: {
    require: false,
    type: String
  },
  listType: {
    require: false,
    type: String
  },
  fileNodeType: {
    require: false,
    type: String,
    default: ''
  },
  searchName: {
    require: false,
    type: String
  }
})
const storageStore = useStorageStore();
const upload = ref<UploadInstance>();
const rules = reactive<FormRules>({
  name: { required: true, message: t('请输入文件夹名称'), trigger: 'blur' },
});
//调整表格高度适应屏幕
const tableHeight = ref(useSettingStore().getWindowHeight - 260 - 34);

window.onresize = () => {
  return (() => {
    tableHeight.value = useSettingStore().getWindowHeight - 260 - 34;
  })();
};
const data = reactive({
  userInfo:{},
  loadingTitle: '正在加载中......',
  fileObject: {},
  audioArray: [],
  fileId: '',
  fileName: '',
  fileUrl: '',
  poster: '',
  uploadLoading: false,
  percentage: 0,
  backSign: '',
  listType: '',
  optButtonShow: '',
  buttonMore: false,
  sharePersons: [],
  showHeader: false,
  optSign: '',
  fileForm: '',
  isEmptyData: false,
  nameSign: '',
  editIndex: '',
  formData: { id: '', parentId: '', name: '' },
  multipleTable: '',
  recursiveToRootFileNodeList: [],
  orderProp: 'UPDATE_TIME',
  orderAsc: true,
  searchKey: '',
  multipleSelection: [],
  loading: false,
  moveFileIds: [],
  editFileNode: {},
  treeSelectedData: {},
  y9TableConfig: {
    //headerBackground: true,
    border: 0,
    height: tableHeight.value,
    pageConfig: false,
    columns: [
      { type: "selection", width: '55', selectable:(row) => {
								if(!row.filePassword){
									return true
								}
							}},
      { title: computed(() => t("文件名")), key: "name", align: "left", width: 'auto', slot: 'name' },
      { title: computed(() => t("收藏")), key: "collect", align: "center", width: '50', slot: 'collect' },
      { title: computed(() => t("所有者")), key: "userName", align: "left", width: '170' },
      { title: computed(() => t("大小")), key: "fileSize", width: '120', slot: 'fileSize' },
      { title: computed(() => t("共享日期")), key: "createTime", width: '170', },
    ],
    tableData: []
  },
  dialogConfig: {
    show: false,
    title: "",
    onOkLoading: true,
    onOk: (newConfig) => {
      return new Promise(async (resolve, reject) => {
      })
    },
    visibleChange: (visible) => {
      if (!visible) {
      }
    }
  },
});

let {
  userInfo,y9TableConfig, loadingTitle, fileObject, audioArray, poster, fileId, fileName, fileUrl, uploadLoading, percentage,
  fileForm, isEmptyData, nameSign, editIndex, formData, multipleTable,
  recursiveToRootFileNodeList, orderProp, orderAsc, searchKey, multipleSelection, loading,
  optSign, sharePersons, optButtonShow,
  moveFileIds, editFileNode, dialogConfig, showHeader, treeSelectedData, treeApiObj, listType, buttonMore, backSign
} = toRefs(data);

onMounted(() => {
  userInfo.value =JSON.parse(sessionStorage.getItem('ssoUserInfo'));
  loadList();
})

watch(() => props.parentId,
  (pId) => {
    if (pId) {
      listType.value = props.parentId;
      orderProp.value = 'CREATE_TIME';
      loadList();
    }
  }, {
  deep: true,
  immediate: true,
});

watch(() => searchKey.value,
  (val) => {
    deleteColumn();
    loadList();
  }, {
  deep: true,
  immediate: true,
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

function clearSearch() {
  deleteColumn();
  loadList();
}

function deleteColumn() {
  y9TableConfig.value.columns.forEach((item, index) => {
    if (item.key == 'place') {
      y9TableConfig.value.columns.splice(index, 1);
    }
  });
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
  console.log('multipleSelection.value',multipleSelection.value);
}

function handleSelectionChange(id, data) {
    multipleSelection.value = data;
}

function loadList() {
  loading.value = true;
  deleteColumn();
  if (searchKey.value != '') {
    y9TableConfig.value.columns.push({ title: computed(() => t("所在目录")), key: "place", width: '150', slot: 'place' });
  }
  FileApi.list(props.parentId, searchKey.value, props.fileNodeType, props.listType, orderProp.value, orderAsc.value).then(res => {
    loading.value = false;
    y9TableConfig.value.tableData = res.data.subFileNodeList;
    recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
  });
}

function subList(row) {
  if (row.filePassword != '' && row.filePassword != null) {
    openDecrypt(row);
  } else {
      router.push({ path: '/share/fileList/all/shared', query: { parentId: row.id, listType: props.listType } });
  }

  backSign.value = t("返回上一级");
  if (row.id == 'my') {
    backSign.value = '';
  }
}

function openDecrypt(row) {
  fileObject.value = row;
  Object.assign(dialogConfig.value, {
    show: true,
    width: '17%',
    title: computed(() => t('文件夹解密')),
    type: 'DecryptPwd',
    showFooter: false
  });
}

function setCollect(row){
  if(row.collect){
    FileApi.cancelCollect(row.id).then(res => {
      if(res.success){
        loadList();
      }
    });
  }else{
    FileApi.setCollect(row.id).then(res => {
      if(res.success){
        loadList();
      }
    });
  }
}

function openFolder(row) {
  if (props.listType == 'my') {
    router.push({ path: '/my/fileList/all', query: { parentId: row.id, listType: props.listType } });
  } else {
    router.push({ path: '/share/fileList/all/shared', query: { parentId: row.id, listType: props.listType } });
  }
  backSign.value = t("返回上一级");
  if (row.id == 'my') {
    backSign.value = '';
  }
}

async function backSuperior() {
  let res = await FileApi.getNetParentId(props.parentId);
  if (res.data != null) {
    if (res.data.parentId == 'shared') {
      backSign.value = '';
    } else {
      backSign.value = t('返回上一级');
    }
    if (props.listType == 'my') {
      router.push({ path: '/my/fileList/all', query: { parentId: res.data.parentId, listType: props.listType } });
    } else {
      router.push({ path: '/share/fileList/all/shared', query: { parentId: res.data.parentId, listType: props.listType } });
    }
  }
}

function openFile(row) {
  let typeName = '', isShow = true, showMsg = false;
  fileUrl.value = encodeURI(row.fileUrl);
  fileName.value = row.name;
  fileId.value = row.fileStoreId;
  let docType = 'doc,docx,xls,xlsx,ppt,pptx,pdf';
  let txtType = 'txt,js,vue,java,css,scss,xml,ts,html,htm,json';
  if (row.fileType == 1) {
    isShow = false;
    viewerApi({
      images: [row.fileUrl],
    })
  } else if (row.fileType == 2) {
    if (docType.indexOf(row.fileSuffix) != -1) {
      isShow = false;
      showMsg = false;
      window.open(encodeURI(row.previewUrl + "?access_token=" + y9_storage.getObjectItem(settings.siteTokenKey, "access_token")), '_blank');
    } else if (txtType.indexOf(row.fileSuffix) != -1) {
      typeName = 'txt';
    } else {
      isShow = false;
      showMsg = true;
    }
  } else if (row.fileType == 3) {
    poster.value = posterImg;
    typeName = 'video';
  } else if (row.fileType == 4) {
    audioArray.value.push({ name: fileName.value, url: fileUrl.value });
    typeName = 'mp3';
  } else {
    isShow = false;
    showMsg = true;
  }
  if (showMsg) {
    ElMessage({ type: "info", message: t("该文件不支持预览！请下载到本地查看。"), offset: 65 });
  }
  Object.assign(dialogConfig.value, {
    show: isShow,
    width: '60%',
    title: computed(() => t(row.name)),
    type: computed(() => t(typeName)),
    showFooter: false
  });
}

function download() {
  var IdArr = multipleSelection.value.map(item => item.id);
  window.open(import.meta.env.VUE_APP_CONTEXT + "vue/fileNode/downloadFile?ids=" + IdArr.join() + "&positionId=" + storageStore.currentPositionId + "&access_token=" + y9_storage.getObjectItem(settings.siteTokenKey, "access_token"), "_blank");
}

function downloadFile(id) {
  var IdArr = [];
  IdArr.push(id);
  window.open(import.meta.env.VUE_APP_CONTEXT + "vue/fileNode/downloadFile?ids=" + IdArr.join() + "&access_token=" + y9_storage.getObjectItem(settings.siteTokenKey, "access_token"), "_blank");
}

function deleteSelect() {
  var idArr = multipleSelection.value.map(item => item.id);
  logicDelete(idArr);
}

function deleteOne(row) {
  if(!row.owner){
    ElMessage({ type: "error", message: t("只能删除所有者为自己的文件"), offset: 65 });
    return;
  }
  let ids = [];
  ids.push(row.id);
  logicDelete(ids);
}

function logicDelete(ids) {
  ElMessageBox.confirm(t('确认要把所选文件放入回收站吗？删除的文件可通过回收站还原'), t('提示'), {
    confirmButtonText: t('确定'),
    cancelButtonText: t('取消'),
    type: 'info'
  }).then(() => {
    FileApi.delete(ids).then(() => {
      loadList();
    });
  }).catch(() => {
    ElMessage({ type: "info", message: t("已取消操作"), offset: 65 });
  });
}

function changeOrder(e, order) {
  if (e.target.tagName === "INPUT") return;
  if (orderProp.value === order) {
    orderAsc.value = !orderAsc.value;
  } else {
    orderAsc.value = true;
  }
  orderProp.value = order;
  loadList();
}

function cancalData(fileForm) {
  editIndex.value = '';
  formData.value.name = '';
  formData.value.type = '';
  fileForm.resetFields();
  for (let i = 0; i < y9TableConfig.value.tableData.length; i++) {
    if (y9TableConfig.value.tableData[i].id == '') {
      y9TableConfig.value.tableData.splice(i, 1);
    }
  }
  isEmptyData.value = false;
}

function saveData(form) {
  if (!form) return;
  form.validate(valid => {
    if (valid) {
      if (optSign.value == 'add') {
        formData.value.id = props.id;
        formData.value.parentId = props.parentId;
        formData.value.listType = 'my';
      }

      FileApi.saveFileNode(formData.value).then(res => {
        let msg = "文件夹创建成功";
        if (optSign.value == 'rename') {
          msg = '文件夹重命名成功';
          if (formData.value.fileType != 0) {
            msg = '文件重命名成功';
          }
        }
        ElMessage({ type: res.success ? 'success' : 'error', message: t(msg), offset: 65 });
        editIndex.value = '';
        isEmptyData.value = false;
        loadList();
      });
    }
  });
}

function renameOutBtn() {
  editFileNode.value = multipleSelection.value[0];
  rename(editFileNode.value);
}

function renameListBtn(row) {
  if(!row.owner){
    ElMessage({ type: "error", message: t("只能重命名所有者为自己的文件"), offset: 65 });
    return;
  }
  rename(row);
}

function rename(row) {
  let index = 0;
  for (let i = 0; i < y9TableConfig.value.tableData.length; i++) {
    if (y9TableConfig.value.tableData[i].id == row.id) {
      index = i;
    }
  }
  editIndex.value = index;
  formData.value.id = row.id;
  formData.value.name = row.name;
  formData.value.parentId = row.parentId;
  formData.value.fileType = row.fileType;
  optSign.value = 'rename';
  nextTick(() => {
    nameSign.value.focus()
    nameSign.value.select()
  })
}

function handlerClickDropdownItem(value, id) {
  buttonMore.value = value;
  if (!value) {
    id = '';
  }
  titleLeave(id);
}

</script>

<style lang="scss" scoped>
@import "@/theme/global.scss";

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
          border-top: 1px solid var(--el-color-primary);
          border-bottom-color: var(--el-color-primary);
          border-left: 0px;
          border-right: 0px;
          background-color: var(--el-color-primary-light-9);
        }

        // td:nth-child(1) {
        //   border-left: 0px solid var(--el-color-primary);
        // }
        // td:last-child{
        //   border-right: 0px solid var(--el-color-primary);
        // }
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
  margin-left: 15px;
}

:deep(.y9-dialog-content) {
  padding: 15px !important;
}

:deep(.y9-dialog-header-title){
    font-size: v-bind('fontSizeObj.mediumFontSize') !important;
}

:deep(.el-form-item) {
  display: inline-flex;
  vertical-align: middle;
  margin-right: 10px;
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

.star{
  font-size:22px;
  color: #d6dde9 !important;
}

.tree-div {
  //width: calc(100% - 20px);
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
  /* display: inline-block; */
  float: right;

  :deep(.el-button) {
    height: 30px;
    line-height: 0;
    min-width: 0px;
    box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
    padding: 8px 15px;
  }
  :deep(.el-form-item__label) {
    font-size: v-bind('fontSizeObj.baseFontSize');
  }
}

.search-input {
  width: 250px;
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
  padding: 15px 0 11px 0;
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
}

.location span {
  color: var(--el-color-primary);
  cursor: pointer;
  margin: 0 5px;
  //font-size: 16px;
}

.location span:after {
  content: ' >';
}

.location span:last-child {
  color: black;
}

.location .backOne {
  margin: 0 0 0 10px;
  color: var(--el-color-primary);
}
 .backOne label {
  margin: 0 10px;
 }
</style>
