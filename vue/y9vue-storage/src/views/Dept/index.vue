<template>
    <y9Card :showHeader="true" :showHeaderSplit="false" :headerPadding="false">
        <template #header>
            <div class="toolbar">
            <div class="toolbar-left">
                <el-upload
                    v-if="parentId !== 'shared'"
                    :show-file-list="false"
                    action=""
                    class="upload-div"
                    multiple
                    v-bind:http-request="uploadFile"
                >
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        class="global-btn-main"
                        type="primary"
                        ><i class="ri-upload-cloud-2-line"></i>{{ $t('上传') }}
                    </el-button>
                </el-upload>
                <el-button-group>
                    <!-- 常规显示的按钮 -->
                        <template v-for="(button, index) in visibleButtons" :key="index">
                            <el-button
                                :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }"
                                v-if="button.condition !== undefined ? button.condition : true"
                                :class="button.class"
                                v-on="button.handler ? { click: button.handler } : {}"
                                :disabled="button.disabled"
                                plain
                            >
                                <i :class="button.icon"></i>{{ $t(button.text) }}
                            </el-button>
                        </template>

                        <!-- 下拉菜单按钮 -->
                        <el-dropdown v-if="hiddenButtons.length > 0">
                            <el-button 
                                :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }"
                                class="global-btn-second"
                                plain
                            >
                                <i class="ri-more-line"></i>{{ $t('更多') }}
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item 
                                        v-for="(button, index) in hiddenButtons" 
                                        :key="'hidden-'+index"
                                        v-on="button.handler ? { click: button.handler } : {}"
                                    ><i :class="button.icon"></i>{{ $t(button.text) }}
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                </el-button-group>
            </div>
            <div class="toolbar-right">
                <el-form :inline="true" @submit.native.prevent>
                    <el-form-item>
                        <el-input
                            v-model="searchKey"
                            :placeholder="$t('输入文件名搜索')"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-select
                            v-model="fileTagKey"
                            multiple
                            filterable
                            collapse-tags
                            collapse-tags-tooltip
                            placeholder="选择标签"
                            style="width: 10vw"
                            @change="tagChange"
                            >
                            <el-option
                                v-for="item in tagOptions"
                                :key="item.id"
                                :label="item.tagName"
                                :value="item.id"
                            />
                            </el-select>
                    </el-form-item>
                </el-form>
                <el-button
                    class="global-btn-second"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }" @click="searchTagTable"
                    ><i class="ri-search-line"></i>{{ $t('查询') }}</el-button>
                <el-button
                    class="global-btn-second"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }" @click="resetSearch"
                    ><i class="ri-reset-left-line"></i>{{ $t('重置') }}</el-button>
            </div>
        </div>
        </template>
        
        <div class="nav">
            <div class="location">
                {{ $t('所在目录') }}：<span @click="backSuperior">{{ backSign }}</span>
                <span
                    v-for="file in recursiveToRootFileNodeList"
                    v-bind:key="file.id"
                    v-bind:title="file.name"
                    @click="subList(file)"
                >
                    {{ $t(file.name) }}
                </span>
            </div>
        </div>
        <el-form ref="fileForm" :model="formData" :rules="rules" class="formClass">
            <el-progress
                v-if="uploadLoading"
                :percentage="percentage"
                :show-text="true"
                :stroke-width="18"
                :text-inside="true"
                class="progress"
                status="success"
                type="line"
            ></el-progress>
            <y9Table
                ref="deptTable"
                v-loading="loading"
                :config="y9TableConfig"
                :element-loading-text="loadingTitle"
                element-loading-background="rgba(0, 0, 0, 0.8)"
                element-loading-spinner="el-icon-loading"
                @on-change="handleSelectionChange"
                @row-click="selectRow"
                @select-all="selectAll"
            >
                <template #name="{ row, column, index }">
                    <el-form-item v-if="editIndex === index" prop="name">
                        <FileNameWithIcon :file-node="row" :opt-type="optSign" />
                        <el-input
                            ref="nameSign"
                            v-model="formData.name"
                            clearable
                            style="width: 20vw; margin-left: 15px"
                            @keyup.enter.native="saveData(fileForm)"
                        />
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-main"
                            style="margin-left: 14px"
                            type="primary"
                            @click="saveData(fileForm)"
                            ><el-tooltip
                                        class="box-item"
                                        effect="light"
                                        :content="$t('保存')"
                                        placement="top-start"
                                    ><i class="ri-check-line"></i></el-tooltip></el-button>
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-second"
                            plain
                            @click="cancalData(fileForm)"
                            >
                            <el-tooltip
                                        class="box-item"
                                        effect="light"
                                        :content="$t('取消')"
                                        placement="top-start"
                                    ><i class="ri-close-line"></i></el-tooltip>
                        </el-button>
                    </el-form-item>
                    <el-row v-else @mouseenter="titleHover(row.id)" @mouseleave="titleLeave(row.id)">
                        <el-col :span="18" class="fileName">
                            <FileNameWithIcon :file-node="row" @fileClick="openFile(row)" @folderClick="subList" />
                        </el-col>
                        <el-col :span="6">
                            <div v-if="optButtonShow == row.id" class="optButtonCss">
                                <template v-if="row.filePassword == '' || row.filePassword == null">
                                    <el-tooltip
                                        :content="$t('重命名')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-edit-2-line" @click="renameListBtn(row)"></i>
                                    </el-tooltip>
                                    <el-tooltip
                                        :content="$t('删除')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-delete-bin-line" @click="deleteOne(row.id)"></i>
                                    </el-tooltip>
                                    <el-tooltip
                                        :content="$t('下载')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-download-2-line" @click="downloadFile(row.id)"></i>
                                    </el-tooltip>
                                    <el-tooltip
                                        :content="$t('移动至')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-login-box-line" @click="move"></i>
                                    </el-tooltip>
                                </template>
                            </div>
                        </el-col>
                    </el-row>
                </template>
                <template #fileTag="{ row, column, index }"> 
                    <div v-if="row.fileType != 0" class="file-tag-container">
                        <template v-if="row.fileTags && row.fileTags.length > 0">
                            <div v-for="tag in row.fileTags" :key="tag.id" class="tag-wrapper">
                                <el-tooltip 
                                    :content="tag.tagType === 'customTag' ? $t('自定义标签') : $t('系统标签')"
                                    placement="left-start"
                                >
                                    <el-tag 
                                        closable
                                        @close="removeTag(row, tag)"
                                        @click="viewTag(row, tag)"
                                        class="tag-item"
                                        :style="{
                                        backgroundColor: tag.tagColor + '33',
                                        color: tag.tagColor
                                        }"
                                    >
                                       <!-- 自定义标签标识 -->
                                       <i v-if="tag.tagType === 'customTag'" class="ri-user-star-line custom-tag-icon"></i>
                                        {{ $t(tag.tagName) }}
                                    </el-tag>
                                 </el-tooltip>
                            </div>
                        </template>
                        <el-dropdown trigger="click" @command="(command) => handleTagMenuClick(command, row)" placement="bottom-start">
                                <i class="ri-add-circle-line add-tag-icon" :title="$t('添加标签')"></i>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="systemTag">
                                        <i class="ri-price-tag-3-line"></i>
                                        {{ $t('系统标签') }}
                                    </el-dropdown-item>
                                    <el-dropdown-item command="customTag">
                                        <i class="ri-edit-line"></i>
                                        {{ $t('自定义标签') }}
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                    <span v-else class="no-tag">---</span>
                </template>
                <template #collect="{ row, column, index }">
                    <i
                        v-if="row.collect"
                        class="ri-star-fill star"
                        style="color: #fdd458 !important"
                        @click="setCollect(row)"
                    ></i>
                    <i v-else class="ri-star-line star" @click="setCollect(row)"></i>
                </template>
                <template #fileSize="{ row, column, index }">
                    {{ row.fileSize ? row.fileSize : '-' }}
                </template>
                <template #fileDate>
                    <span v-if="listType === 'shared'">{{ $t('共享日期') }}</span>
                    <span v-else>{{ $t('修改日期') }}</span>
                </template>
                <template #time="{ row, column, index }">
                    <span v-if="listType === 'shared'">{{ row.createTime }}</span>
                    <span v-else>{{ row.updateTime }}</span>
                </template>
                <template #place="{ row, column, index }">
                    <div v-if="row.filePassword != null && row.filePassword != ''">
                        <el-link v-if="row.parentFileNode" @click="openDecrypt(row)">
                            {{ row.parentFileNode.name }}
                        </el-link>
                        <el-link v-else @click="openDecrypt(row)">{{ $t('全部文件') }}</el-link>
                    </div>
                    <div v-else>
                        <router-link
                            v-if="row.parentFileNode"
                            v-bind:to="{
                                path: '/dept/fileList/all',
                                query: { parentId: row.parentId, listType: listType }
                            }"
                        >
                            {{ row.parentFileNode.name }}
                        </router-link>
                        <router-link v-else to="/dept/fileList/all">{{ $t('全部文件') }}</router-link>
                    </div>
                </template>
            </y9Table>
        </el-form>
        <y9Dialog v-model:config="dialogConfig">
            <div v-if="dialogConfig.type == 'moveNode'" class="tree-div">
                <selectTree :showHeader="showHeader" :treeApiObj="treeApiObj" @onTreeClick="moveNodeData"></selectTree>
            </div>
            <OrgUnitSelector v-if="dialogConfig.type == 'share'" @org-click="selectedNode" />
            <!-- 视频播放器 -->
            <VideoPlayer v-if="dialogConfig.type == 'video'" :poster="poster" :video_url="fileUrl" />
            <!-- 视频播放器 -->
            <!-- txt,java,js,java,vue,css,xml文件预览-->
            <TextViewer v-if="dialogConfig.type == 'txt'" :fileId="fileId" :fileName="fileName" :fileUrl="fileUrl" />
            <!--音频文件预览-->
            <AudioPlayer v-if="dialogConfig.type == 'mp3'" :audioArray="audioArray" />
            <!--直链-->
            <FileLink v-if="dialogConfig.type == 'link'" :fileObject="fileObject" />
            <!--文件夹加密-->
            <FolderPwd
                v-if="dialogConfig.type == 'FolderPwd'"
                :dialogObj="dialogConfig"
                :fileObject="fileObject"
                :reloadTable="loadList"
            />
            <!--文件夹解密-->
            <DecryptPwd
                v-if="dialogConfig.type == 'DecryptPwd'"
                :dialogObj="dialogConfig"
                :fileObject="fileObject"
                :reloadTable="loadList"
                @openFolder="openFolder"
            />
            <Tag v-if="dialogConfig.type == 'Tag'" ref="fileTagRef"/>
            <TagView v-if="dialogConfig.type == 'TagView'" ref="tagViewRef" :currentViewFile="currentViewFile" :currentViewTag="currentViewTag"/>
            <!-- 自定义标签组件 -->
            <CustomTag 
                v-if="dialogConfig.type == 'CustomTag'" 
                ref="customTagRef" 
                :tagData="currentViewTag"
                @success="handleCustomTagSuccess"
            />
        </y9Dialog>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, onMounted, watch, computed, reactive, toRefs, nextTick, inject ,h,resolveComponent} from 'vue';
    import type { FormRules } from 'element-plus';
    import FileApi from '@/api/storage/file';
    import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
    import OrgUnitSelector from '@/components/storage/OrgUnitSelector/index.vue';
    import FileLink from '@/components/file/FileLink.vue';
    import FolderPwd from '@/components/storage/Folder/index.vue';
    import DecryptPwd from '@/components/storage/Folder/decrypt.vue';
    import TagView from '@/components/storage/Tag/tagDetail.vue';
    import CustomTag from '@/components/storage/Tag/addTag.vue';
    import TextViewer from '@/components/file/TextViewer.vue';
    import AudioPlayer from '@/components/file/AudioPlayer.vue';
    import FileNodeShareApi from '@/api/storage/fileNodeShare';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import { useRoute, useRouter } from 'vue-router';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useSettingStore } from '@/store/modules/settingStore';
    import FileTagApi from '@/api/storage/fileTag';
    import axios from 'axios';
    import posterImg from '@/assets/images/bg.jpg';
    import { api as viewerApi } from 'v-viewer';
    import { useI18n } from 'vue-i18n';
    import { $filteredNullObj } from '@/utils/object';

    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const router = useRouter();
    // 获取当前路由
    const currentrRute = useRoute();
    const props = defineProps({
        parentId: {
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
    });
    const storageStore = useStorageStore();
    const rules = reactive<FormRules>({
        name: { required: true, message: t('请输入文件夹名称'), trigger: 'blur' }
    });
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 260 - 15);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 260 - 15;
        })();
    };
    const data = reactive({
        positionId: '',
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
        // 点击更多
        buttonMore: false,
        sharePersons: [],
        showHeader: false,
        optSign: '',
        fileForm: '',
        isEmptyData: false,
        nameSign: '',
        editIndex: '',
        formData: { id: '', parentId: '', name: '' },
        deptTable: '',
        notCurrentSelectedOwner: false,
        recursiveToRootFileNodeList: [],
        orderProp: 'UPDATE_TIME',
        orderAsc: true,
        searchKey: '',
        multipleSelection: [],
        loading: false,
        moveFileIds: [],
        editFileNode: {},
        treeSelectedData: {},
        treeApiObj: {
            //tree接口对象
            topLevel: () => {
                return FileApi.treeFolder('dept');
            },
            childLevel: {
                api: FileApi.treePublicFolderById,
                params: {}
            }
        },
        y9TableConfig: {
            //headerBackground: true,
            border: 0,
            height: tableHeight.value,
            pageConfig: false,
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
                {
                    title: computed(() => t('文件名')),
                    key: 'name',
                    align: 'left',
                    minWidth: '500',
                    sortable: true,
                    slot: 'name'
                },
                { title: computed(() => t('所属标签')), key: "fileTags", align: "left", width: '330', slot: 'fileTag' },
                {
                    title: computed(() => t('收藏')),
                    key: 'collect',
                    align: 'center',
                    width: '80',
                    sortable: true,
                    slot: 'collect'
                },
                { title: computed(() => t('所有者')), key: 'userName', align: 'left', sortable: true, width: '170' },
                { title: computed(() => t('大小')), key: 'fileSize', width: 'auto', sortable: true, slot: 'fileSize' },
                {
                    title: computed(() => t('日期')),
                    key: 'time',
                    width: '200',
                    sortable: true,
                    slot: 'time',
                    headerSlot: 'fileDate'
                }
            ],
            tableData: []
        },
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    if (dialogConfig.value.type == 'share') {
                        if (sharePersons.value.length > 0) {
                            var orgUnitIdArr = sharePersons.value.map((orgUnit) => orgUnit.id);

                            var shareFileNodeIdArr = multipleSelection.value.map((item) => item.id);
                            FileNodeShareApi.share(shareFileNodeIdArr, orgUnitIdArr).then(() => {
                                ElMessage({
                                    type: 'success',
                                    message: t("共享成功，文件会移动至'共享空间'中"),
                                    offset: 65
                                });
                                loadList();
                                resolve();
                            });
                        } else {
                            ElMessage({ type: 'error', message: t('请选择要共享的人员'), offset: 65 });
                            reject();
                            return;
                        }
                    } else if(dialogConfig.value.type == 'moveNode'){
                        if (treeSelectedData.value) {
                            // 判断要移动到的路径是否为当前目录或其子目录
                            let pathValid = true;
                            for (let i = 0; i < moveFileIds.value.length; i++) {
                                if (moveFileIds.value[i] === treeSelectedData.value.id) {
                                    pathValid = false;
                                }
                            }

                            if (pathValid) {
                                FileApi.move(moveFileIds.value, treeSelectedData.value.id).then((res) => {
                                    moveFileIds.value = [];
                                    ElMessage({
                                        type: res.success ? 'success' : 'error',
                                        message: res.msg,
                                        offset: 65
                                    });
                                    loadList();
                                    resolve();
                                });
                            } else {
                                ElMessage({
                                    type: 'error',
                                    message: t('不能将文件夹移动到自身及其子目录下'),
                                    offset: 65
                                });
                                reject();
                                return;
                            }
                        } else {
                            ElMessage({ type: 'error', message: t('请先选择要移动到的目录'), offset: 65 });
                            reject();
                            return;
                        }
                    } else if (dialogConfig.value.type == 'Tag') { 
                        let tagSelect = fileTagRef.value.tagSelect;
                        let tagIds = tagSelect.map((item) => item.id);
                        if (tagIds.length == 0) {
                            ElMessage({ type: 'error', message: t('请选择文件标签'), offset: 65 });
                            reject();
                            return;
                        }
                        console.log("标签选择",tagSelect.map((item) => item.id));
                        console.log('multipleSelection.value', multipleSelection.value);
                        
                        let filesFilter = multipleSelection.value.filter(item => item.fileType !== 0);
                        if (filesFilter.length == 0) {
                            ElMessage({ type: 'error', message: t('请选择要添加标签的文件'), offset: 65 });
                            reject();
                            return;
                        }
                        let fileNodeIds = filesFilter.map((item) => item.id);
                        FileTagApi.addFileTagToFile(fileNodeIds, tagIds,listType.value).then(() => {
                            ElMessage({
                                type: 'success',
                                message: t("添加标签成功"),
                                offset: 65
                            });
                            loadList();
                            resolve();
                        });
                    }else if (dialogConfig.value.type == 'CustomTag') {
                        // 自定义标签保存
                        let formRef = customTagRef.value.formRef;
                        if (!formRef) {
                            resolve();
                            return;
                        }
                        formRef.validate().then((valid) => {
                            if (valid) {
                                let formData = customTagRef.value.formData;
                                formData.tagType = 'customTag';
                                formData.listType = listType.value;
                                let fileId = currentViewFile.value.id;
                                
                                FileTagApi.saveCustomTag($filteredNullObj(formData), fileId,tagOpt.value).then((res) => {
                                    if (res.success) {
                                        if (res.msg && res.msg.indexOf('标签名称已存在') > -1) {
                                            // 标签名称已存在，询问是否使用全局标签
                                            ElMessageBox.confirm(t('该标签已存在，是否直接使用全局标签？'), t('提示'), {
                                                confirmButtonText: t('确定'),
                                                cancelButtonText: t('取消'),
                                                type: 'info'
                                            }).then(() => {
                                                // 用户确认使用全局标签
                                                FileTagApi.simpleFileToTag(fileId, res.data.id,listType.value).then(() => {
                                                    ElMessage({ type: 'success', message: t('标签添加成功'), offset: 65 });
                                                    dialogConfig.value.show = false;
                                                    loadList();
                                                    resolve();
                                                });
                                            }).catch(() => {
                                                // 用户取消，保持弹窗打开让用户修改名称
                                                ElMessage({ type: 'info', message: t('请修改自定义标签名称（避免冲突）！'), offset: 65 });
                                                // 关键：确保弹窗保持打开状态
                                                dialogConfig.value.show = true;
                                                // 停止 loading 状态
                                                dialogConfig.value.onOkLoading = false;
                                                // 不调用 resolve()，让弹窗保持打开
                                            });
                                        } else {
                                            // 保存成功，无冲突
                                            ElMessage({ type: 'success', message: t('自定义标签添加成功'), offset: 65 });
                                            dialogConfig.value.show = false;
                                            loadList();
                                            resolve();
                                        }
                                    } else {
                                        // 其他错误，保持弹窗打开
                                        ElMessage({ type: 'error', message: res.msg || t('保存失败'), offset: 65 });
                                        dialogConfig.value.show = true;
                                        dialogConfig.value.onOkLoading = false;
                                    }
                                }).catch((err) => {
                                    // 接口调用失败，保持弹窗打开
                                    ElMessage({ type: 'error', message: t('保存失败'), offset: 65 });
                                    dialogConfig.value.show = true;
                                    dialogConfig.value.onOkLoading = false;
                                });
                            } else {
                                // 表单验证失败，保持弹窗打开
                                ElMessage({ type: 'warning', message: t('请填写标签必填信息'), offset: 65 });
                                dialogConfig.value.show = true;
                                dialogConfig.value.onOkLoading = false;
                            }
                        }).catch(() => {
                            dialogConfig.value.show = true;
                            dialogConfig.value.onOkLoading = false;
                        });
                    }
                });
            },
            visibleChange: (visible) => {
                if (!visible) {
                    treeSelectedData.value = {};
                }
            }
        },
        maxVisibleButtons: 5,
        fileTagKey: [],
        tagOptions: [],
        fileTagRef: null,
        currentViewFile: {},  // 当前查看的文件
        currentViewTag: {},   // 当前查看的标签
        customTagRef: null,
        tagOpt: '',
        y9UserInfo: {},
    });

    let {
        positionId,
        y9TableConfig,
        loadingTitle,
        fileObject,
        audioArray,
        poster,
        fileId,
        fileName,
        fileUrl,
        uploadLoading,
        percentage,
        fileForm,
        isEmptyData,
        nameSign,
        editIndex,
        formData,
        deptTable,
        notCurrentSelectedOwner,
        recursiveToRootFileNodeList,
        orderProp,
        orderAsc,
        searchKey,
        multipleSelection,
        loading,
        optSign,
        sharePersons,
        optButtonShow,
        moveFileIds,
        editFileNode,
        dialogConfig,
        showHeader,
        treeSelectedData,
        treeApiObj,
        listType,
        buttonMore,
        backSign,
        maxVisibleButtons,
        fileTagKey,
        tagOptions,
        fileTagRef,
        currentViewFile,
        currentViewTag,
        customTagRef,
        tagOpt,
        y9UserInfo,
    } = toRefs(data);

    onMounted(() => {
        positionId.value = storageStore.currentPositionId;
        y9UserInfo.value = y9_storage.getObjectItem('ssoUserInfo');
        loadList();
        
    });

    computed(() => {
        notCurrentSelectedOwner: () => {
            let owner = true;
            multipleSelection.value.forEach((item) => {
                owner = owner && item.owner;
            });
            return !owner;
        };
    });

    watch(
        () => [props.parentId, props.fileNodeType],
        ([pId, fileNodeType]) => {
            if (fileNodeType) {
                loadList();
            }

            if (pId) {
                listType.value = props.parentId;
                if (props.parentId == 'shared') {
                    orderProp.value = 'CREATE_TIME';
                }
                loadList();
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    watch(
        () => searchKey.value,
        (val) => {
            deleteColumn();
            loadList();
        },
        {
            deep: true,
            immediate: true
        }
    );

    const allButtons = computed(() => {
        const buttons = [];
        
        // 文件夹按钮
        if (!props.fileNodeType && props.parentId !== 'shared') {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: createFolder,
                icon: "ri-folder-add-line",
                text: "文件夹",
                disabled: false
            });
        }

        // 批量标签按钮
        if (multipleSelection.value.length) {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: multiTagTable,
                icon: "ri-price-tag-3-line",
                text: "批量标签",
                disabled: false
            });
        }
        
        // 下载按钮
        if (multipleSelection.value.length) {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: download,
                icon: "ri-download-2-line",
                text: "下载",
                disabled: false
            });
        }
        
        // 删除按钮
        if (multipleSelection.value.length) {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: deleteSelect,
                icon: "ri-delete-bin-line",
                text: "删除",
                disabled: notCurrentSelectedOwner.value
            });
        }
        
        // 移动按钮
        if (multipleSelection.value.length) {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: move,
                icon: "ri-login-box-line",
                text: "移动到",
                disabled: notCurrentSelectedOwner.value
            });
        }
        
        // 重命名按钮
        if (multipleSelection.value.length === 1) {
            buttons.push({
                condition: true,
                class: "global-btn-second",
                handler: renameOutBtn,
                icon: "ri-edit-2-line",
                text: "重命名",
                disabled: notCurrentSelectedOwner.value
            });
        }
        
        // 刷新按钮（始终显示）
        buttons.push({
            condition: true,
            class: "global-btn-second",
            handler: loadList,
            icon: "ri-refresh-line",
            text: "刷新",
            disabled: false
        });
        
        return buttons;
    });

    const visibleButtons = computed(() => {
        return allButtons.value.slice(0, data.maxVisibleButtons);
    });

    const hiddenButtons = computed(() => {
        return allButtons.value.slice(data.maxVisibleButtons);
    });

    function moveNodeData(node) {
        treeSelectedData.value = node;
    }

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

    function selectedNode(nodes) {
        sharePersons.value = nodes;
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
                deptTable.value.elTableRef.toggleRowSelection(row);
            }
        } else {
            deptTable.value.elTableRef.clearSelection();
        }
    }

    function selectAll(selection) {
        console.log('multipleSelection.value', multipleSelection.value);
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
    }

    function loadList() {
        loading.value = true;
        if (orderProp.value == 'UPDATE_TIME') {
            orderAsc.value = false;
        }
        deleteColumn();
        if (searchKey.value != '') {
            y9TableConfig.value.columns.push({
                title: computed(() => t('所在目录')),
                key: 'place',
                width: '150',
                slot: 'place'
            });
        }
        FileApi.list(props.parentId, searchKey.value,fileTagKey.value.join(), props.fileNodeType, 'dept', orderProp.value, orderAsc.value).then(
            (res) => {
                loading.value = false;
                y9TableConfig.value.tableData = res.data.subFileNodeList;
                recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
            }
        );
        loadTagList();
    }

    function subList(row) {
        if (row.filePassword != '' && row.filePassword != null) {
            openDecrypt(row);
        } else {
            router.push({ path: '/dept/fileList/all', query: { parentId: row.id } });
        }
        backSign.value = t('返回上一级');
        if (row.id == 'dept') {
            backSign.value = '';
        }
    }

    function openDecrypt(row) {
        fileObject.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '25%',
            title: computed(() => t('文件夹解密')),
            type: 'DecryptPwd',
            showFooter: false
        });
    }

    function setCollect(row) {
        if (row.collect) {
            FileApi.cancelCollect(row.id).then((res) => {
                if (res.success) {
                    loadList();
                }
            });
        } else {
            FileApi.setCollect(row.id).then((res) => {
                if (res.success) {
                    loadList();
                }
            });
        }
    }

    function openFolder(row) {
        router.push({ path: '/dept/fileList/all', query: { parentId: row.id } });
        backSign.value = t('返回上一级');
        if (row.id == 'dept') {
            backSign.value = '';
        }
    }

    async function backSuperior() {
        let res = await FileApi.getNetParentId(props.parentId);
        if (res.data != null) {
            if (res.data.parentId == 'dept') {
                backSign.value = '';
            } else {
                backSign.value = t('返回上一级');
            }
            router.push({ path: '/dept/fileList/all', query: { parentId: res.data.parentId } });
        }
    }

    function openFile(row) {
        let typeName = '',
            isShow = true,
            showMsg = false;
        fileUrl.value = encodeURI(row.fileUrl);
        fileName.value = row.name;
        fileId.value = row.fileStoreId;
        let docType = 'doc,docx,xls,xlsx,ppt,pptx,pdf';
        let txtType = 'txt,js,vue,java,css,scss,xml,ts,html,htm,json';
        if (row.fileType == 1) {
            isShow = false;
            viewerApi({
                images: [row.fileUrl]
            });
        } else if (row.fileType == 2) {
            if (docType.indexOf(row.fileSuffix) != -1) {
                isShow = false;
                showMsg = false;
                window.open(
                    encodeURI(
                        row.previewUrl +
                            '?access_token=' +
                            y9_storage.getObjectItem(settings.siteTokenKey, 'access_token')
                    ),
                    '_blank'
                );
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
            ElMessage({ type: 'info', message: t('该文件不支持预览！请下载到本地查看。'), offset: 65 });
        }
        Object.assign(dialogConfig.value, {
            show: isShow,
            width: '60%',
            title: computed(() => t(row.name)),
            type: typeName,
            showFooter: false
        });
    }

    function download() {
        var IdArr = multipleSelection.value.map((item) => item.id);
        window.open(
            import.meta.env.VUE_APP_CONTEXT +
                'vue/fileNode/downloadFile?ids=' +
                IdArr.join() +
                '&positionId=' +
                storageStore.currentPositionId +
                '&access_token=' +
                y9_storage.getObjectItem(settings.siteTokenKey, 'access_token'),
            '_blank'
        );
    }

    function downloadFile(id) {
        var IdArr = [];
        IdArr.push(id);
        window.open(
            import.meta.env.VUE_APP_CONTEXT +
                'vue/fileNode/downloadFile?ids=' +
                IdArr.join() +
                '&positionId=' +
                storageStore.currentPositionId +
                '&access_token=' +
                y9_storage.getObjectItem(settings.siteTokenKey, 'access_token'),
            '_blank'
        );
    }

    function setPassword(row) {
        fileObject.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '25%',
            title: computed(() => t('文件夹加密')),
            type: 'FolderPwd',
            showFooter: false
        });
    }

    function deleteSelect() {
        var idArr = multipleSelection.value.map((item) => item.id);
        logicDelete(idArr);
    }

    function deleteOne(id) {
        let ids = [];
        ids.push(id);
        logicDelete(ids);
    }

    function logicDelete(ids) {
        ElMessageBox.confirm(t('确认要把所选文件放入回收站吗？删除的文件可通过回收站还原'), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(() => {
                FileApi.delete(ids).then(() => {
                    loadList();
                });
            })
            .catch(() => {
                ElMessage({ type: 'info', message: t('已取消操作'), offset: 65 });
            });
    }

    function uploadFile(params) {
        percentage.value = 0;
        let config = {
            onUploadProgress: (progressEvent) => {
                //progressEvent.loaded:已上传文件大小,progressEvent.total:被上传文件的总大小
                let percent = ((progressEvent.loaded / progressEvent.total) * 100) | 0;
                percentage.value = percent;
            },
            headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: 'Bearer ' + y9_storage.getObjectItem(settings.siteTokenKey, 'access_token'),
                positionId: storageStore.currentPositionId
            }
        };
        uploadLoading.value = true;
        const loading = ElLoading.service({ lock: true, text: t('正在处理中'), background: 'rgba(0, 0, 0, 0.3)' });
        var formData = new FormData();
        formData.append('file', params.file);
        formData.append('parentId', props.parentId == undefined ? 'dept' : props.parentId);
        formData.append('listType', 'dept');
        axios
            .post(import.meta.env.VUE_APP_CONTEXT + 'vue/fileNode/uploadFile', formData, config)
            .then((res) => {
                loading.close();
                uploadLoading.value = false;
                if (res.data.data.success) {
                    loadList();
                }
                //upload.value.clearFiles();
                ElMessage({
                    type: res.data.data.success ? 'success' : 'error',
                    message: res.data.data.msg,
                    offset: 65
                });
            })
            .catch((err) => {
                ElMessage({ type: 'error', message: t('发生异常'), offset: 65 });
            });
    }

    function changeOrder(e, order) {
        if (e.target.tagName === 'INPUT') return;
        if (orderProp.value === order) {
            orderAsc.value = !orderAsc.value;
        } else {
            orderAsc.value = true;
        }
        orderProp.value = order;
        loadList();
    }

    function share() {
        Object.assign(dialogConfig.value, {
            show: true,
            width: '50%',
            title: computed(() => t('文件共享')),
            type: 'share',
            okText: computed(() => t('共享')),
            showFooter: true
        });
    }

    function cancelShare() {
        ElMessageBox.confirm(t('确认取消共享选中文件吗？'), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(() => {
                var IdArr = multipleSelection.value.map((item) => item.id);
                FileNodeShareApi.cancelShare(IdArr).then(() => {
                    loadList();
                });
            })
            .catch(() => {
                ElMessage({ type: 'info', message: t('已取消共享文件'), offset: 65 });
            });
    }

    function createFolder() {
        for (let i = 0; i < y9TableConfig.value.tableData.length; i++) {
            if (y9TableConfig.value.tableData[i].id == '') {
                isEmptyData.value = true;
            }
        }
        if (!isEmptyData.value) {
            editIndex.value = 0;
            y9TableConfig.value.tableData.unshift({ id: '', typeName: '' });
            formData.value.id = '';
            formData.value.name = '';
            formData.value.type = '';
            nextTick(() => {
                nameSign.value.focus();
                nameSign.value.select();
            });
        }
        optSign.value = 'add';
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
        form.validate((valid) => {
            if (valid) {
                if (optSign.value == 'add') {
                    formData.value.id = props.id;
                    formData.value.parentId = props.parentId;
                    formData.value.listType = 'dept';
                    formData.value.userId = storageStore.currentPositionId;
                    formData.value.userName = storageStore.currentPositionName;
                }

                FileApi.saveFileNode(formData.value).then((res) => {
                    let msg = t('文件夹创建成功');
                    if (optSign.value == 'rename') {
                        msg = t('文件夹重命名成功');
                        if (formData.value.fileType != 0) {
                            msg = t('文件重命名成功');
                        }
                    }
                    ElMessage({ type: res.success ? 'success' : 'error', message: msg, offset: 65 });
                    editIndex.value = '';
                    isEmptyData.value = false;
                    loadList();
                });
            }
        });
    }

    function move() {
        moveFileIds.value = multipleSelection.value.map((item) => item.id);
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('文件移动')),
            okText: computed(() => t('移动')),
            type: 'moveNode',
            showFooter: true
        });
    }

    function renameOutBtn() {
        editFileNode.value = multipleSelection.value[0];
        rename(editFileNode.value);
    }

    function renameListBtn(row) {
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
        optSign.value = 'rename';
        nextTick(() => {
            nameSign.value.focus();
            nameSign.value.select();
        });
    }

    function handlerClickDropdownItem(value, id) {
        buttonMore.value = value;
        if (!value) {
            id = '';
        }
        titleLeave(id);
    }

    async function loadTagList() { 
        let res = await FileTagApi.getAllTag();
        if (res.data != null) {
            tagOptions.value = res.data;
        }
    }

    function tagChange(){
        console.log('tagChange',fileTagKey.value);
    }

    function searchTagTable(){
        loadList();
    }

    function resetSearch(){
        searchKey.value = '';
        fileTagKey.value = [];
        loadList();
    }

    // 处理标签菜单点击
    function handleTagMenuClick(command: string, row?: any) {
        currentViewFile.value = row;
        if (command === 'systemTag') {
            // 打开管理标签弹窗（现有 Tag 组件）
            openTagTable(currentViewFile.value);
        } else if (command === 'customTag') {
            // 打开自定义标签弹窗（可以新建组件或复用）
            tagOpt.value = 'add';
            currentViewTag.value = {};
            addCustomTagTable(row);
        }
    }

    // 删除标签
    async function removeTag(row, tag) {
        ElMessageBox.confirm(
            t('确认要删除该标签吗？'),
            t('提示'),
            {
                confirmButtonText: t('确定'),
                cancelButtonText: t('取消'),
                type: 'warning'
            }
        ).then(async () => {
            const res = await FileTagApi.removeFileTag(row.id, tag.id,listType.value);
            if (res.success) {
                ElMessage({ type: 'success', message: t('删除标签成功'), offset: 65 });
                loadList();
            } else {
                ElMessage({ type: 'error', message: res.msg, offset: 65 });
            }
        }).catch(() => {});
    }

    // 查看标签详情（带编辑功能）
    function viewTag(row, tag) {
        currentViewFile.value = row;
        currentViewTag.value = tag;
        if(tag.tagType == 'systemTag'){
            openTagDetail();
        } else {
            // 处理非系统标签的逻辑
            tagOpt.value = 'edit';
            if(tag.createId == y9UserInfo.value.personId){
                editCustomTagTable(row);
            }else{
                 openTagDetail();
            }
        }
    }

    function openTagDetail() { 
        Object.assign(dialogConfig.value, {
            show: true,
            width: '20%',
            title: computed(() => t('标签详情')),
            type: 'TagView',
            showFooter: false
        });
    }

    function multiTagTable(){
        let count = multipleSelection.value.length;
        ElMessageBox.confirm(
            t('是否对选中的' + count + '个文件执行该操作？'),
            t('提示'),
            {
                confirmButtonText: t('确定'),
                cancelButtonText: t('取消'),
                type: 'warning'
            }
        ).then(async () => {
            Object.assign(dialogConfig.value, {
                show: true,
                width: '25%',
                title: computed(() => t('文件标签')),
                type: 'Tag',
                okText: computed(() => t('设置标签')),
                showFooter: true
            });
        }).catch(() => {

        });
        
    }
    
    function openTagTable(row) {
        Object.assign(dialogConfig.value, {
            show: true,
            width: '25%',
            title: computed(() => t('文件标签')),
            type: 'Tag',
            okText: computed(() => t('设置标签')),
            showFooter: true
        });
    }

    // 打开自定义标签弹窗
    function addCustomTagTable(row) {
        currentViewFile.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('添加自定义标签')),
            type: 'CustomTag',
            okText: computed(() => t('确定')),
            cancelText: computed(() => t('取消')),
            showFooter: true
        });
    }

    // 打开自定义标签弹窗
    function editCustomTagTable(row) {
        currentViewFile.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('编辑自定义标签')),
            type: 'CustomTag',
            okText: computed(() => t('确定')),
            cancelText: computed(() => t('取消')),
            showFooter: true
        });
    }

    function handleCustomTagSuccess() {
        dialogConfig.value.show = false;
        loadList();
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
        padding: 0px 15px !important;
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

.file-tag-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    min-height: 32px;
    
    .tag-wrapper {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        
        .tag-item {
            cursor: pointer;

            .custom-tag-icon {
                font-size: 14px;
                animation: twinkle 2s infinite;
            }

            :deep(.el-tag__close) {
                &:hover {
                    color: var(--el-color-white) !important;
                    background-color: var(--el-color-danger);
                }
            }
            
        }
        
        .tag-actions {
            display: inline-flex;
            align-items: center;
            gap: 2px;
            
            .tag-action-icon {
                cursor: pointer;
                font-size: 14px;
                color: var(--el-color-info);
                padding: 2px;
                border-radius: 2px;
                
                &:hover {
                    color: var(--el-color-primary);
                    background-color: var(--el-color-primary-light-9);
                }
            }
        }
    }
    
    .no-tag {
        color: var(--el-color-info-light-5);
        font-size: 12px;
    }
    
    .add-tag-icon {
        cursor: pointer;
        color: var(--el-color-primary);
        font-size: 18px;
        padding: 2px;
        border-radius: 50%;
        
        &:hover {
            color: var(--el-color-primary-light-3);
            background-color: var(--el-color-primary-light-9);
        }
    }

    .add-tag-icon {
        cursor: pointer;
        color: var(--el-color-primary);
        font-size: 18px;
        padding: 2px;
        border-radius: 50%;
        
        &:hover {
            color: var(--el-color-primary-light-3);
            background-color: var(--el-color-primary-light-9);
        }
    }

    
    // 下拉菜单样式
    :deep(.el-dropdown) {
        display: inline-flex;
        align-items: center;
        
        .el-dropdown-menu__item {
            display: flex;
            align-items: center;
            gap: 8px;
            
            i {
                font-size: 16px;
            }
        }
    }
}

// 标签详情弹窗样式
.tag-detail-messagebox {
    width: 400px !important;
    
    .el-message-box__header {
        padding-bottom: 15px;
    }
    
    .el-message-box__content {
        padding: 10px 0;
    }
    
    .tag-detail-container {
        .tag-detail-row {
            display: flex;
            align-items: center;
            padding: 15px 0;
            border-bottom: 1px solid var(--el-border-color-lighter);
            
            &:last-child {
                border-bottom: none;
            }
            
            .tag-detail-label {
                width: 100px;
                font-weight: 600;
                color: var(--el-text-color-regular);
                flex-shrink: 0;
                font-size: 14px;
            }
            
            .tag-detail-value {
                flex: 1;
                color: var(--el-text-color-primary);
                word-break: break-all;
                font-size: 14px;
            }
            
            .tag-name-input {
                flex: 1;
                
                :deep(.el-input__wrapper) {
                    padding: 5px 10px;
                    box-shadow: 0 0 0 1px var(--el-border-color) inset;
                    
                    &:hover {
                        box-shadow: 0 0 0 1px var(--el-color-primary-light-5) inset;
                    }
                    
                    &.is-focus {
                        box-shadow: 0 0 0 1px var(--el-color-primary) inset;
                    }
                }
                
                :deep(.el-input__inner) {
                    font-size: 14px;
                }
            }
        }
    }
    
    .el-message-box__btns {
        padding-top: 20px;
        
        .el-button {
            min-width: 80px;
        }
    }
}
:deep(.el-tag .el-tag__close:hover) {
    color: var(--el-color-white) !important;
    background-color: var(--el-tag-hover-color);
}   

</style>
