<template>
    <div v-if="optType == 'rename' || optType == 'add'">
        <div v-if="fileNodeObj.fileType === 0">
            <span class="file-icon">
                <div
                    v-if="fileNodeObj.filePassword == '' || fileNodeObj.filePassword == null"
                    class="file-icon-div dir-small"
                ></div>
                <div v-else class="file-icon-div dir-lock-small"></div>
            </span>
            <!-- <span class="file-name" @click="folderClick(fileNodeObj)">{{ fileNodeObj.name }}</span> -->
        </div>
        <div v-else>
            <span class="file-icon">
                <div class="file-icon-div" v-bind:class="fileTypeClass(fileNodeObj.fileSuffix)"></div>
            </span>
            <!-- <span class="file-name" @click="fileClick" >{{ fileNodeObj.name }}</span> -->
        </div>
    </div>
    <div v-else>
        <div v-if="fileNodeObj.fileType === 0">
            <span class="file-icon">
                <div
                    v-if="fileNodeObj.filePassword == '' || fileNodeObj.filePassword == null"
                    class="file-icon-div dir-small"
                ></div>
                <div v-else class="file-icon-div dir-lock-small"></div>
            </span>
            <span class="file-name" @click="folderClick(fileNodeObj)">{{ fileNodeObj.name }}</span>
        </div>
        <div v-else-if="fileNodeObj.fileType === 1">
            <span class="file-icon">
                <img
                    :src="fileNodeObj.fileUrl"
                    style="width: 22px; height: 22px"
                    @click="viewImg(fileNodeObj.fileUrl)"
                />
            </span>
            <span class="file-name" @click="fileClick">{{ fileNodeObj.name }}</span>
        </div>
        <div v-else>
            <span class="file-icon">
                <div class="file-icon-div" v-bind:class="fileTypeClass(fileNodeObj.fileSuffix)"></div>
            </span>
            <span class="file-name" @click="fileClick">{{ fileNodeObj.name }}</span>
        </div>
    </div>
</template>

<script lang="ts" setup>
    import { defineExpose, defineProps, reactive, toRefs, watch } from 'vue';
    import { $deepAssignObject } from '@/utils/object';
    import { api as viewerApi } from 'v-viewer';

    const fontSizeObj: any = inject('sizeObjInfo') || {};
    let props = defineProps({
        fileNode: {
            type: Object
        },
        optType: {
            type: String
        }
    });

    let data = reactive({
        fileNodeObj: {}
    });

    let { fileNodeObj } = toRefs(data);

    watch(
        () => props.fileNode,
        (newVal) => {
            fileNodeObj.value = $deepAssignObject(newVal);
            if (newVal.id == '') {
                fileNodeObj.value.fileType = 0;
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    const emits = defineEmits(['folderClick', 'fileClick']);

    function folderClick(row) {
        emits('folderClick', row);
    }

    function fileClick() {
        emits('fileClick');
    }

    function viewImg(fileUrl) {
        viewerApi({
            images: [fileUrl]
        });
    }

    function fileTypeClass(fileSuffix) {
        if (fileSuffix === 'doc' || fileSuffix === 'docx' || fileSuffix === 'wps') {
            return 'fileicon-small-doc';
        } else if (fileSuffix === 'pdf') {
            return 'fileicon-small-pdf';
        } else if (fileSuffix === 'xls' || fileSuffix === 'xlsx') {
            return 'fileicon-small-xls';
        } else if (fileSuffix === 'ppt' || fileSuffix === 'pptx') {
            return 'fileicon-small-ppt';
        } else if (fileSuffix === 'html' || fileSuffix === 'htm' || fileSuffix === 'xhtm') {
            return 'fileicon-sys-s-web';
        } else if (fileSuffix === 'txt') {
            return 'fileicon-small-txt';
        } else if (fileSuffix === 'zip' || fileSuffix === 'war' || fileSuffix === 'rar') {
            return 'fileicon-small-zip';
        } else if (fileSuffix === 'exe' || fileSuffix === 'msi') {
            return 'fileicon-sys-s-exe';
        } else if (fileSuffix === 'mp4' || fileSuffix === 'avi') {
            return 'fileicon-small-video';
        } else if (fileSuffix === 'mp3' || fileSuffix === 'wav') {
            return 'fileicon-small-mp3';
        } else if (
            fileSuffix === 'jsp' ||
            fileSuffix === 'java' ||
            fileSuffix === 'css' ||
            fileSuffix === 'vue' ||
            fileSuffix === 'js' ||
            fileSuffix === 'html'
        ) {
            return 'fileicon-sys-s-code';
        } else {
            return 'default-small';
        }
    }

    defineExpose({
        fileTypeClass
    });
</script>

<style scoped>
    .file-icon {
        display: inline-block;
        margin-left: 0px;
        overflow: hidden;
        position: relative;
        text-align: center;
        text-overflow: ellipsis;
        vertical-align: middle;
        white-space: nowrap;
        width: 24px;
        margin-top: 2px;
    }

    .file-name {
        position: relative;
        overflow: visible;
        text-overflow: inherit;
        vertical-align: middle;
        white-space: inherit;
        margin-left: 15px;
        font-size: v-bind('fontSizeObj.baseFontSize');
        cursor: pointer;
        color: #333333 !important;
    }

    .file-icon-div {
        cursor: default;
        display: block;
        height: 26px;
        width: 26px;
        left: 43px;
        top: 10px;
    }

    .fileicon-small-bt,
    .fileicon-small-dws,
    .fileicon-small-code,
    .fileicon-small-txt,
    .fileicon-small-pdf,
    .fileicon-small-doc,
    .fileicon-small-ppt,
    .fileicon-small-xls,
    .fileicon-small-vsd,
    .fileicon-small-pic,
    .fileicon-small-mmap,
    .fileicon-small-xmind,
    .fileicon-small-mm,
    .fileicon-small-mp3,
    .icon-play-music,
    .default-small,
    .dir-multi-small,
    .dir-multi-middle,
    .dir-small,
    .dir-lock-small,
    .dir-cang-small,
    .dir-app-small,
    .dir-apps-small,
    .dir-backup-small,
    .dir-share-middle,
    .dir-phone-small,
    .fileicon-folder-lock,
    .fileicon-sys-s-exe,
    .fileicon-sys-s-apk,
    .fileicon-sys-s-psd,
    .fileicon-sys-s-key,
    .fileicon-sys-s-ai,
    .fileicon-sys-s-ipa,
    .fileicon-sys-s-vsd,
    .fileicon-sys-s-pages,
    .fileicon-sys-s-numbers,
    .fileicon-sys-s-fonts,
    .fileicon-sys-s-code,
    .fileicon-sys-s-web,
    .fileicon-sys-s-links,
    .fileicon-sys-s-eps,
    .fileicon-sys-s-swf,
    .fileicon-sys-s-video,
    .fileicon-small-video,
    .fileicon-small-zip,
    .fileicon-small-rar {
        background-image: url('@/assets/fileType/all_fileTypes.png');
    }

    .dir-small {
        background-position: -594px -862px;
        background-repeat: no-repeat;
    }

    .dir-lock-small {
        background-position: -45px -800px;
        background-repeat: no-repeat;
    }

    .fileicon-small-doc {
        background-position: -596px -170px;
        background-repeat: no-repeat;
    }

    .fileicon-sys-s-web {
        background-position: -594px -1458px;
        background-repeat: no-repeat;
    }

    .fileicon-small-pic {
        background-position: -596px -306px;
        background-repeat: no-repeat;
    }

    .fileicon-small-pdf {
        background-position: -596px -136px;
        background-repeat: no-repeat;
    }

    .fileicon-small-txt {
        background-position: -596px -102px;
        background-repeat: no-repeat;
    }

    .fileicon-small-xls {
        background-position: -596px -238px;
        background-repeat: no-repeat;
    }

    .fileicon-small-ppt {
        background-position: -596px -204px;
        background-repeat: no-repeat;
    }

    .fileicon-small-zip {
        background-position: -596px -1664px;
        background-repeat: no-repeat;
    }

    .fileicon-sys-s-exe {
        background-position: -596px -1084px;
        background-repeat: no-repeat;
    }

    .fileicon-small-video {
        background-position: -596px -1630px;
        background-repeat: no-repeat;
    }

    .fileicon-sys-s-code {
        background-position: -596px -1424px;
        background-repeat: no-repeat;
    }

    .fileicon-small-mp3 {
        background-position: -596px -442px;
        background-repeat: no-repeat;
    }

    .default-small {
        background-position: -596px -566px;
        background-repeat: no-repeat;
    }

    .largeIcon {
        width: 90px;
        height: 80px;
        /* float: left; */
        margin: 4px 0 0 6px;
        text-align: center;
        border: 1px solid #fff;
        position: relative;
    }

    .fileicon-folder-lock {
        background: url('@/assets/fileType/folder-locked.png') center no-repeat;
    }

    .fileicon-large-xls {
        background: url('@/assets/fileType/large/Excel.png') center no-repeat;
    }

    .fileicon-large-code {
        background: url('@/assets/fileType/large/Code.png') center no-repeat;
    }

    .fileicon-large-exe {
        background: url('@/assets/fileType/large/EXE.png') center no-repeat;
    }

    .fileicon-large-folder {
        background: url('@/assets/fileType/large/Folder.png') center no-repeat;
    }

    .fileicon-large-misc {
        background: url('@/assets/fileType/large/Misc.png') center no-repeat;
    }

    .fileicon-large-pdf {
        background: url('@/assets/fileType/large/PDF.png') center no-repeat;
    }

    .fileicon-large-ppt {
        background: url('@/assets/fileType/large/PPT.png') center no-repeat;
    }

    .fileicon-large-txt {
        background: url('@/assets/fileType/large/Text.png') center no-repeat;
    }

    .fileicon-large-video {
        background: url('@/assets/fileType/large/Video.png') center no-repeat;
    }

    .fileicon-large-web {
        background: url('@/assets/fileType/large/Web.png') center no-repeat;
    }

    .fileicon-large-word {
        background: url('@/assets/fileType/large/Word.png') center no-repeat;
    }

    .fileicon-large-zip {
        background: url('@/assets/fileType/large/ZIP.png') center no-repeat;
    }

    .fileicon-large-mp3 {
        background: url('@/assets/fileType/large/Music.png') center no-repeat;
    }
</style>
