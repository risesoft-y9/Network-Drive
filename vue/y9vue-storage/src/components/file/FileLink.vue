<template>
    <el-row class="rowCss">
        <el-input v-model="file.name" :prefix-icon="Document" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-input v-model="file.createTime" :prefix-icon="Calendar" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-input v-model="file.fileSize" :prefix-icon="Coin" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-radio-group v-model="encryption" @change="changePwd">
            <el-radio :label="true" border>{{ $t('加密') }}</el-radio>
            <el-radio :label="false" border>{{ $t('不加密') }}</el-radio>
        </el-radio-group>
    </el-row>
    <el-row class="rowCss" v-if="urlShow">
        <el-input
            ref="linkRef"
            v-model="file.fileUrl"
            :prefix-icon="Link"
            :readonly="true"
            class="w-50 m-2"
            @click="selectlink"
        />
        
    </el-row>
    <el-row class="rowCss center" v-if="!encryption">
        <el-button 
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="copyLink"
                >{{ $t('复制链接') }}
            </el-button>
    </el-row>
    <el-row v-if="encryption" :gutter="10" class="rowCssTwo">
        <el-col :span="3">下载码：</el-col>
        <el-col :span="21"> 
            <el-input v-if="pwdShow"
                v-model="decodedLinkPassword"
                :prefix-icon="Lock"
                class="w-50 m-2"
                clearable
                maxlength="6"
                show-password
                show-word-limit
                type="password"
                >
                <template #append>六位数密码（字母、数字）</template>
            </el-input>
            <el-input v-if="linkPwdShow"
                v-model="linkPwd"
                :prefix-icon="Lock"
                class="w-50 m-2"
                type="text"
                style="width: 30%;"
            >
            </el-input>
        </el-col>   
    </el-row>
    <el-row  :gutter="10" class="rowCss center" v-if="encryption">
        <el-button v-if="copyLinkBtn"
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="copyLinkPwd"
                >{{ $t('复制链接及下载码') }}
            </el-button>
        <el-button 
                v-if="createLinkBtn"
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="submitPwd('save')"
                >{{ $t('创建链接') }}
            </el-button>
    </el-row>
   
</template>
<script lang="ts" setup>
    import { defineProps, inject, nextTick, reactive, toRefs, watch } from 'vue';
    import { Calendar, Coin, Document, Link, Lock } from '@element-plus/icons-vue';
    import FileApi from '@/api/storage/file';
    import type { ElMessage } from 'element-plus';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useI18n } from 'vue-i18n';

    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const storageStore = useStorageStore();
    const props = defineProps({
        fileObject: {
            //表格配置
            type: Object,
            default: () => {
                return {};
            }
        }
    });

    let data = reactive({
        linkUrl: props.fileObject.fileUrl,
        file: {},
        linkRef: '',
        decodedLinkPassword:'',
        copyLinkBtn:false,
        createLinkBtn:false,
        encryption:false,
        pwdShow: true,
        urlShow: true,
        linkPwd:'',
        linkPwdShow: false,
    });

    let { linkUrl, file, linkRef ,decodedLinkPassword,copyLinkBtn,createLinkBtn,encryption,pwdShow,urlShow,linkPwd,linkPwdShow} = toRefs(data);

    watch(
        () => props.fileObject,
        (newVal) => {
            file.value = newVal;
            if (newVal.encryption) {
                
                console.log('linkpwd:',props.fileObject.linkPassword);
                // try {
                //     if(props.fileObject.linkPassword){
                //         decodedLinkPassword.value = atob(props.fileObject.linkPassword);
                //     }
                // } catch(e){
                //     decodedLinkPassword.value = '';
                // }
            }
        },
        {
            deep: true,
            immediate: true
        }
    );


    function changePwd(val) {
        props.fileObject.encryption = val;
        linkPwdShow.value = false;
        if (val) {
            props.fileObject.fileUrl = '';
            urlShow.value = false;
            copyLinkBtn.value = false;
            pwdShow.value = true;
            createLinkBtn.value = true;
        } else {
            urlShow.value = true;
            createLinkBtn.value = false;
            pwdShow.value = false;
            props.fileObject.fileUrl = linkUrl.value;
            decodedLinkPassword.value = '';
            //submitPwd('clear');
        }
    }

    function copyLink(){
        if (file.value.fileUrl) {
            navigator.clipboard.writeText(file.value.fileUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
        } else {
            ElMessage({ type: 'error', message: t('链接为空'), offset: 65 });
        }
    }

    function copyLinkPwd(){
        if (file.value.fileUrl) {
            let linkUrl = "下载链接：" + file.value.fileUrl + "\n下载码：" + linkPwd.value;
            navigator.clipboard.writeText(linkUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
        } else {
            ElMessage({ type: 'error', message: t('链接为空'), offset: 65 });
        }

    }

    function selectlink() {
        nextTick(() => {
            linkRef.value.focus();
            linkRef.value.select();
        });
    }

    function submitPwd(type) {
        if (decodedLinkPassword.value) {
            if(decodedLinkPassword.value.length < 6){
                ElMessage({ type: 'error', message: t('请输入长度为六位的下载码'), offset: 65 });
                return;
            }
            FileApi.createLink(file.value.id, decodedLinkPassword.value).then(res => {
                if(res.success){
                    let link = res.data;
                    file.value.fileUrl = import.meta.env.VUE_APP_HOST_INDEX + 'link?tenantId=' + storageStore.tenantId + '&key=' + link.linkKey;
                    copyLinkBtn.value = true;
                    createLinkBtn.value = false;
                    pwdShow.value = false;
                    urlShow.value = true;
                    linkPwdShow.value = true;
                    linkPwd.value = atob(link.linkPassword);
                }
                if (type == 'save') {
                    ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
                }
            });
            // FileApi.setLinkPwd(file.value.id, file.value.encryption, decodedLinkPassword.value).then((res) => {
            //     if (type == 'save') {
            //         ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
            //     }
            // });
        } else {
            ElMessage({ type: 'error', message: t('请输入文件下载密码'), offset: 65 });
            return;
        }
    }
</script>
<style lang="scss" scoped>
    @import '@/theme/global.scss';

    .rowCss {
        padding: 10px 0px;
        :deep(.el-input__wrapper) {
            box-shadow: 0 0 0 0px var(--el-input-border-color, var(--el-border-color)) inset;
        }

        
    }
    .rowCssTwo {
        padding: 10px 0px;
    }
    .center {
            text-align: center;
            display: block;
        }
</style>
