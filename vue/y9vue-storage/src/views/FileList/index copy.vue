<template>
  <el-card class="box-card">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-upload v-if="parentId !== 'shared'"
            action=""
            class="upload-div"
            :show-file-list="false"
            multiple
            v-bind:http-request="uploadFile">
          <el-button class="global-btn-main" type="primary"><i class="ri-upload-cloud-2-line"></i>上传</el-button>
        </el-upload>

        <el-button v-if="!fileNodeType && parentId !== 'shared'" class="global-btn-second" v-on:click="createFolder" plain>
          <i class="ri-folder-add-line"></i>新建文件夹
        </el-button>
        <el-button v-if="multipleSelection.length" class="global-btn-second" v-on:click="download" plain>
          <i class="ri-download-2-line"></i>下载
        </el-button>
        <el-button v-if="multipleSelection.length" :disabled="notCurrentSelectedOwner" class="global-btn-second" v-on:click="deleteSelect" plain>
          <i class="ri-delete-bin-line"></i> 删除
        </el-button>
        <el-button v-if="multipleSelection.length" :disabled="notCurrentSelectedOwner" class="global-btn-second" v-on:click="move" plain>
          <i class="ri-login-box-line"></i> 移动到
        </el-button>
        <el-button v-if="multipleSelection.length === 1" :disabled="notCurrentSelectedOwner" class="global-btn-second" v-on:click="share" plain>
          <i class="ri-share-fill"></i>共享
        </el-button>
        <el-button v-if="multipleSelection.length === 1" :disabled="notCurrentSelectedOwner" v-on:click="renameOutBtn" class="global-btn-second" plain>
          <i class="ri-edit-2-line"></i>重命名
        </el-button>
        <el-button class="global-btn-second" @click="loadList" plain><i class="ri-refresh-line"></i>刷新</el-button>
      </div>
      <div class="toolbar-right">
        <el-form :inline="true">
            <el-form-item label="文件名称">
              <el-input v-model="searchKey" placeholder="输入文件名搜索" @clear="clearSearch" @change="toSearchView" class="search-input global-btn-second" clearable>
                <template #append>
                  <el-button slot="append" @click="toSearchView"><i class="ri-search-line global-btn-second"></i></el-button>
                </template>
              </el-input>
              </el-form-item>
              <el-form-item>
                <el-dropdown>
                  <span class="el-dropdown-link">
                    <el-button class="global-btn-second"><i class="ri-arrow-up-down-line"></i>排序</el-button>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>
                        <el-radio v-model="orderProp" label="FILE_NAME" @click.native="changeOrder($event, 'FILE_NAME')">文件名
                        </el-radio>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-radio v-model="orderProp" label="FILE_SIZE" @click.native="changeOrder($event, 'FILE_SIZE')">文件大小
                        </el-radio>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-radio v-if="listType !== 'shared'" v-model="orderProp" label="UPDATE_TIME" @click.native="changeOrder($event, 'UPDATE_TIME')">
                          修改时间
                        </el-radio>
                        <el-radio v-else v-model="orderProp" label="CREATE_TIME" @click.native="changeOrder($event, 'CREATE_TIME')">
                          共享时间
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
        <span v-if="capacityShow">存储空间：{{remainingLength}}/{{ capacitySize }}</span>
        所在目录：<span @click="backSuperior">{{ backSign }}</span>
        <span v-for="file in recursiveToRootFileNodeList" v-bind:key="file.id" v-bind:title="file.name" @click="subList(file.id)">
          {{ file.name }}
        </span>
      </div>
    </div>
    <el-form class="formClass" ref="fileForm" :model="formData" :rules="rules">
      <el-progress v-if="uploadLoading" type="line" :percentage="percentage" :stroke-width="18" class="progress" status="success"
    :text-inside="true" :show-text="true"></el-progress>
      <el-table ref="multipleTable" v-loading="loading" :data="tableData" tooltip-effect="dark"
          style="width: 100%" :height="tableHeight" empty-text="暂无文件"  @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" align="left"></el-table-column>
        <!-- <el-table-column type="index" label="序号" width="60"> </el-table-column> -->
        <el-table-column prop="name" label="文件名" width="auto">
          <template #default="name">
            <el-form-item prop="name" v-if="editIndex === name.$index">
              <FileNameWithIcon :file-node="name.row" :opt-type="optSign"/>
              <el-input style="width:300px;margin-left:15px;" ref="nameSign" v-model="formData.name" @keyup.enter.native="saveData(fileForm)" clearable/>
              <el-button style="margin-left: 14px;" type="primary" @click="saveData(fileForm)" plain><i class="ri-check-line"></i></el-button>
              <el-button type="primary" @click="cancalData(fileForm)" plain><i class="ri-close-line"></i></el-button>
            </el-form-item>
            <el-row v-else @mouseenter="titleHover(name.row.id)" @mouseleave="titleLeave(name.row.id)">
            <el-col :span="18">
              <FileNameWithIcon :file-node="name.row"  @folderClick="subList" @fileClick="openFile(name.row)"/>
            </el-col>
            <el-col :span="6">
              <div class="optButtonCss" v-if="optButtonShow==name.row.id">
                <el-tooltip class="box-item" effect="light" content="重命名" placement="top-start">
                  <i class="ri-edit-2-line" @click="renameListBtn(name.row)"></i>
                </el-tooltip>
                <el-tooltip class="box-item" effect="light" content="删除" placement="top-start">
                  <i class="ri-delete-bin-line" @click="deleteOne(name.row.id)"></i>
                </el-tooltip>
                <el-tooltip class="box-item" effect="light" content="下载" placement="top-start">
                  <i class="ri-download-2-line" @click="downloadFile(name.row.id)"></i>
                </el-tooltip>
                <el-tooltip class="box-item" effect="light" content="移动至" placement="top-start">
                  <i class="ri-login-box-line" @click="move"></i>
                </el-tooltip>
                <el-tooltip class="box-item" effect="light" content="共享" placement="top-start">
                  <i class="ri-share-line" @click="fileShare(name.row)"></i>
                </el-tooltip>
                <el-tooltip v-if="name.row.fileType!=0" class="box-item" effect="light" content="生成直链" placement="top-start">
                  <i class="ri-links-fill" @click="createLink(name.row)"></i>
                </el-tooltip>
              </div>
            </el-col>
          </el-row>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="所有者" width="170"></el-table-column>
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="fileSize">
            {{ fileSize.row.fileSize ? fileSize.row.fileSize : '-' }}
          </template>
        </el-table-column>
        <el-table-column  width="170">
          <template #header>
            <span v-if="listType==='shared'">共享日期</span>
            <span v-else>修改日期</span>
          </template>
          <template #default="time">
            <span v-if="listType === 'shared'">{{time.row.createTime}}</span>
            <span v-else>{{time.row.updateTime}}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="searchName" label="所在目录" width="150">
          <template #default="scope">
            <router-link v-if="scope.row.parentFileNode" v-bind:to="'/my/fileList/all/' + scope.row.parentId">
              {{ scope.row.parentFileNode.name }}
            </router-link>
            <router-link v-else to="/my/fileList/all">全部文件</router-link>
          </template>
        </el-table-column>
      </el-table>
    </el-form>
    <y9Dialog v-model:config="dialogConfig">
      <div class="tree-div" v-if="dialogConfig.type=='moveNode'">
        <selectTree :treeApiObj="treeApiObj" @onTreeClick="moveNodeData" :showHeader="showHeader"></selectTree>
      </div>
      <OrgUnitSelector v-if="dialogConfig.type=='share'" @org-click="selectedNode"/>
       <!-- 视频播放器 -->
        <VideoPlayer v-if="dialogConfig.type=='video'" :video_url="fileUrl" :poster="poster"/>
       <!-- 视频播放器 -->
       <!-- txt,java,js,java,vue,css,xml文件预览-->
       <TextViewer v-if="dialogConfig.type=='txt'" :fileId="fileId" :fileUrl="fileUrl" :fileName="fileName"/>
       <!--音频文件预览-->
        <AudioPlayer v-if="dialogConfig.type=='mp3'" :audioArray="audioArray"/>
        <!--直链-->
        <FileLink v-if="dialogConfig.type=='link'" :fileObject="fileObject" />
      </y9Dialog>
  </el-card>
</template>

<script lang="ts" setup>
import { ref, defineProps, onMounted, watch,computed,reactive, toRefs,nextTick } from 'vue';
import type{ ElMessage, ElMessageBox,UploadInstance } from 'element-plus';
import FileApi from '@/api/storage/file';
import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
import OrgUnitSelector from '@/components/storage/OrgUnitSelector/index.vue';
import FileLink from '@/components/file/FileLink.vue';
import TextViewer from '@/components/file/TextViewer.vue';
import AudioPlayer from '@/components/file/AudioPlayer.vue';
import FileNodeShareApi from '@/api/storage/fileNodeShare';
import y9_storage from "@/utils/storage";
import settings from "@/settings";
import { useRoute,useRouter } from 'vue-router'
import { useStorageStore } from "@/store/modules/storageStore";
import CapacityApi from '@/api/storage/capacity';
import axios from 'axios';
import posterImg from '@/assets/images/bg.jpg';
import { api as viewerApi } from "v-viewer";

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
  name:{ required: true,message: '请输入文件夹名称', trigger: 'blur' },
    });
const data = reactive({
      fileObject:{},
      audioArray:[],
      fileId:'',
      fileName:'',
      fileUrl:'',
      poster:'',
      uploadLoading:false,
      percentage:0,
      backSign:'',
      listType:'',
      optButtonShow:'',
      // 点击更多
      buttonMore: false,
      sharePersons:[],
      showHeader:false,
      optSign:'',
      fileForm:'',
      isEmptyData:false,
      nameSign:'',
      editIndex:'',
      formData:{id:'',parentId:'',name:''},
      multipleTable:'',
      notCurrentSelectedOwner:false,
      tableData: [],
      recursiveToRootFileNodeList: [],
      orderProp: 'UPDATE_TIME',
      orderAsc: true,
      searchKey: '',
      multipleSelection: [],
      loading: false,
      tableHeight: window.innerHeight - 295,
      tableScreenHeight: window.innerHeight,
      shareVisible: false,
      moveFileNodeVisible: false,
      moveFileIds: [],
      editFileNode: {},
      treeSelectedData:{},
      treeApiObj:{//tree接口对象
        topLevel:()=>{
          return FileApi.treeFolder("my")
        },
        childLevel:{
          api:FileApi.treeFolderById,
          params:{}
        },
      },
      //弹窗配置
      dialogConfig: {
        show: false,
        title: "",
        onOkLoading: true,
        onOk: (newConfig) => {
          return new Promise(async (resolve, reject) => {
            if(dialogConfig.value.type=='share'){
              if(sharePersons.value.length>0){
                var orgUnitIdArr = sharePersons.value.map(orgUnit => orgUnit.id);
                console.log(sharePersons.value);
                console.log(multipleSelection.value);
                
                var shareFileNodeIdArr = multipleSelection.value.map(item => item.id);
                console.log('props.listType',props.listType);
                
                FileNodeShareApi.share(shareFileNodeIdArr, orgUnitIdArr).then(() => {
                  ElMessage({ type: "success", message: '共享成功，文件会移动至"共享文件"中',offset:65});
                  loadList();
                  resolve()
                });
              }else{
                  ElMessage({ type: "error", message: "请选择要共享的人员",offset:65});
                  reject();
                  return;
              }
            }else{
                if(treeSelectedData.value){
                  // 判断要移动到的路径是否为当前目录或其子目录
                  let pathValid = true;
                  for (let i = 0; i < moveFileIds.value.length; i++) {
                      if (moveFileIds.value[i] === treeSelectedData.value.id ) {
                        pathValid = false;
                      }
                  }
                  
                  if (pathValid) {
                    FileApi.move(moveFileIds.value, treeSelectedData.value.id).then(res => {
                      moveFileIds.value = [];
                      ElMessage({ type: res.success ? 'success' : 'error', message: res.msg,offset:65});
                      loadList();
                      resolve()
                    });
                  } else {
                    ElMessage({ type: "error", message: "不能将文件夹移动到自身及其子目录下",offset:65});
                    reject();
                    return;
                  }
                } else {
                  ElMessage({ type: "error", message: "请先选择要移动到的目录",offset:65});
                  reject();
                  return;
                }
             }
          })
        },
        visibleChange:(visible) => {
          if(!visible){
            treeSelectedData.value = {};
          }
        }
      },
    });

    let {
      fileObject,audioArray,poster,fileId,fileName,fileUrl,uploadLoading,percentage,fileForm, isEmptyData, nameSign,  editIndex, formData,  multipleTable, notCurrentSelectedOwner,
      tableData, recursiveToRootFileNodeList, orderProp, orderAsc, searchKey, multipleSelection, loading,
      tableHeight, tableScreenHeight, shareVisible, moveFileNodeVisible,optSign,sharePersons,optButtonShow,
      moveFileIds, editFileNode, dialogConfig,showHeader,treeSelectedData,treeApiObj,listType,buttonMore,backSign
    } = toRefs(data);

  onMounted(() => {
    loadList();
    getCapacityLength();
    window.onresize = () => {
      return (() => {
        window.screenHeight = window.innerHeight;
        tableScreenHeight.value = window.screenHeight;
      })();
    };
  })
 
  computed(() => {
    notCurrentSelectedOwner: () => {
      let owner = true;
      multipleSelection.value.forEach(item => {
        owner = owner && item.owner;
      });
      return !owner;
    }
  })

  watch(() => [props.parentId,props.fileNodeType,props.searchName],
    ([pId,fileNodeType,searchName])=>{
      if(fileNodeType){
        loadList();
      }
      if(searchName!=undefined){
        searchKey.value = searchName;
        loadList();
      }
      if(pId){
        listType.value = props.parentId;
        if(props.parentId=='shared'){
          orderProp.value = 'CREATE_TIME';
        }
        loadList();
      }
    },{
      deep: true,
      immediate: true,
    });


  const capacityShow = ref(false);
  const capacitySize = ref('');
  const remainingLength = ref('');
  async function getCapacityLength(){
    let res = await CapacityApi.getCapacitySize();
    if(res.data != null){
      if(props.listType=='my'){
        capacityShow.value = true;
      }
      capacitySize.value = res.data.capacitySize;
      remainingLength.value = res.data.remainingLength;
    }
    
  }

  function moveNodeData(node){
    treeSelectedData.value = node;
  }

  function titleHover(id){
    optButtonShow.value = id;
  }
  function titleLeave(id){
    if(buttonMore.value){
      optButtonShow.value = id;
    }else {
      optButtonShow.value = '';
    }
  }

  function clearSearch(){
    loadList();
  }

  function selectedNode(nodes){
      sharePersons.value = nodes;
  }
   
  function handleSelectionChange(data){
    multipleSelection.value = data;
  }

  function loadList() {
      loading.value = true;
      if(orderProp.value == 'UPDATE_TIME'){
        orderAsc.value = false;
      }
      
      FileApi.list(props.parentId, props.searchName, props.fileNodeType,props.listType, orderProp.value, orderAsc.value).then(res => {
        loading.value = false;
        tableData.value = res.data.subFileNodeList;
        recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
      });
  }
  
  function subList(id) {
    console.log('props.listType',props.listType);
    
    if(props.listType=='my'){
      router.push({path:'/my/fileList/all',query:{parentId:id,listType:props.listType}});
    }else{
      router.push({path:'/share/fileList/all/shared',query:{parentId:id,listType:props.listType}});
    }
    
    backSign.value = "返回上一级";
    if(id=='my'){
      backSign.value = '';
    }
  }

  async function backSuperior(){
      let res = await FileApi.getNetParentId(props.parentId);
      if(res.data!=null){
        if(res.data.parentId=='my'){
          backSign.value = '';
        }else{
          backSign.value = '返回上一级';
        }
        if(props.listType=='my'){
          router.push({path:'/my/fileList/all',query:{parentId:res.data.parentId,listType:props.listType}});
        }else{
          router.push({path:'/share/fileList/all/shared',query:{parentId:res.data.parentId,listType:props.listType}});
        }
      }
  }
   
  function openFile(row) {
    let typeName = '',isShow = true,showMsg = false;
    fileUrl.value = encodeURI(row.fileUrl);
    fileName.value = row.name;
    fileId.value = row.fileStoreId;
    let docType = 'doc,docx,xls,xlsx,ppt,pptx,wps,pdf';
    let txtType = 'txt,js,vue,java,css,scss,xml,ts,html,htm,json';
    if(row.fileType==1){
      isShow = false;
      viewerApi({
        images: [row.fileUrl],
      })
    }else if(row.fileType==2){
      if(docType.indexOf(row.fileSuffix)!=-1){
        isShow = false;
        window.open(encodeURI(row.previewUrl+"?access_token="+y9_storage.getObjectItem(settings.siteTokenKey,"access_token")), '_blank');
      }
      if(txtType.indexOf(row.fileSuffix)!=-1){
        typeName = 'txt';
      }else{
        isShow = false;
        showMsg = true;
      }
    }else if(row.fileType==3){
      poster.value = posterImg;
      typeName = 'video';
    }else if(row.fileType==4){
      audioArray.value.push({name:fileName.value,url:fileUrl.value});
      typeName = 'mp3';
    }else{
      isShow = false;
      showMsg = true;
    }
    if(showMsg){
      ElMessage({ type: "info", message: "该文件不支持预览！请下载到本地查看。",offset:65});
    }
    Object.assign(dialogConfig.value,{
          show:isShow,
          width:'60%',
          title:row.name,
          type:typeName,
          showFooter:false
      });
  }

  function createLink(row){
    fileObject.value = row;
    Object.assign(dialogConfig.value,{
          show:true,
          width:'20%',
          title:'生成直链',
          type:'link',
          showFooter:false
      });
  }

  function download() {
      var IdArr = multipleSelection.value.map(item => item.id);
      window.open(import.meta.env.VUE_APP_CONTEXT + "vue/fileNode/downloadFile?ids=" + IdArr.join()+"&positionId="+storageStore.currentPositionId+"&access_token="+y9_storage.getObjectItem(settings.siteTokenKey,"access_token"), "_blank");
  }

  function downloadFile(id){
    var IdArr = [];
    IdArr.push(id);
    window.open(import.meta.env.VUE_APP_CONTEXT + "vue/fileNode/downloadFile?ids=" + IdArr.join()+"&access_token="+y9_storage.getObjectItem(settings.siteTokenKey,"access_token"), "_blank");
  }

  function deleteSelect(){
    var idArr = multipleSelection.value.map(item => item.id);
    logicDelete(idArr);
  }

  function deleteOne(id){
    let ids = [];
    ids.push(id);
    logicDelete(ids);
  }

  function logicDelete(ids) {
    ElMessageBox.confirm('确认要把所选文件放入回收站吗？删除的文件可通过回收站还原', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        FileApi.delete(ids).then(() => {
          loadList();
        });
      }).catch(() => {
        ElMessage({ type: "info", message: "已取消操作",offset:65});
      });
    }

  function uploadFile(params) {
      percentage.value = 0;
      let config = {
        onUploadProgress: progressEvent => {
          //progressEvent.loaded:已上传文件大小,progressEvent.total:被上传文件的总大小
          let percent = (progressEvent.loaded / progressEvent.total * 100) | 0;
          percentage.value = percent;
        },
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': 'Bearer ' + y9_storage.getObjectItem(settings.siteTokenKey,"access_token"),
          'positionId': storageStore.currentPositionId
        }
      };
      uploadLoading.value = true;
      const loading = ElLoading.service({ lock: true, text: '正在处理中', background: 'rgba(0, 0, 0, 0.3)' });
      var formData = new FormData();
      formData.append("file", params.file);
      formData.append("parentId", props.parentId==undefined ? 'my' : props.parentId);
      formData.append("listType", "my");
      axios.post(import.meta.env.VUE_APP_CONTEXT + "vue/fileNode/uploadFile", formData, config).then((res) => {
        loading.close();
        uploadLoading.value = false;
        if (res.data.data.success) {
          loadList();
        }
        ElMessage({ type: res.data.data.success ? 'success' : 'error', message: res.data.data.msg , offset: 65 });
      }).catch((err) => {
        ElMessage({ type: 'error', message: '发生异常', offset: 65 });
      });
  }

  function toSearchView() {
    let query = {
      parentId:props.parentId,
      type:props.fileNodeType,
      key: searchKey.value,
    };
    if(props.listType=='my'){
      router.push({path:'/my/fileList/search',query:query});
    }else{
      router.push({path:'/share/fileList/search',query:query});
    }
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

  function share() {
    Object.assign(dialogConfig.value,{
          show:true,
          width:'50%',
          title:'文件分享',
          type:'share',
          okText:'共享',
          showFooter:true
        });
  }

  function fileShare(row) {
    multipleSelection.value = [];
    multipleSelection.value.push(row);
    
    Object.assign(dialogConfig.value,{
          show:true,
          width:'50%',
          title:'文件分享',
          type:'share',
          okText:'共享',
          showFooter:true
        });
  }
  
  function cancelShare() {
    ElMessageBox.confirm('确认取消共享选中文件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        var IdArr = multipleSelection.value.map(item => item.id);
        FileNodeShareApi.cancelShare(IdArr).then(() => {
          loadList();
        });
      }).catch(() => {
        ElMessage({ type: "info", message: "已取消共享文件",offset:65});
      });
  }

  function createFolder() {
    for (let i = 0; i < tableData.value.length; i++) {
      if(tableData.value[i].id==''){
          isEmptyData.value = true;
      }
    }
    if(!isEmptyData.value){
      editIndex.value = tableData.value.length;
      tableData.value.push({id:'',typeName:'',});
      formData.value.id = '';
      formData.value.name = '';
      formData.value.type = '';
      nextTick(()=>{
				nameSign.value.focus()
        nameSign.value.select()
			})
    } 
    optSign.value = 'add';
  }

  function cancalData (fileForm){
    editIndex.value = '';
    formData.value.name = '';
    formData.value.type = '';
    fileForm.resetFields();
    for (let i = 0; i < tableData.value.length; i++) {
      if(tableData.value[i].id==''){
          tableData.value.splice(i,1);
      }
    }
    isEmptyData.value = false;
 }

 function saveData(form){
  if(!form) return;
  form.validate(valid => {
    if (valid) {
      if(optSign.value=='add'){
        formData.value.id = props.id;
        formData.value.parentId = props.parentId;
        formData.value.listType = 'my';
      }
      
      FileApi.saveFileNode(formData.value).then(res => {
        let msg = "文件夹创建成功";
        if(optSign.value=='rename'){
          msg = '文件夹重命名成功';
        }
      	ElMessage({ type: res.success?'success':'error', message: msg, offset: 65 });
        editIndex.value = '';
        isEmptyData.value = false;
        loadList();
      });
    }
  });
 }

  function move() {
      moveFileIds.value = multipleSelection.value.map(item => item.id);
      Object.assign(dialogConfig.value,{
          show:true,
          width:'30%',
          title:'文件移动',
          okText:'移动',
          type:'moveNode',
          showFooter:true
        });
  }
   
  function renameOutBtn(){
    editFileNode.value = multipleSelection.value[0];
    rename(editFileNode.value);
  }

  function renameListBtn(row){
    rename(row);
  }

  function rename(row) {
      let index = 0;
      for (let i = 0; i < tableData.value.length; i++) {
        if(tableData.value[i].id== row.id){
            index = i;
        }
      }
      editIndex.value = index;
      formData.value.id = row.id;
      formData.value.name = row.name;
      formData.value.parentId = row.parentId;
      optSign.value = 'rename';
      nextTick(()=>{
				nameSign.value.focus()
        nameSign.value.select()
			})
  }

  function handlerClickDropdownItem(value, id) {
    buttonMore.value = value;
    if(!value){
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

  :deep(.el-table){
    .el-table__body {
      .el-table__row:hover{
        td {
          border-top: 1px solid var(--el-color-primary);
          border-bottom-color:  var(--el-color-primary);
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
    }
  } 
}

.optButtonCss i{
  color: var(--el-color-primary);
  font-size: 20px;
  margin-left: 15px;
}

:deep(.el-form-item) {
    display: inline-flex;
    vertical-align: middle;
    margin-right: 10px;
    margin-bottom: 0px;
}

:deep(.aplayer-fixed){
  position: relative !important;
  padding: 0px 360px;
}

:deep(.aplayer-body){
  position: relative !important;
}

:deep(.el-table__cell .cell){
  padding-left: 0px;
}

:deep(.el-table-column--selection .cell){
  padding-left: 12px;
  padding-right: 12px;
}

:deep(.el-table__inner-wrapper::before) {
    height: 0px;
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
  :deep(.el-button){
    height: 30px;
    line-height: 0;
    min-width:0px;
    box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
    padding: 8px 15px;
  }
}

.search-input {
  width: 250px;
  margin-right: 10px;
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
  font-size: 14px;
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
  margin-right: 5px;
}

.location span:after {
  content: ' >';
}

.location span:last-child {
  color: black;
}

</style>
