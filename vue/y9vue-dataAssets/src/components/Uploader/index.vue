<template>
    <y9Table :config="tableConfig">
        <template #opt="{row,columns,index}">
            <i class="ri-arrow-down-circle-line" style="font-size: 22px;margin-right: 5px;" title="下载" @click="downloadFile(row.id)"></i>
            <i class="ri-close-circle-line" style="font-size: 22px;" title="删除" @click="deleteFile(row.id)"></i>
        </template>
    </y9Table>
    <uploader
        :options="options"
        :file-status-text="statusText"
        class="uploader-example"
        ref="uploaderRef"
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
    import { nextTick, ref, onMounted, defineProps, defineComponent, toRefs } from 'vue';
    import y9_storage from '@/utils/storage';
    import { getArchivesFileList,deleteArchivesFile } from '@/api/dataAssets/files';
    import settings from '@/settings';
    import axios from 'axios';
    import qs from 'qs';
    import { useI18n } from 'vue-i18n';
    
    const { t } = useI18n();
    const props = defineProps({
        archivesId: String,
        reloadTable: Function
    });

    const data = reactive({
        tableConfig: {
            columns: [
                { title: '序号', type: 'index', width: '60' },
                { title: '文件名称', key: 'fileName',  },
                { title: '文件类型', key: 'fileType', width: '120' },
                { title: '上传人', key: 'personName', width: '150' },
                { title: '操作', width: '120', slot: 'opt' }
            ],
            border: false,
            tableData: [],
            pageConfig: false,
            height: '240'
        },
    });

    let { tableConfig} = toRefs(data);

    getFileList();
    function getFileList() {
        getArchivesFileList(props.archivesId).then(res => {
            tableConfig.value.tableData = res.data;
        })
    }

    function deleteFile(id){
        ElMessageBox.confirm(t('确认要删除文件吗?'), t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        }).then(() => {
            deleteArchivesFile(id).then(() => {
                getFileList();
                props.reloadTable();
            });
        }).catch(() => {
            ElMessage({ type: "info", message: t("已取消操作"), offset: 65 });
        });
    }

    function downloadFile(id){
        window.open(import.meta.env.VUE_APP_CONTEXT + "vue/files/downloadFile?id=" + id + "&access_token=" + y9_storage.getObjectItem(settings.siteTokenKey, "access_token"), "_blank");
    }

    const uploaderRef = ref(null);
    const options = {
        target: import.meta.env.VUE_APP_CONTEXT + 'vue/files/chunk', // '//jsonplaceholder.typicode.com/posts/',
        query: {
            archivesId: props.archivesId,
            access_token: y9_storage.getObjectItem(settings.siteTokenKey, 'access_token')
        },
        testChunks: false,
        chunkSize: 10 * 1024 * 1024
    };
    const attrs = {
        accept: 'image/*'
    };
    const statusText = {
        success: 'success',
        error: 'error',
        uploading: 'uploading',
        paused: 'paused',
        waiting: 'waiting'
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
                import.meta.env.VUE_APP_CONTEXT + 'vue/files/mergeFile',
                qs.stringify({
                    filename: file.name,
                    identifier: rootFile.uniqueIdentifier,
                    totalSize: file.size,
                    type: file.type,
                    archivesId: props.archivesId,
                }),
                config
            )
            .then(function (response) {
                console.log(response);
                getFileList();
                props.reloadTable();
            })
            .catch(function (error) {
                console.log(error);
            });
    };

    onMounted(() => {
        console.log('archivesId',props.archivesId);
        
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
        width: 35vw;
        /* padding: 15px; */
        margin: 0px auto 0;
        font-size: 12px;
        /* box-shadow: 0 0 10px rgba(0, 0, 0, 0.4); */
    }
    .uploader-drop {
        position: relative;
        height: 17.7vh;
        padding: 10px;
        overflow: hidden;
        border: 1px dashed #ccc;
        background-color: #f5f5f5;
    }
    .uploader-example .uploader-btn {
        margin-right: 4px;
    }
    .uploader-example .uploader-list {
        max-height: 26vh;
        overflow: auto;
        overflow-x: hidden;
        overflow-y: auto;
    }
</style>
