<template>
    <el-row class="rowCss">
        <el-input
            v-model="file.name"
            class="w-50 m-2"
            :prefix-icon="Document"
            :readonly="true"
        />
    </el-row>
    <el-row class="rowCss">
        <el-input
            v-model="file.createTime"
            class="w-50 m-2"
            :prefix-icon="Calendar"
            :readonly="true"
        />
    </el-row>
    <el-row class="rowCss">
        <el-input
            v-model="file.fileSize"
            class="w-50 m-2"
            :prefix-icon="Coin"
            :readonly="true"
        />
    </el-row>
    <el-row class="rowCss">
        <el-radio-group v-model="file.encryption" @change="changePwd">
            <el-radio :label="true" border>{{ $t('加密') }}</el-radio>
            <el-radio :label="false" border>{{ $t('不加密') }}</el-radio>
        </el-radio-group>
    </el-row>
    <el-row class="rowCss">
        <el-input @click="selectlink"
            ref="linkRef"
            v-model="file.fileUrl"
            class="w-50 m-2"
            :prefix-icon="Link"
            :readonly="true"
        />
    </el-row>
    <el-row v-if="file.encryption" class="rowCss" :gutter="10">
        <el-col :span="19">
            <el-input
            v-model="file.linkPassword"
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
                       :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="submitPwd('save')">{{ $t('提交') }}</el-button>
        </el-col>
    </el-row>

</template>
<script lang="ts" setup>
import { ref, defineProps, onMounted, watch,reactive, toRefs,nextTick, inject } from 'vue';
import {Document ,Calendar, Coin ,Link,Lock,Unlock} from '@element-plus/icons-vue';
import FileApi from '@/api/storage/file';
import type{ ElMessage } from 'element-plus';
import { useStorageStore } from "@/store/modules/storageStore";
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const fontSizeObj: any = inject('sizeObjInfo')||{};
const storageStore = useStorageStore();
const props = defineProps({
    fileObject: {//表格配置
        type: Object,
        default: () => { return {} }
    },
  })

let data = reactive({
    linkUrl: props.fileObject.fileUrl,
    file: props.fileObject,
    linkRef:''
});

let {
    linkUrl,
    file,
    linkRef
} = toRefs(data);

watch(
	  () => props.fileObject,
	  (newVal) => {
		file.value = newVal;
        if(newVal.encryption){
            file.value.fileUrl = import.meta.env.VUE_APP_HOST_INDEX + "link?id="+newVal.id+"&tenantId="+storageStore.tenantId;
        }
	  },
	  {
	    deep: true,
	    immediate: true,
	  }
	)

function changePwd(val){
    props.fileObject.encryption = val;
    if(val){
        props.fileObject.fileUrl = import.meta.env.VUE_APP_HOST_INDEX + "link";
    }else{
        props.fileObject.fileUrl = linkUrl.value;
        file.value.linkPassword = '';
        submitPwd('clear');
    }
}

function selectlink(){
    nextTick(()=>{
        linkRef.value.focus()
        linkRef.value.select()
	})
}

function submitPwd(type){
    if(file.value.linkPassword){
        FileApi.setLinkPwd(file.value.id,file.value.encryption,file.value.linkPassword).then(res => {
            if(type=='save'){
                ElMessage({ type: res.success?'success':'error', message: res.msg, offset: 65 });
            }
        });
    }else{
        ElMessage({ type: 'error', message: t("请输入文件密码"), offset: 65 });
    }

}
</script>
<style lang="scss" scoped>
@import "@/theme/global.scss";
.rowCss {
    padding: 10px 0px;
}
</style>
