<template>
      <el-form class="decrypt" ref="decryptPwdForm" :model="decryptPwdData" :rules="pwdRules" :inline="true">

            <el-form-item prop="initPassword">
                <el-row style="text-align: center;">
                    <el-col :span="20">
                        <el-input
                            v-model="decryptPwdData.initPassword"
                            class="w-50 m-2"
                            :prefix-icon="Unlock"
                            :placeholder="$t('请输入密码打开文件夹')"
                            show-password
                        />
                    </el-col>
                    <el-col :span="4">
                        <el-button :size="fontSizeObj.buttonSize"
                                   :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="decryptPwd(decryptPwdForm)">{{ $t('解密') }}</el-button>
                    </el-col>
                </el-row>
            </el-form-item>

            <!-- <el-form-item prop="initPassword">
                <el-button :size="fontSizeObj.buttonSize"
:style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="decryptPwd(decryptPwdForm)">解密</el-button>
            </el-form-item> -->
            <!-- <el-row class="rowCss" style="text-align: center;">
                <el-col :span="6"></el-col>
                <el-col :span="12">
                    <el-button :size="fontSizeObj.buttonSize"
:style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="decryptPwd(decryptPwdForm)">确认</el-button>
                    <el-button :size="fontSizeObj.buttonSize"
:style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-secord" @click="dialogObj.show=false">取消</el-button>
                </el-col>
            </el-row> -->
        </el-form>
</template>
<script lang="ts" setup>
import { ref, defineProps,onMounted, watch,reactive, toRefs,nextTick, inject } from 'vue';
import {Lock,Unlock} from '@element-plus/icons-vue';
import FileApi from '@/api/storage/file';
import type{ ElMessage } from 'element-plus';
const fontSizeObj: any = inject('sizeObjInfo')||{};
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const props = defineProps({
    fileObject: {//表格配置
        type: Object,
        default: () => { return {} }
    },
    dialogObj: {
        type: Object,
        default: () => { return {} }
    },
    reloadTable: Function
  })

let data = reactive({
    decryptPwdForm:'',
    decryptPwdData:{},
    pwdRules:{
        initPassword:{
            required: true, message: t("请输入密码"), trigger: 'blur'
        }
    },
});

let {
    decryptPwdForm,
    decryptPwdData,
    pwdRules,
} = toRefs(data);

const emits = defineEmits(['openFolder'])
function decryptPwd(form){
    if(!form) return;
  form.validate(valid => {
    if (valid) {
        let node = {};
        node.id = props.fileObject.id;
        node.filePassword = decryptPwdData.value.initPassword;
        FileApi.decryptPassword(node).then(res => {
            if(res.success){
                props.dialogObj.show = false;
                emits('openFolder',props.fileObject);
            }else{
                ElMessage({ type: 'error', message: res.msg, offset: 65 });
            }
        });
    }else{
        ElMessage({ type: 'error', message: t("文件夹密码错误！"), offset: 65 });
    }
  });
}

</script>
<style lang="scss" scoped>
@import "@/theme/global.scss";
.rowCss {
    padding: 10px 0px 0px 0px;
    width: 100%;
}

.decrypt {
    padding: 10px 0px 0px 5px;
    :deep(.el-tabs__content){
     height: 160px;
    }

    .el-form-item {
        margin-bottom: 16px !important;
    }
    :deep(.y9-dialog-header-title){
        font-size: v-bind('fontSizeObj.mediumFontSize');
    }
}

</style>
