<template>
    <y9Card :showHeader="false">
        <div class="toolbar">
            <div class="toolbar-left">
                <el-button
                    v-if="multipleSelection.length && isFile"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    plain
                    v-on:click="download"
                >
                    <i class="ri-download-2-line"></i>{{ $t('下载') }}
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
        <el-form ref="fileForm" :model="formData" class="formClass">
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
                :empty-text="$t('暂无数据')"
                element-loading-background="rgba(0, 0, 0, 0.8)"
                element-loading-spinner="el-icon-loading"
                @on-change="handleSelectionChange"
                @row-click="selectRow"
                @select-all="selectAll"
            >
                <template #name="{ row, column, index }">
                    <el-row @mouseenter="titleHover(row.id)" @mouseleave="titleLeave(row.id)">
                        <el-col :span="22">
                            <FileNameWithIcon :file-node="row" @fileClick="openFile(row)" @folderClick="subList" />
                        </el-col>
                        <el-col :span="2">
                            <div v-if="optButtonShow == row.id" class="optButtonCss">
                                <el-tooltip
                                    v-if="row.collect"
                                    :content="$t('取消收藏')"
                                    class="box-item"
                                    effect="light"
                                    placement="top-start"
                                >
                                    <i class="ri-star-line" @click="cancelCollect(row)"></i>
                                </el-tooltip>
                                <el-tooltip
                                    v-if="row.filePassword == '' || row.filePassword == null"
                                    :content="$t('下载')"
                                    class="box-item"
                                    effect="light"
                                    placement="top-start"
                                >
                                    <i class="ri-download-2-line" @click="downloadFile(row.id)"></i>
                                </el-tooltip>
                            </div>
                        </el-col>
                    </el-row>
                </template>
                <template #fileSize="{ row, column, index }">
                    {{ row.fileSize ? row.fileSize : '-' }}
                </template>
                <template #collectRoute="{ row, column, index }">
                    <el-tooltip :content="row.collectRoute" placement="top">
                        <el-link v-if="row.listType == 'my'" :title="row.collectRoute">{{ $t('我的文件') }}</el-link>
                        <el-link v-else-if="row.listType == 'shared'" :title="row.collectRoute"
                            >{{ $t('共享空间') }}
                        </el-link>
                        <el-link v-else-if="row.listType == 'dept'" :title="row.collectRoute"
                            >{{ $t('部门文件') }}
                        </el-link>
                        <el-link v-else-if="row.listType == 'public'" :title="row.collectRoute"
                            >{{ $t('公共文件') }}
                        </el-link>
                    </el-tooltip>
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
                                path: '/collect/index',
                                query: { parentId: row.parentId, listType: listType }
                            }"
                        >
                            {{ row.parentFileNode.name }}
                        </router-link>
                        <router-link v-else to="/collect/index">{{ $t('全部文件') }}</router-link>
                    </div>
                </template>
            </y9Table>
        </el-form>
        <y9Dialog v-model:config="dialogConfig">
            <!-- 视频播放器 -->
            <VideoPlayer v-if="dialogConfig.type == 'video'" :poster="poster" :video_url="fileUrl" />
            <!-- 视频播放器 -->
            <!-- txt,java,js,java,vue,css,xml文件预览-->
            <TextViewer v-if="dialogConfig.type == 'txt'" :fileId="fileId" :fileName="fileName" :fileUrl="fileUrl" />
            <!--音频文件预览-->
            <AudioPlayer v-if="dialogConfig.type == 'mp3'" :audioArray="audioArray" />
            <!--直链-->
            <FileLink v-if="dialogConfig.type == 'link'" :fileObject="fileObject" />
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
    import { ref, onMounted, watch, computed, reactive, toRefs, nextTick, inject } from 'vue';
    import FileApi from '@/api/storage/file';
    import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
    import FileLink from '@/components/file/FileLink.vue';
    import DecryptPwd from '@/components/storage/Folder/decrypt.vue';
    import TextViewer from '@/components/file/TextViewer.vue';
    import AudioPlayer from '@/components/file/AudioPlayer.vue';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import { useRoute, useRouter } from 'vue-router';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useSettingStore } from '@/store/modules/settingStore';
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
        listType: {
            require: false,
            type: String
        }
    });
    const storageStore = useStorageStore();
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 260 - 25);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 260 - 25;
        })();
    };
    const multipleTable = ref();

    const data = reactive({
        uploadShowBtn: '',
        y9UserInfo: {},
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
        optButtonShow: '',
        buttonMore: false,
        selectedDate: '',
        listType: '',
        fileForm: '',
        formData: { id: '', parentId: '', name: '' },
        isFile: false,
        recursiveToRootFileNodeList: [],
        orderProp: 'CREATE_TIME',
        orderAsc: true,
        searchKey: '',
        multipleSelection: [],
        loading: false,
        y9TableConfig: {
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
                    width: 'auto',
                    sortable: true,
                    slot: 'name'
                },
                { title: computed(() => t('所有者')), key: 'userName', align: 'left', sortable: true, width: '170' },
                { title: computed(() => t('大小')), key: 'fileSize', width: '120', sortable: true, slot: 'fileSize' },
                {
                    title: computed(() => t('收藏路径')),
                    key: 'collectRoute',
                    width: '150',
                    sortable: true,
                    slot: 'collectRoute'
                },
                { title: computed(() => t('创建日期')), key: 'createTime', sortable: true, width: '170' }
            ],
            tableData: []
        },
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {},
            visibleChange: (visible) => {
                if (!visible) {
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
        formData,
        isFile,
        buttonMore,
        recursiveToRootFileNodeList,
        orderProp,
        orderAsc,
        searchKey,
        multipleSelection,
        loading,
        optButtonShow,
        dialogConfig,
        listType,
        selectedDate,
        backSign
    } = toRefs(data);

    onMounted(() => {
        y9UserInfo.value = y9_storage.getObjectItem('ssoUserInfo');
        loadList();
    });

    watch(
        () => [props.parentId],
        ([pId]) => {
            if (pId) {
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
            loadList();
        },
        {
            deep: true,
            immediate: true
        }
    );

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

    function refresh() {
        selectedDate.value = '';
        searchKey.value = '';
        loadList();
    }

    function selectRow(row, column, event) {
        if (row.id != '') {
            if (!row.filePassword) {
                multipleTable.value.elTableRef.toggleRowSelection(row);
            }
            if (row.fileType != 0) {
                isFile.value = true;
            } else {
                isFile.value = false;
            }
        } else {
            isFile.value = false;
            multipleTable.value.elTableRef.clearSelection();
        }
    }

    function selectAll(selection) {
        console.log('multipleSelection.value', multipleSelection.value);
    }

    function handleSelectionChange(id, data) {
        multipleSelection.value = data;
        console.log('selecy', data);
        if (data.length == 1) {
            if (data[0].fileType != 0) {
                isFile.value = true;
            } else {
                isFile.value = false;
            }
        }
    }

    function loadList() {
        loading.value = true;
        if (orderProp.value == 'CREATE_TIME') {
            orderAsc.value = false;
        }
        FileApi.getCollectList(props.parentId, searchKey.value, props.listType, orderProp.value, orderAsc.value).then(
            (res) => {
                loading.value = false;
                y9TableConfig.value.tableData = res.data.subFileNodeList;
                recursiveToRootFileNodeList.value = res.data.recursiveToRootFileNodeList;
            }
        );
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
            router.push({ path: '/collect/index', query: { parentId: row.id, listType: row.listType } });
        }
        backSign.value = t('返回上一级');
        if (row.id == 'collect') {
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

    function openFolder(row) {
        router.push({ path: '/collect/index', query: { parentId: row.id, listType: props.listType } });
        backSign.value = t('返回上一级');
        if (row.id == 'collect') {
            backSign.value = '';
        }
    }

    function cancelCollect(row) {
        FileApi.cancelCollect(row.id).then((res) => {
            if (res.success) {
                loadList();
            }
        });
    }

    async function backSuperior() {
        let res = await FileApi.getNetParentId(props.parentId);
        if (res.data != null) {
            if (res.data.parentId == 'collect') {
                backSign.value = '';
            } else {
                backSign.value = t('返回上一级');
            }

            router.push({ path: '/collect/index', query: { parentId: res.data.parentId, listType: props.listType } });
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
        multipleSelection.value.forEach((item) => {
            if (item.fileType == 0) {
                ElMessage({ type: 'info', message: t('选中的数据包含文件夹，请选择文件进行下载！'), offset: 65 });
                return;
            }
        });
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
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';

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
        width: 250px;
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
</style>
