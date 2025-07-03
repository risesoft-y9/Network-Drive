<template>
    <y9Card :showHeader="false">
        <div class="toolbar">
            <div class="toolbar-left">
                <el-upload
                    v-if="roleType === 'manage'"
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

                <el-button
                    v-if="!fileNodeType && roleType === 'manage'"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="createFolder"
                >
                    <i class="ri-folder-add-line"></i>{{ $t('新建文件夹') }}
                </el-button>
                <el-button
                    v-if="multipleSelection.length"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="download"
                >
                    <i class="ri-download-2-line"></i>{{ $t('下载') }}
                </el-button>
                <el-button
                    v-if="multipleSelection.length && roleType === 'manage'"
                    :disabled="notCurrentSelectedOwner"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="deleteSelect"
                >
                    <i class="ri-delete-bin-line"></i> {{ $t('删除') }}
                </el-button>
                <el-button
                    v-if="multipleSelection.length && roleType === 'manage'"
                    :disabled="notCurrentSelectedOwner"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="move"
                >
                    <i class="ri-login-box-line"></i> {{ $t('移动到') }}
                </el-button>
                <el-button
                    v-if="
                        multipleSelection.length === 1 && roleType === 'manage' && multipleSelection[0].fileType != '0'
                    "
                    :disabled="notCurrentSelectedOwner"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="publicTo"
                >
                    <i class="ri-share-fill"></i>{{ $t('公开至') }}
                </el-button>
                <el-button
                    v-if="multipleSelection.length === 1 && roleType === 'manage'"
                    :disabled="notCurrentSelectedOwner"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="renameOutBtn"
                >
                    <i class="ri-edit-2-line"></i>{{ $t('重命名') }}
                </el-button>
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    @click="refresh"
                    ><i class="ri-refresh-line"></i>{{ $t('刷新') }}
                </el-button>
            </div>
            <div class="toolbar-right">
                <el-form :inline="true">
                    <el-form-item :label="$t('文件名称')">
                        <el-input
                            v-model="searchKey"
                            :placeholder="$t('输入文件名搜索')"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item :label="$t('创建时间')">
                        <el-date-picker
                            v-model="selectedDate"
                            :end-placeholder="$t('结束时间')"
                            :range-separator="$t('至')"
                            :shortcuts="shortcuts"
                            :start-placeholder="$t('开始时间')"
                            :teleported="false"
                            format="YYYY-MM-DD"
                            type="daterange"
                            value-format="YYYY-MM-DD"
                            @change="selectdDate()"
                        ></el-date-picker>
                    </el-form-item>
                    <el-form-item>
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-second"
                            @click="toSearchView"
                            ><i class="ri-search-line"></i>{{ $t('查询') }}
                        </el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-dropdown>
                            <span class="el-dropdown-link">
                                <el-button
                                    :size="fontSizeObj.buttonSize"
                                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                                    class="global-btn-second"
                                    ><i class="ri-arrow-up-down-line"></i>{{ $t('排序') }}</el-button
                                >
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item>
                                        <el-radio
                                            v-model="orderProp"
                                            label="FILE_NAME"
                                            @click.native="changeOrder($event, 'FILE_NAME')"
                                            >{{ $t('文件名') }}
                                        </el-radio>
                                    </el-dropdown-item>
                                    <el-dropdown-item>
                                        <el-radio
                                            v-model="orderProp"
                                            label="FILE_SIZE"
                                            @click.native="changeOrder($event, 'FILE_SIZE')"
                                            >{{ $t('文件大小') }}
                                        </el-radio>
                                    </el-dropdown-item>
                                    <el-dropdown-item>
                                        <el-radio
                                            v-model="orderProp"
                                            label="CREATE_TIME"
                                            @click.native="changeOrder($event, 'CREATE_TIME')"
                                            >{{ $t('创建时间') }}
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
                ref="multipleTable"
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
                            style="width: 500px; margin-left: 15px"
                            @keyup.enter.native="saveData(fileForm)"
                        />
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-main"
                            style="margin-left: 14px"
                            type="primary"
                            @click="saveData(fileForm)"
                            ><i class="ri-check-line"></i
                        ></el-button>
                        <el-button
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            class="global-btn-second"
                            plain
                            @click="cancalData(fileForm)"
                            ><i class="ri-close-line"></i
                        ></el-button>
                    </el-form-item>
                    <el-row v-else @mouseenter="titleHover(row.id)" @mouseleave="titleLeave(row.id)">
                        <el-col :span="19" class="fileName">
                            <FileNameWithIcon :file-node="row" @fileClick="openFile(row)" @folderClick="subList" />
                        </el-col>
                        <el-col :span="5">
                            <div v-if="roleType == 'manage' && optButtonShow == row.id" class="optButtonCss">
                                <template v-if="row.filePassword == '' || row.filePassword == null">
                                    <el-tooltip
                                        v-if="row.fileType != 0"
                                        :content="$t('公开记录')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-team-line" @click="openPublicRecord(row.id)"></i>
                                    </el-tooltip>
                                    <el-tooltip
                                        v-if="row.fileType != 0"
                                        :content="$t('下载记录')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                    >
                                        <i class="ri-download-2-line" @click="openDownloadRecord(row.id)"></i>
                                    </el-tooltip>
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
                                    <el-tooltip
                                        v-if="row.fileType != 0"
                                        :content="$t('公开至')"
                                        class="box-item"
                                        effect="light"
                                        placement="top-start"
                                        @click="publicTo"
                                    >
                                        <i class="ri-share-line" @click="publicTo"></i>
                                    </el-tooltip>
                                </template>
                                <el-tooltip
                                    v-if="row.fileType == 0 && y9UserInfo.personId == row.userId"
                                    :content="$t('加密')"
                                    class="box-item"
                                    effect="light"
                                    placement="top-start"
                                >
                                    <i class="ri-lock-2-line" @click="setPassword(row)"></i>
                                </el-tooltip>
                            </div>
                        </el-col>
                    </el-row>
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
                <template #place="{ row, column, index }">
                    <div v-if="roleType == 'manage'">
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
                                    path: '/public/manage/index',
                                    query: { parentId: row.parentId, roleType: roleType }
                                }"
                            >
                                {{ row.parentFileNode.name }}
                            </router-link>
                            <router-link v-else to="/public/manage/index">{{ $t('全部文件') }}</router-link>
                        </div>
                    </div>
                    <div v-else>
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
                                    path: '/public/fileList/all',
                                    query: { parentId: row.parentId, roleType: roleType }
                                }"
                            >
                                {{ row.parentFileNode.name }}
                            </router-link>
                            <router-link v-else to="/public/fileList/all">{{ $t('全部文件') }}</router-link>
                        </div>
                    </div>
                </template>
            </y9Table>
        </el-form>
        <y9Dialog v-model:config="dialogConfig">
            <div v-if="dialogConfig.type == 'moveNode'" class="tree-div">
                <selectTree :showHeader="showHeader" :treeApiObj="treeApiObj" @onTreeClick="moveNodeData"></selectTree>
            </div>
            <OrgUnitSelector v-if="dialogConfig.type == 'public'" @org-click="selectedNode" />
            <Record v-if="dialogConfig.type == 'record'" :fileId="fileId" :recordType="recordType" />
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
        </y9Dialog>
    </y9Card>
</template>

<script lang="ts" setup>
    import { ref, defineProps, onMounted, watch, computed, reactive, toRefs, nextTick } from 'vue';
    import type { ElMessage, ElMessageBox } from 'element-plus';
    import FileApi from '@/api/storage/file';
    import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
    import OrgUnitSelector from '@/components/storage/OrgUnitSelector/index.vue';
    import Record from '@/views/Public/record.vue';
    import FileLink from '@/components/file/FileLink.vue';
    import FolderPwd from '@/components/storage/Folder/index.vue';
    import DecryptPwd from '@/components/storage/Folder/decrypt.vue';
    import TextViewer from '@/components/file/TextViewer.vue';
    import AudioPlayer from '@/components/file/AudioPlayer.vue';
    import FileNodeShareApi from '@/api/storage/fileNodeShare';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import { useRoute, useRouter } from 'vue-router';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useSettingStore } from '@/store/modules/settingStore';
    import axios from 'axios';
    import posterImg from '@/assets/images/bg.jpg';
    import { api as viewerApi } from 'v-viewer';
    import { useI18n } from 'vue-i18n';

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
        roleType: {
            require: false,
            type: String
        },
        searchName: {
            require: false,
            type: String
        },
        startDate: {
            require: false,
            type: String
        },
        endDate: {
            require: false,
            type: String
        }
    });
    const storageStore = useStorageStore();
    const rules = reactive<FormRules>({
        name: { required: true, message: t('请输入文件夹名称'), trigger: 'blur' }
    });
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 260 - 25);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 260 - 25;
        })();
    };
    const data = reactive({
        y9UserInfo: {},
        loadingTitle: t('正在加载中......'),
        fileObject: {},
        audioArray: [],
        fileId: '',
        fileName: '',
        fileUrl: '',
        poster: '',
        uploadLoading: false,
        percentage: 0,
        backSign: '',
        recordType: '',
        optButtonShow: '',
        buttonMore: false,
        startTime: '',
        endTime: '',
        selectedDate: '',
        listType: '',
        sharePersons: [],
        showHeader: false,
        optSign: '',
        fileForm: '',
        isEmptyData: false,
        nameSign: '',
        editIndex: '',
        formData: { id: '', parentId: '', name: '' },
        multipleTable: '',
        notCurrentSelectedOwner: false,
        recursiveToRootFileNodeList: [],
        orderProp: 'CREATE_TIME',
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
                return FileApi.treeFolder('public');
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
                { title: computed(() => t('文件名')), key: 'name', align: 'left', width: '900',sortable: true, slot: 'name' },
                { title: computed(() => t('收藏')), key: 'collect', align: 'center', width: '70',sortable: true, slot: 'collect' },
                { title: computed(() => t('所有者')), key: 'userName', align: 'left', sortable: true,width: '170' },
                { title: computed(() => t('大小')), key: 'fileSize', width: 'auto', sortable: true,slot: 'fileSize' },
                { title: computed(() => t('创建日期')), key: 'createTime',sortable: true, width: '170' }
            ],
            tableData: []
        },
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    if (dialogConfig.value.type == 'public') {
                        if (sharePersons.value.length > 0) {
                            var orgUnitIdArr = sharePersons.value.map((orgUnit) => orgUnit.id);
                            var shareFileNodeIdArr = multipleSelection.value.map((item) => item.id);
                            FileNodeShareApi.publicTo(shareFileNodeIdArr, orgUnitIdArr).then(() => {
                                ElMessage({ type: 'success', message: t('公开文件成功'), offset: 65 });
                                loadList();
                                resolve();
                            });
                        } else {
                            ElMessage({ type: 'error', message: t('请选择要共享的人员'), offset: 65 });
                            reject();
                            return;
                        }
                    } else {
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
                    }
                });
            },
            visibleChange: (visible) => {
                if (!visible) {
                    treeSelectedData.value = {};
                }
            }
        }
    });

    let {
        y9UserInfo,
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
        multipleTable,
        notCurrentSelectedOwner,
        buttonMore,
        recursiveToRootFileNodeList,
        orderProp,
        orderAsc,
        searchKey,
        multipleSelection,
        loading,
        optSign,
        sharePersons,
        startTime,
        endTime,
        recordType,
        optButtonShow,
        moveFileIds,
        editFileNode,
        dialogConfig,
        showHeader,
        treeSelectedData,
        treeApiObj,
        listType,
        selectedDate,
        backSign
    } = toRefs(data);

    onMounted(() => {
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
        () => [props.parentId, props.fileNodeType, props.roleType],
        ([pId, fileNodeType, roleType]) => {
            if (fileNodeType || roleType || pId) {
                loadList();
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    watch(
        () => [props.startDate, props.endDate],
        ([startDate, endDate]) => {
            if (startDate != undefined && endDate != undefined) {
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

    const selectdDate = () => {
        startTime.value = selectedDate.value[0];
        endTime.value = selectedDate.value[1];
        console.log('time', startTime.value, endTime.value);
    };

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

    function deleteColumn() {
        y9TableConfig.value.columns.forEach((item, index) => {
            if (item.key == 'place') {
                y9TableConfig.value.columns.splice(index, 1);
            }
        });
    }

    function toggleSelection(rows) {
        if (rows) {
            rows.forEach((row) => {
                multipleTable.value.toggleRowSelection(row);
            });
        } else {
            multipleTable.value.clearSelection();
        }
    }

    function selectedNode(nodes) {
        sharePersons.value = nodes;
    }

    function refresh() {
        selectedDate.value = '';
        searchKey.value = '';
        loadList();
    }

    function clearSearch() {
        loadList();
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
        console.log('multipleSelection.value', multipleSelection.value);
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
    }

    function loadList() {
        loading.value = true;
        if (orderProp.value == 'CREATE_TIME') {
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
        if (props.roleType == 'manage') {
            FileApi.manageList(
                props.parentId,
                searchKey.value,
                props.fileNodeType,
                startTime.value,
                endTime.value,
                'public',
                orderProp.value,
                orderAsc.value
            ).then((res) => {
                loading.value = false;
                y9TableConfig.value.tableData = res.data.subFileNodeList;
                recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
            });
        } else {
            FileApi.publicList(
                props.parentId,
                searchKey.value,
                props.fileNodeType,
                startTime.value,
                endTime.value,
                'public',
                orderProp.value,
                orderAsc.value
            ).then((res) => {
                loading.value = false;
                y9TableConfig.value.tableData = res.data.subFileNodeList;
                recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
            });
        }
    }

    function subList(row) {
        if (row.filePassword != '' && row.filePassword != null) {
            fileObject.value = row;
            Object.assign(dialogConfig.value, {
                show: true,
                width: '25%',
                title: computed(() => t('文件夹解密')),
                type: 'DecryptPwd',
                showFooter: false
            });
        } else {
            if (props.roleType == 'manage') {
                router.push({ path: '/manage/index', query: { parentId: row.id, roleType: props.roleType } });
            } else {
                router.push({ path: '/public/fileList/all', query: { parentId: row.id, roleType: props.roleType } });
            }
        }
        backSign.value = t('返回上一级');
        if (row.id == 'public') {
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
        if (props.roleType == 'manage') {
            router.push({ path: '/manage/index', query: { parentId: row.id, roleType: props.roleType } });
        } else {
            router.push({ path: '/public/fileList/all', query: { parentId: row.id, roleType: props.roleType } });
        }
        backSign.value = t('返回上一级');
        if (row.id == 'public') {
            backSign.value = '';
        }
    }

    async function backSuperior() {
        let res = await FileApi.getNetParentId(props.parentId);
        if (res.data != null) {
            if (res.data.parentId == 'public') {
                backSign.value = '';
            } else {
                backSign.value = t('返回上一级');
            }
            if (props.roleType == 'manage') {
                router.push({
                    path: '/manage/index',
                    query: { parentId: res.data.parentId, roleType: props.roleType }
                });
            } else {
                router.push({
                    path: '/public/fileList/all',
                    query: { parentId: res.data.parentId, roleType: props.roleType }
                });
            }
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
            type: computed(() => t(typeName)),
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

    function openDownloadRecord(id) {
        fileId.value = id;
        recordType.value = 'downloadRecord';
        Object.assign(dialogConfig.value, {
            show: true,
            width: '25%',
            title: computed(() => t('下载记录')),
            type: 'record',
            showFooter: false
        });
    }

    function openPublicRecord(id) {
        fileId.value = id;
        recordType.value = 'publicRecord';
        Object.assign(dialogConfig.value, {
            show: true,
            width: '30%',
            title: computed(() => t('公开记录')),
            type: 'record',
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
                FileApi.delete(ids).then((res) => {
                    ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
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
        formData.append('parentId', props.parentId);
        formData.append('listType', 'public');
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

    function toSearchView() {
        let query = {
            parentId: props.parentId,
            fileNodeType: props.fileNodeType,
            searchName: searchKey.value,
            startDate: startTime.value,
            endDate: endTime.value
        };
        if (props.roleType == 'manage') {
            router.push({ path: '/manage/search', query: query });
        } else {
            router.push({ path: '/public/fileList/search', query: query });
        }
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

    function publicTo() {
        Object.assign(dialogConfig.value, {
            show: true,
            width: '50%',
            title: computed(() => t('文件公开')),
            type: 'public',
            okText: computed(() => t('公开至')),
            showFooter: true
        });
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
                    formData.value.listType = 'public';
                }

                FileApi.saveFileNode(formData.value).then((res) => {
                    let msg = t('文件夹创建成功');
                    if (optSign.value == 'rename') {
                        msg = t('文件夹重命名成功');
                        if (formData.value.fileType != 0) {
                            msg = t('文件重命名成功');
                        }
                    }
                    loadList();
                    ElMessage({ type: res.success ? 'success' : 'error', message: msg, offset: 65 });
                    editIndex.value = '';
                    isEmptyData.value = false;
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

    .optButtonCss {
        i {
            color: var(--el-color-primary) !important;
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

    :deep(.y9-dialog-header-title) {
        font-size: v-bind('fontSizeObj.mediumFontSize') !important;
    }

    :deep(.el-form-item) {
        display: inline-flex;
        vertical-align: middle;
        margin-right: 10px;
        margin-bottom: 0px;

        .el-form-item__label {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }

    :deep(.aplayer-fixed) {
        position: relative !important;
        padding: 0px 360px;
    }

    :deep(.aplayer-body) {
        position: relative !important;
    }

    :deep(.el-date-editor--daterange) {
        width: 250px;

        .el-range__icon,
        .el-range-input,
        .el-range-separator {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
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

    :deep(.el-dropdown) {
        line-height: 25px;
    }

    .star {
        font-size: 22px;
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
    }

    .search-input {
        width: 150px;
        margin-right: 10px;
        font-size: v-bind('fontSizeObj.baseFontSize');
    }

    .upload-div {
        display: inline-block;
        margin-right: 10px;
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
        margin-right: 5px;
    }

    .location span:after {
        content: ' >';
    }

    .location span:last-child {
        color: black;
    }

    :global(.el-date-range-picker .el-date-range-picker__header button) {
        font-size: v-bind('fontSizeObj.smallFontSize');
    }

    :global(.el-date-range-picker .el-date-range-picker__header div) {
        font-size: v-bind('fontSizeObj.mediumFontSize');
    }

    :global(.el-date-range-picker .el-date-table) {
        font-size: v-bind('fontSizeObj.smallFontSize');
    }
</style>
