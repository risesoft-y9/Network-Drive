<template>
    <!-- <el-container>
      <el-header style="padding: 16vh 40vw;">
        <el-row class="rowCss">
            <el-col :span="6">
                <img style="width: 100px;" src="@/assets/images/yun.png"/>
            </el-col>
            <el-col :span="18">
                <span style="font-size:4vh;line-height: 70px;">网络硬盘</span>
            </el-col>
        </el-row>
      </el-header>
      <el-main style="top: 50%;left: 50%;">
        <Y9Card style="width:25vw;height: 20vh;" :title="title">
            <el-row class="rowCss"><b style="color:#685d5d;">请输入文件密码</b></el-row>
            <el-row class="rowCss" :gutter="20">
                <el-col :span="18">
                    <el-input
                        v-model="fileObject.filePassword"
                        class="w-50 m-2"
                        :prefix-icon="Lock"
                        type="password"
                        maxlength="8"
                        show-word-limit
                        show-password
                        clearable
                    />
                </el-col>
                <el-col :span="6">
                    <el-button :size="fontSizeObj.buttonSize"
:style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary">下载文件</el-button>
                </el-col>
            </el-row>
        </Y9Card>
      </el-main>
      <el-footer></el-footer>
    </el-container> -->
    <div class="acss-header" id="zpre3Nn3">
    <div class="header-title">
        <el-row>
            <el-col :span="11">
                <div class="acss_banner"></div>
            </el-col>
            <el-col :span="13">
                <div><span style="font-size:4vh;line-height: 50px;color: var(--el-color-primary);">{{ $t('网络硬盘') }}</span></div>
            </el-col>
        </el-row>
    </div>
    <div class="verify-form">
        <form action="" class="clearfix" name="accessForm" onsubmit="return false">
            <div class="CMxQsC">
            <div class="avatar">
                <div class="photo-frame theme-share-head">
                    <span class="radius-3">
                        <span class="cert-info">{{ $t('直链下载') }}</span>
                    </span>
                </div>
            </div>
            <div class="verify-property">

            </div>
            <div class="cb"></div>
            </div>
            <div class="verify-memo">
                <div class="verify-memo-container">
                    <span class="verify-memo-text"></span>
                </div>
            </div>
            <div class="verify-input ac-close clearfix">
                <dl class="pickpw clearfix">
                    <dt>{{ $t('请输入文件密码：') }}</dt>
                    <dd class="clearfix input-area">
                        <el-row class="rowCss" :gutter="20">
                            <el-col :span="19">
                                <el-input
                                    v-model="filePassword"
                                    class="w-50 m-2"
                                    :prefix-icon="Lock"
                                    type="password"
                                    maxlength="8"
                                    show-word-limit
                                    show-password
                                    clearable
                                />
                            </el-col>
                            <el-col :span="5">
                                <el-button :size="fontSizeObj.buttonSize"
                                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="download">{{ $t('下载文件') }}</el-button>
                            </el-col>
                        </el-row>
                    </dd>
                </dl>
            </div>
        </form>
    </div>
</div>

</template>
<script lang="ts" setup>
import { ref, defineProps, onMounted, watch,computed,reactive, toRefs,nextTick, inject } from 'vue';
import {Lock} from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import FileApi from '@/api/storage/file';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const fontSizeObj: any = inject('sizeObjInfo')||{};
const currentrRute = useRoute();
let data = reactive({
    title: '文件下载',
    filePassword: '',
    tenantId: currentrRute.query.tenantId,
    id: currentrRute.query.id
});

let {
    title,
    filePassword,
    id,
    tenantId
} = toRefs(data);

  onMounted(() => {
    console.log('tenantId',currentrRute.query.tenantId);
    console.log('id',currentrRute.query.id);
});



async function download(){
    if(filePassword.value){
        let res = await FileApi.checkPwd(id.value,tenantId.value,filePassword.value);
        ElMessage({ type: res.data.success?'success':'error', message: res.data.msg, offset: 65 });
        if(res.data.success){
            window.open(import.meta.env.VUE_APP_CONTEXT + "link/df/"+id.value+"/"+tenantId.value, "_blank");
        }
    }else{
        ElMessage({ type: 'error', message: t("请输入文件密码"), offset: 65 });
    }
}
</script>
<style lang="scss" scoped>
@import "@/theme/global.scss";
@import "@/assets/css/linkDownLoad.scss";
.rowCss {
    padding: 10px 0px;
}

body, div, dl, dt, dd, ul, ol, li, h1, h2, h3, h4, h5, h6, pre, code, form, textarea, select, optgroup, option, fieldset, legend, p, blockquote, th, td {
    margin: 0;
    padding: 0;
}
</style>
