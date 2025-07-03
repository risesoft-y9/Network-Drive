<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-09-10 11:18:27
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-04-08 14:40:31
 * @FilePath: \y9vue-app\y9-vue\y9vue-storage\src\components\storage\Uploader\index.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
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
        <uploader-drop>
            <p>将文件拖放到此处进行上传或点击"选择文件"按钮上传</p>
            <uploader-btn>选择文件</uploader-btn>
            <!-- <uploader-btn :attrs="attrs">select images</uploader-btn> -->
        </uploader-drop>
        <uploader-list></uploader-list>
    </uploader>
</template>

<script setup>
    import { nextTick, ref, onMounted, defineProps, defineComponent } from 'vue';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import axios from 'axios';
    import qs from 'qs';

    const props = defineProps({
        parentId: String,
        listType: String,
        reloadTable: Function
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
                props.reloadTable();
            })
            .catch(function (error) {
                console.log(error);
                
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
    .uploader-example {
        width: 880px;
        padding: 15px;
        margin: 0px auto 0;
        font-size: 12px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.4);
    }
    .uploader-example .uploader-btn {
        margin-right: 4px;
    }
    .uploader-example .uploader-list {
        max-height: 440px;
        overflow: auto;
        overflow-x: hidden;
        overflow-y: auto;
    }
</style>
