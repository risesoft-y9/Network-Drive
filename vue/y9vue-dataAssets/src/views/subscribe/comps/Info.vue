<template>
    <el-form
        ref="formRef"
        :model="form"
        class="y9-form"
    >
        <el-divider content-position="left" border-style="dashed">资产信息</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item>
                <template #label>
                    数据资产名称
                </template>
                <el-form-item label="" prop="name">
                    <span>{{ form.name }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据资产编码
                </template>
                <el-form-item label="" prop="code">
                    <span>{{ form.code }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item v-if="form.id" label="全球统一码">
                <el-form-item label="" prop="codeGlobal">
                    <span>{{ form.codeGlobal }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据资产图片"
                :span="2"
            >
                <el-form-item label="" prop="picture">
                    <el-image
                        style="width: 100px; height: 100px"
                        v-if="form.picture"
                        :src="form.picture"
                        :preview-src-list="[form.picture]"
                    ></el-image>
                    <span v-else> 暂无图片 </span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                v-if="form.id"
                label="二维码"
                :span="2"
            >
                <el-form-item label="" prop="qrcode">
                    <el-image
                        style="width: 100px; height: 100px"
                        v-if="form.qrcode"
                        :src="form.qrcode"
                        :preview-src-list="[form.qrcode]"
                    ></el-image>
                    <template v-else> - </template>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据所有者"
            >
                <el-form-item label="" prop="dataOwner">
                    <span>{{ form.dataOwner }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据管理员"
            >
                <el-form-item label="" prop="dataManager">
                    <span>{{ form.dataManager }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="存储位置"
            >
                <el-form-item label="" prop="dataPath">
                    <span>{{ form.dataPath }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据量"
            >
                <el-form-item label="" prop="dataSize">
                    <span>{{ form.dataSize }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="更新频率"
            >
                <el-form-item label="" prop="dataFrequency">
                    <span>{{ form.dataFrequency }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据敏感度"
            >
                <el-form-item label="" prop="dataSensitivity">
                    <span>{{ form.dataSensitivity }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="保留期限"
            >
                <el-form-item label="" prop="dataRetain">
                    <span>{{ form.dataRetain }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据质量评分"
            >
                <el-form-item label="" prop="dataQualityScore">
                    <span>{{ form.dataQualityScore }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2">
                <template #label>
                    主要用途
                </template>
                <el-form-item label="" prop="dataPurpose">
                    <span>{{ form.dataPurpose }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="关键字段"
            >
                <el-form-item label="" prop="keyField">
                    <span>{{ form.keyField }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="合规要求"
            >
                <el-form-item label="" prop="dataCompliance">
                    <span>{{ form.dataCompliance }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据安全等级"
            >
                <el-form-item label="" prop="dataSecurityLevel">
                    <span>{{ form.dataSecurityLevel }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据价值评估"
            >
                <el-form-item label="" prop="dataValuation">
                    <span>{{ form.dataValuation }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据产品URL"
            >
                <el-form-item label="" prop="dataProductUrl">
                    <span>{{ form.dataProductUrl }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据服务URL"
            >
                <el-form-item label="" prop="dataServiceUrl">
                    <span>{{ form.dataServiceUrl }}</span>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed">基础属性</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item
                :span="2"
                label="数据资产摘要"
            >
                <el-form-item label="" prop="remark">
                    <span>{{ form.remark }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据资产格式
                </template>
                <el-form-item label="" prop="dataType">
                    <span>{{ form.dataType }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    共享类型
                </template>
                <el-form-item label="" prop="shareTypeName">
                    <span>{{ form.shareTypeName }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据来源系统"
            >
                <el-form-item label="" prop="dataOriginSystem">
                    <span>{{ form.dataOriginSystem }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据资产提供方"
            >
                <el-form-item label="" prop="dataProvider">
                    <span>{{ form.dataProvider }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据专区
                </template>
                <el-form-item label="" prop="dataZone">
                    <span>{{ form.dataZone }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    产品类型
                </template>
                <el-form-item label="" prop="productType">
                    <span>{{ form.productType }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    应用场景
                </template>
                <el-form-item label="" prop="appScenarios">
                    <span>{{ form.appScenarios }}</span>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed">订阅信息</el-divider>
        <el-descriptions border>
            <el-descriptions-item>
                <template #label>
                    数据提供方式
                </template>
                <el-form-item label="" prop="provideType">
                    <span v-if="type == 3 || (type == 2 && reviewStatus != '待审核')">{{ provideType }}</span>
                    <el-select v-model="provideType" clearable v-else>
                        <el-option
                            v-for="item in provideTypeList"
                            :key="item.code"
                            :label="item.name"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    用途说明
                </template>
                <el-form-item label="" prop="purpose">
                    <span v-if="type == 3 || (type == 2 && reviewStatus != '待审核')">{{ purpose }}</span>
                    <el-input v-model="purpose" type="textarea" :autosize="{ minRows: 2 }" v-else></el-input>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed" v-if="type != 1">审核信息</el-divider>
        <el-descriptions border v-if="type != 1">
            <el-descriptions-item>
                <template #label>
                    审核结果
                </template>
                <el-form-item label="" prop="reviewStatus">
                    <el-select v-model="reviewStatus" v-if="type == 3">
                        <el-option label="通过" value="通过" />
                        <el-option label="不通过" value="不通过" />
                    </el-select>
                    <span v-else>{{ reviewStatus }}</span>
                </el-form-item>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    备注
                </template>
                <el-form-item label="" prop="reason">
                    <el-input
                        placeholder="提供方式为应用地址审核通过时需填写访问地址和访问账号密码，为线下交流审核通过时需填写相关信息。审核不通过时不能为空，请填写不通过原因"
                        v-model="reason"
                        type="textarea"
                        :autosize="{ minRows: 2 }"
                        v-if="type == 3"
                    ></el-input>
                    <span v-else>{{ reason }}</span>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed" v-if="provideType == '库表推送' && userRole == 'systemAdmin'">推送信息</el-divider>
        <el-descriptions border v-if="provideType == '库表推送' && userRole == 'systemAdmin'" :column="2">
            <el-descriptions-item :span="2">
                <template #label>
                    连接信息
                </template>
                <el-form-item label="">
                    <span>{{ baseForm.url }}</span>
                </el-form-item>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    用户名
                </template>
                <el-form-item label="">
                    <span>{{ baseForm.username }}</span>
                </el-form-item>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    密码
                </template>
                <el-form-item label="">
                    <span>{{ baseForm.password }}</span>
                </el-form-item>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    备注
                </template>
                <el-form-item label="">
                    <span>{{ baseForm.remark }}</span>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-form-item style="float: right; margin-top: 20px;" v-if="type != 2 || (type == 2 && reviewStatus == '待审核')">
            <el-button type="primary" @click="submitForm(formRef)">提交</el-button>
        </el-form-item>
    </el-form>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="saveLoading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue';
import { FormInstance, Action, ElNotification } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDataById, getSubscribeBaseById, getSubscribeById, reviewData, saveSubscribe } from '@/api/pretreat';
import { getOptionValueList } from '@/api/dataAssets/dictionaryOption';
import y9_storage from '@/utils/storage';
import settings from '@/settings';

const emits = defineEmits(['close']);
const props = defineProps({
    id: {
        type: String
    },
    type: {
        type: String
    },
    subscribeId: {
        type: String,
        default: () => {
            return '';
        }
    }
});

const userRole = sessionStorage.getItem('userRole');

let provideType = ref('');// 提供方式
let purpose = ref('');// 用途说明
let reviewStatus = ref('');// 审核结果
let reason = ref('');// 备注

let formRef = ref<FormInstance>();
const form = ref({});

let saveLoading = ref(false);
const submitForm = async (formEl: FormInstance | undefined) => {
    return new Promise<void>((resolve, reject) => {
        if (!formEl) {
            reject();
            return;
        }
        formEl.validate((valid) => {
            if (valid) {
                if(props.type == '1' || props.type == '2') {// 提交订阅信息
                    saveLoading.value = true;
                    let params: any = Object.assign({'provideType': provideType.value, 'purpose': purpose.value, 
                    'assetsId': form.value.id, 'id': props.subscribeId});
                    saveSubscribe(params)
                        .then((res: any) => {
                            saveLoading.value = false;
                            if (res.success) {
                                emits('close');
                            }
                            ElNotification({
                                title: res.success ? '成功' : '失败',
                                message: res.msg,
                                type: res.success ? 'success' : 'error',
                                duration: 2000,
                                offset: 80
                            });
                            resolve();
                        })
                        .catch((e: any) => {
                            saveLoading.value = false;
                            ElMessage.error('程序出错:' + e);
                            reject();
                        });
                } else if(props.type == '3') {// 提交审核信息
                    review();
                }
            } else {
                reject();
            }
        });
    });
}

async function review() {
    if(reviewStatus.value == '不通过' && reason.value == '') {
        ElNotification({
            title: '失败',
            message: "不通过，必须填写原因",
            type: 'error',
            duration: 2000,
            offset: 80
        });
        return;
    }else {
        saveLoading.value = true;
        let params = {
            reviewStatus: reviewStatus.value,
            reason: reason.value,
            id: props.subscribeId
        };

        let result = await reviewData(params);
        if (result.success) {
            emits('close');
        }
        saveLoading.value = false;
        ElNotification({
            title: result.success ? '成功' : '失败',
            message: result.msg,
            type: result.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
    }
}

watch(
    () => props.id,
    async (new_, old_) => {
        if (new_ && new_ !== old_) {
            getData();
        }
    }
);

onMounted(() => {
    getData();
});

let provideTypeList = ref([]);
async function getData() {
    let res = await getDataById(props.id);
    if(res.success) {
        form.value = res.data;
        getOptionValueList('provideType').then((res) => {
            provideTypeList.value = res.data.filter(item => form.value.provideType.indexOf(item.code) != -1);
        });
        // 获取审核与订阅信息
        if(props.type != '1') {
            let res2 = await getSubscribeById(props.subscribeId);
            if(res2.success) {
                provideType.value = res2.data.provideType;
                purpose.value = res2.data.purpose;
                if(props.type == '3' && res2.data.reviewStatus == '待审核') {
                    reviewStatus.value = '通过';
                } else {
                    reviewStatus.value = res2.data.reviewStatus
                }
                reason.value = res2.data.reason == null ? '' : res2.data.reason;

                if(userRole == 'systemAdmin' && provideType.value == '库表推送') {
                    getSubscribeBase();
                }
            }
        }
    }
}

const baseForm = ref({});
// 根据id获取订阅-库表推送信息
async function getSubscribeBase() {
    let result = await getSubscribeBaseById(props.subscribeId);
    if (result.success) {
        baseForm.value = result.data;
    }
}

</script>

<style lang="scss" scoped>
.el-form-item {
    margin-bottom: 0px !important;
}

:deep(.y9-form) {
    .el-descriptions__body {
        tr {
            display: flex;

            .el-descriptions__label {
                width: 150px;
                vertical-align: middle;
                line-height: 32px;
            }

            .el-descriptions__content {
                flex: 1;
            }

            .el-select {
                width: 100% !important;
            }
        }
    }
}
</style>