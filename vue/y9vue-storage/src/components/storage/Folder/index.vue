<template>
    <el-tabs v-model="activeName" class="folder-tabs">
    <el-tab-pane :label="$t('设置密码')" name="setPwd">
      <template #label>
        <span class="custom-tabs-label">
          <el-icon style="vertical-align: middle;"><i class="ri-lock-2-line"></i></el-icon>
          <span>{{ $t('设置密码') }}</span>
        </span>
      </template>
      <el-form ref="pwdForm" :model="pwdData" :rules="pwdRules" :inline="true" v-if="fileObject.filePassword==null||fileObject.filePassword==''">
        <el-form-item prop="password" class="rowCss" >
            <el-input
                v-model="pwdData.password"
                class="w-50 m-2"
                :prefix-icon="Lock"
                :placeholder="$t('请输入密码')"
                show-password
            />
        </el-form-item>
        <el-form-item prop="repeatPwd" class="rowCss">
            <el-input
            v-model="pwdData.repeatPwd"
            class="w-50 m-2"
            :prefix-icon="Lock"
            :placeholder="$t('请再次输入密码')"
            show-password
                />
        </el-form-item>
        <el-row class="rowCss" style="text-align: center;">
            <el-col :span="6"></el-col>
            <el-col :span="12">
                <el-button :size="fontSizeObj.buttonSize"
                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="submitPwd(pwdForm)">{{ $t('提交') }}</el-button>
                <el-button :size="fontSizeObj.buttonSize"
                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-secord" @click="dialogObj.show=false">{{ $t('取消') }}</el-button>
            </el-col>
        </el-row>
      </el-form>
      <el-form v-else>
        <el-empty :image="imgUrl" :image-size="50" :description="$t('当前文件夹已设置密码')"></el-empty>
      </el-form>
    </el-tab-pane>
    <el-tab-pane :label="$t('取消密码')" name="cancelPwd">
      <template #label>
        <span class="custom-tabs-label">
          <el-icon style="vertical-align: middle;"><i class="ri-lock-unlock-line"></i></el-icon>
          <span>{{ $t('取消密码') }}</span>
        </span>
      </template>
      <el-form ref="cancelPwdForm" :model="cancelPwdData" :rules="pwdRules">
        <el-form-item prop="initPassword" class="rowCss">
                <el-input
                    v-model="cancelPwdData.initPassword"
                    class="w-50 m-2"
                    :prefix-icon="Unlock"
                    :placeholder="$t('请输入原始密码')"
                    show-password
                />
            </el-form-item>
            <el-row class="rowCss" style="text-align: center;margin-top:10px;">
                <el-col :span="6"></el-col>
                <el-col :span="12">
                    <el-button :size="fontSizeObj.buttonSize"
                               :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="cancelPwd(cancelPwdForm)">{{ $t('确认') }}</el-button>
                    <el-button :size="fontSizeObj.buttonSize"
                               :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-secord" @click="dialogObj.show=false">{{ $t('取消') }}</el-button>
                </el-col>
            </el-row>
        </el-form>
    </el-tab-pane>
    <el-tab-pane :label="$t('重置密码')" name="repeatPwd">
      <template #label>
        <span class="custom-tabs-label">
          <el-icon style="vertical-align: middle;"><i class="ri-lock-password-line"></i></el-icon>
          <span>{{ $t('重置密码') }}</span>
        </span>
      </template>
      <el-form ref="resetPwdForm" :model="resetPwdData" :rules="pwdRules">
        <el-form-item prop="newPassword" class="rowCss">
            <el-input
            v-model="resetPwdData.newPassword"
            class="w-50 m-2"
            :prefix-icon="Lock"
            :placeholder="$t('请输入新密码')"
            show-password
                />
        </el-form-item>

        <el-form-item prop="repeatNewPwd" class="rowCss">
            <el-input
            v-model="resetPwdData.repeatNewPwd"
            class="w-50 m-2"
            :prefix-icon="Lock"
            :placeholder="$t('请再次输入新密码')"
            show-password
            />
        </el-form-item>
        <el-row class="rowCss" style="text-align: center;">
            <el-col :span="6"></el-col>
            <el-col :span="12">
                <el-button :size="fontSizeObj.buttonSize"
                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="resetPwd(resetPwdForm)">{{ $t('重置') }}</el-button>
                <el-button :size="fontSizeObj.buttonSize"
                           :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-secord" @click="dialogObj.show=false">{{ $t('取消') }}</el-button>
            </el-col>
        </el-row>
      </el-form>
    </el-tab-pane>
  </el-tabs>
</template>
<script lang="ts" setup>
import { ref, defineProps,onMounted, watch,reactive, toRefs,nextTick, inject } from 'vue';
import {Lock,Unlock} from '@element-plus/icons-vue';
import FileApi from '@/api/storage/file';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
import type{ ElMessage } from 'element-plus';
import { useStorageStore } from "@/store/modules/storageStore";
const fontSizeObj: any = inject('sizeObjInfo')||{};
const storageStore = useStorageStore();
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

const checkRepeatPwd = (rule, value, callback) => {
    if(value!=pwdData.value.password){
        return callback(new Error(t("两次输入的密码不一致，请重新输入！")));
    } else {
		callback();
	}
}
const imgUrl = new URL('../../../assets/images/lock.png', import.meta.url).href;
let data = reactive({
    cancelPwdForm:'',
    cancelPwdData:{},
    pwdForm:'',
    pwdData:{},
    pwdRules:{
        password:{
            required: true, message: t("请输入密码"), trigger: 'blur'
        },
        repeatPwd:[{
            required: true, message: t("请再次输入密码"), trigger: 'blur'
        },{
            validator: checkRepeatPwd, trigger: "blur"
        }],
        newPassword:{
            required: true, message: t("请输入新密码"), trigger: 'blur'
        },
        repeatNewPwd:{
            required: true, message: t("请再次输入新密码"), trigger: 'blur'
        },
        initPassword:{
            required: true, message: t("请输入原始密码"), trigger: 'blur'
        }
    },
    resetPwdForm:'',
    resetPwdData:{},
    activeName:'setPwd',
    initPassword:'',
});

let {
    cancelPwdForm,
    cancelPwdData,
    pwdForm,
    pwdData,
    pwdRules,
    resetPwdForm,
    resetPwdData,
    activeName,
    initPassword,
} = toRefs(data);

function submitPwd(form){
  if(!form) return;
  form.validate(valid => {
    if (valid) {
        let node = {};
        node.id = props.fileObject.id;
        node.filePassword = pwdData.value.repeatPwd;
        FileApi.setFolderPassword(node).then(res => {
            ElMessage({ type: res.success?'success':'error', message: res.msg, offset: 65 });
            if(res.success){
                props.dialogObj.show = false;
                props.reloadTable();
            }
        });
    }else{
        ElMessage({ type: 'error', message: t("文件夹密码设置失败！"), offset: 65 });
    }
  });
}

function cancelPwd(form){
    if(!form) return;
  form.validate(valid => {
    if (valid) {
        let node = {};
        node.id = props.fileObject.id;
        node.filePassword = cancelPwdData.value.initPassword;
        FileApi.cancelFolderPassword(node).then(res => {
            ElMessage({ type: res.success?'success':'error', message: res.msg, offset: 65 });
            if(res.success){
                props.dialogObj.show = false;
                props.reloadTable();
            }
        });
    }else{
        ElMessage({ type: 'error', message: t("文件夹密码取消失败！"), offset: 65 });
    }
  });
}

function resetPwd(resetPwdForm){
  if(!resetPwdForm) return;
  resetPwdForm.validate(valid => {
    if (valid) {
        // let node = {};
        // node.id = props.fileObject.id;
        // node.filePassword = resetPwdData.value.newPassword;
        // FileApi.checkFolderPassword(node).then(res => {
        //     if(res.success){
                let folderNode = {};
                folderNode.id = props.fileObject.id;
                folderNode.filePassword = resetPwdData.value.repeatNewPwd;
                FileApi.resetFolderPassword(folderNode).then(res => {
                    ElMessage({ type: res.success?'success':'error', message: res.msg, offset: 65 });
                    if(res.success){
                        props.dialogObj.show = false;
                        props.reloadTable();
                    }
                });
        //     } else {
        //         ElMessage({ type: 'error', message: res.msg, offset: 65 });
        //     }
        // });
    }else{
        ElMessage({ type: 'error', message: t("文件夹密码重置失败！"), offset: 65 });
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

.folder-tabs {
    :deep(.el-tabs__content){
     height: 160px;
    }

    .el-form-item {
        margin-bottom: 16px !important;
    }
}

</style>
