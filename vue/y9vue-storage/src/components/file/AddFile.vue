<template>
    <div class="addfile"> 
        <el-upload
            ref="upload"
            :auto-upload="false"
            :file-list="filesList"
            :http-request="saveFile"
            :multiple="multiple"
            :on-change="onChange"
            :on-remove="onRemove"
            action=""
            class="upload-demo"
            drag
            style="margin: 20px"
        >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
            拖拽文件上传  或 点击按钮  <em> <el-button :size="fontSizeObj.buttonSize" :style="{ fontSize: fontSizeObj.baseFontSize }" type="primary"
                    >{{ $t('选取文件') }}
                </el-button></em>
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    请上传不超过120MB的文件
                </div>
            </template>
        </el-upload>
        <div class="foot-button">
            <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    style="margin-left: 10px"
                    type="primary"
                    @click="submitUpload"
                >
                    {{ $t('确定上传') }}
            </el-button>
            <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    style="margin-left: 10px"
                    @click="cancelUpload"
                >
                    {{ $t('取消') }}
            </el-button>
        </div>
        <div
            v-if="uploadLoading"
            v-loading="true"
            :element-loading-text="$t('正在上传中..')"
            class="loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
            element-loading-spinner="el-icon-loading"
        >
            <el-progress
                :percentage="percentage"
                :show-text="true"
                :stroke-width="18"
                :text-inside="true"
                class="progress"
                color="#67c23a"
                type="line"
            ></el-progress>
        </div>
    </div>
    
</template>

<script lang="ts" setup>
    import { defineProps, inject, reactive, ref, toRefs } from 'vue';
    import type { UploadInstance } from 'element-plus';
    import { ElMessage } from 'element-plus';
    import axios from 'axios';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import { useI18n } from 'vue-i18n';
    import { UploadFilled } from '@element-plus/icons-vue'

    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const props = defineProps({
        reloadTable: Function,
        dialogConfig: {
            type: Object,
            default: () => {
                return {};
            }
        },
        parentId: {
            require: false,
            type: String
        },
        listType: {
            require: false,
            type: String
        },
    });

    const upload = ref<UploadInstance>();

    const data = reactive({
        multiple: true,
        uploadLoading: false,
        filesList: [],
        percentage: 0
    });

    let { multiple, uploadLoading, filesList, percentage } = toRefs(data);

    const MAX_SIZE = 120 * 1024 * 1024; // 120MB

    function submitUpload() {
        if (filesList.value.length == 0) {
            ElMessage({ type: 'error', message: t('请选择文件上传！'), offset: 65, appendTo: '.upload-demo' });
            return;
        }
        const totalSize = filesList.value.reduce((sum, f) => sum + f.size, 0);
        if (totalSize > MAX_SIZE) {
            ElMessage({
                type: 'warning',
                message: '文件总大小不能超过120MB，需要上传大文件，请点击"大文件上传"按钮。',
                offset: 65,
                appendTo: '.upload-demo'
            });
            return;
        }
        upload.value!.submit();
    }

    const onChange = (file, fileList) => {
        const totalSize = fileList.reduce((sum, f) => sum + f.size, 0);
        if (totalSize > MAX_SIZE) {
            // 移除超限文件
            filesList.value = fileList.filter((f) => f.uid !== file.uid);
            ElMessage({
                type: 'warning',
                message: '文件总大小不能超过120MB，需要上传大文件，请点击"大文件上传"按钮。',
                offset: 65,
                appendTo: '.upload-demo'
            });
            return;
        }
        filesList.value = fileList;
    };

    const onRemove = (file, fileList) => {
        filesList.value = fileList;
    };

    const getToken = () => {
        return y9_storage.getObjectItem(settings.siteTokenKey, 'access_token');
    };

    const cancelUpload = () => {
        upload.value!.clearFiles();
        filesList.value = [];
        props.dialogConfig.show = false;
    };

    function saveFile(params) {
        percentage.value = 0;
        let formData = new FormData();
        formData.append('file', params.file);
        formData.append('parentId', props.parentId);
        formData.append('listType', props.listType);
        let config = {
            onUploadProgress: (progressEvent) => {
                //progressEvent.loaded:已上传文件大小,progressEvent.total:被上传文件的总大小
                let percent = ((progressEvent.loaded / progressEvent.total) * 100) | 0;
                percentage.value = percent;
            },
            headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: 'Bearer ' + getToken()
            }
        };
        uploadLoading.value = true;
        axios
            .post(import.meta.env.VUE_APP_CONTEXT + 'vue/fileNode/uploadFile', formData, config)
            .then((res) => {
                uploadLoading.value = false;
                ElMessage({
                    type: res.data.data.success ? 'success' : 'error',
                    message: res.data.data.msg,
                    offset: 65,
                    appendTo: '.upload-demo'
                });
                if (res.data.data.success) {
                    props.reloadTable();
                    // 延迟关闭弹窗，先让用户看到提示信息
                    setTimeout(() => {
                        props.dialogConfig.show = false;
                    }, 1500);
                }
            })
            .catch((err) => {
                uploadLoading.value = false;
                ElMessage({ type: 'error', message: t('发生异常'), offset: 65, appendTo: '.upload-demo' });
            });
    }
</script>

<style>
    .addfile { 
        min-height: 20vh;
    }
    .addfile .el-main-table {
        padding: 0px;
    }

    .addfile .el-card__body {
        padding: 0 20px;
    }

    .addfile .el-table__header-wrapper {
        border-top: 1px solid #ebeef5;
    }

    .addfile .el-table-column--selection .cell {
        padding-left: 10px;
        padding-right: 10px;
    }

    .addfile .el-progress-bar__outer {
        background-color: #bbb;
    }

    .addfile .foot-button {
        text-align: right;
    }

    .loading {
        position: absolute;
        left: 0;
        top: 0;
        right: 0;
        bottom: 0;
        background: black;
        opacity: 0.8;
    }

    .progress {
        width: 200px;
        height: 200px;
        position: absolute;
        top: 50%;
        left: 50%;
        margin-left: -100px;
        margin-top: -50px;
        z-index: 99999;
    }
</style>

<style lang="scss" scoped>
    .upload-demo {
        /*message */
        :global(.el-message .el-message__content) {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }
</style>
