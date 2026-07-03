<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-09-10 11:18:27
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-07-01 15:58:59
 * @FilePath: \y9vue-app\y9-vue\y9vue-storage\src\components\storage\Uploader\index.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <div class="big-file-uploader">
        <uploader
            :options="options"
            :file-status-text="statusText"
            class="uploader-example"
            ref="uploaderRef"
            @fileError="fileError"
            @fileComplete="fileComplete"
            @complete="complete"
        >
            <uploader-unsupport></uploader-unsupport>
            <uploader-drop class="uploader-drop-area">
                <div class="drop-content">
                    <i class="ri-upload-cloud-2-line drop-icon"></i>
                    <p class="drop-text">将文件拖放到此处进行上传</p>
                    <p class="drop-tip">支持任意大小文件，分片上传</p>
                    <uploader-btn class="drop-btn">选择文件</uploader-btn>
                </div>
            </uploader-drop>
            <uploader-list></uploader-list>
        </uploader>
    </div>
</template>

<script setup>
    import { nextTick, ref, onMounted, defineProps, defineComponent } from 'vue';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import axios from 'axios';
    import qs from 'qs';
    import { ElMessage } from 'element-plus';

    const props = defineProps({
        parentId: String,
        listType: String,
        reloadTable: Function,
        dialogConfig: {
            type: Object,
            default: () => {
                return {};
            }
        }
    });

    const uploaderRef = ref(null);
    const options = {
        target: import.meta.env.VUE_APP_CONTEXT + 'vue/uploader/chunk', // '//jsonplaceholder.typicode.com/posts/',
        query: {
            parentId: 'my',
            access_token: y9_storage.getObjectItem(settings.siteTokenKey, 'access_token')
        },
        testChunks: true,
        chunkSize: 10 * 1024 * 1024,
        successStatuses: [200,201,202]
    };
    const attrs = {
        accept: 'image/*'
    };
    const statusText = {
        success: '上传成功',
        error: '上传失败',
        uploading: '上传中',
        paused: '暂停',
        waiting: '等待上传中'
    };

    const fileError = (rootFile, file, message, chunk) => {
        console.log('fileError', message);
        ElMessage({ type: 'error', message: message, offset: 65 });
        
    };
    const complete = () => {
        console.log('complete', arguments);
    };
    const fileComplete = (rootFile) => {
        console.log('file complete', arguments);
        console.log('rootFile', rootFile);
        let config = {
            onUploadProgress: (progressEvent) => {
                //progressEvent.loaded:已上传文件大小,progressEvent.total:被上传文件的总大小
                // let percent = (progressEvent.loaded / progressEvent.total * 100) | 0;
                // percentage.value = percent;
            },
            headers: {
                //'Content-Type': 'multipart/form-data',
                Authorization: 'Bearer ' + y9_storage.getObjectItem(settings.siteTokenKey, 'access_token')
            }
        };
        const file = rootFile.file;
        console.log('filefilefilearguments[0]', file);

        axios
            .post(
                import.meta.env.VUE_APP_CONTEXT + 'vue/uploader/mergeFile',
                qs.stringify({
                    filename: file.name,
                    identifier: rootFile.uniqueIdentifier,
                    totalSize: file.size,
                    type: file.type,
                    parentId: props.parentId,
                    listType: props.listType
                }),
                config
            )
            .then(function (response) {
                console.log(response);
                const resData = response.data.data;
                if (resData && resData.msg) {
                    ElMessage({
                        type: 'success',
                        message: resData.msg,
                        offset: 65
                    });
                } else {
                    ElMessage({
                        type: 'success',
                        message: '文件上传成功',
                        offset: 65
                    });
                }
                props.reloadTable();
                setTimeout(() => {
                        props.dialogConfig.show = false;
                    }, 1500);
            })
            .catch(function (error) {
                console.log(error);
                ElMessage({ type: 'error', message: '文件合并失败', offset: 65 });
            });
    };

    onMounted(() => {
        console.log('parentId',props.parentId);
        
        nextTick(() => {
            window.uploader = uploaderRef.value.uploader;
        });
    });
    defineExpose({
        uploaderRef,
        options,
        attrs,
        statusText,
        complete,
        fileComplete
    });
</script>

<style>
    .big-file-uploader {
        min-height: 20vh;
        padding: 10px 0;
    }

    .uploader-file-info{
        width: 100%;
    }
    

    .uploader-example {
        width: 100%;
        font-size: 14px;
    }

    /* 拖拽区域 */
    .uploader-example .uploader-drop-area {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 20vh;
        padding: 40px 20px;
        border: 2px dashed #c0c4cc;
        border-radius: 8px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s ease;
    }
    .uploader-example .uploader-drop-area:hover {
        border-color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
    }
    .uploader-example .uploader-drop-area.uploader-dragover {
        border-color: #67c23a;
        background: #f0f9eb;
        box-shadow: 0 0 12px rgba(103, 194, 58, 0.2);
    }

    .drop-content {
        text-align: center;
    }

    .drop-icon {
        font-size: 48px;
        color: #c0c4cc;
        display: block;
        margin-bottom: 12px;
        transition: color 0.3s;
    }
    .uploader-drop-area:hover .drop-icon,
    .uploader-drop-area.uploader-dragover .drop-icon {
        color: var(--el-color-primary);
    }

    .drop-text {
        margin: 0 0 6px;
        color: #606266;
        font-size: 15px;
    }

    .drop-tip {
        margin: 0 0 18px;
        color: #909399;
        font-size: 13px;
    }

    /* 选择文件按钮 */
    .uploader-example .uploader-btn {
        display: inline-block;
        padding: 9px 24px;
        background: var(--el-color-primary);
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: background 0.3s;
    }
    .uploader-example .uploader-btn:hover {
        background: var(--el-color-primary-light-3);
    }

    /* 文件列表 */
    .uploader-example .uploader-list {
        max-height: 20vh;
        margin-top: 16px;
        border: 1px solid #ebeef5;
        border-radius: 6px;
        overflow: auto;
        overflow-x: hidden;
    }
    .uploader-example .uploader-list .uploader-file {
        display: flex;
        align-items: center;
        padding: 10px 14px;
        border-bottom: 1px solid #f2f3f5;
        transition: background 0.2s;
    }
    .uploader-example .uploader-list .uploader-file:last-child {
        border-bottom: none;
    }
    .uploader-example .uploader-list .uploader-file:hover {
        background: #f5f7fa;
    }
    .uploader-example .uploader-list .uploader-file-name {
        flex: 1;
        color: #303133;
        font-size: 13px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .uploader-example .uploader-list .uploader-file-size {
        color: #909399;
        font-size: 12px;
        margin-right: 12px;
        white-space: nowrap;
    }
    .uploader-example .uploader-list .uploader-file-status {
        font-size: 12px;
        white-space: nowrap;
    }
    .uploader-example .uploader-list .uploader-file-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-left: 10px;
    }
    .uploader-example .uploader-list .uploader-file-actions > * {
        cursor: pointer;
        color: #909399;
        transition: color 0.2s;
    }
    .uploader-example .uploader-list .uploader-file-actions > *:hover {
        color: var(--el-color-primary);
    }

    /* 进度条 */
    .uploader-example .uploader-file-progress {
        height: 48px;
        background: #f0f0f0;
        border-radius: 3px;
        overflow: hidden;
        margin-top: 6px;
    }
    .uploader-example .uploader-file-progress-inner {
        height: 100%;
        background: var(--el-color-primary);
        border-radius: 3px;
        transition: width 0.3s;
    }
    .uploader-file-status {
        width: 20%;
        text-indent: 20px;
    }
</style>
