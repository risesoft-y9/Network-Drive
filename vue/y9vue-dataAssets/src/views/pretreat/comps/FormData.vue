<template>
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="y9-form"
    >
        <el-divider content-position="left" border-style="dashed">资产信息</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item>
                <template #label>
                    数据资产名称<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="name">
                    <span v-if="disabled">{{ form.name }}</span>
                    <el-input v-else v-model="form.name"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据资产编码<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="code">
                    <span v-if="disabled">{{ form.code }}</span>
                    <el-input v-else v-model="form.code" :disabled="form.id"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item v-if="form.id" label-align="right" label="全球统一码">
                <el-form-item label="" prop="codeGlobal">
                    <span>{{ form.codeGlobal }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据资产图片"
                :span="2"
            >
                <el-form-item label="" prop="picture">
                    <template v-if="disabled">
                        <el-image
                            style="width: 100px; height: 100px"
                            v-if="form.picture"
                            :src="form.picture"
                            :preview-src-list="[form.picture]"
                        ></el-image>
                        <span v-else> 暂无图片 </span>
                    </template>

                    <UploadAvatar v-else v-model="form.picture" :assetsId="form.id" @setId="setId" name="uploadFile" width="460" height="480">
                    </UploadAvatar>
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
                    <span v-if="disabled">{{ form.dataOwner }}</span>
                    <el-input v-else v-model="form.dataOwner"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据管理员"
            >
                <el-form-item label="" prop="dataManager">
                    <span v-if="disabled">{{ form.dataManager }}</span>
                    <el-input v-else v-model="form.dataManager"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="存储位置"
            >
                <el-form-item label="" prop="dataPath">
                    <span v-if="disabled">{{ form.dataPath }}</span>
                    <el-input v-else v-model="form.dataPath"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据量"
            >
                <el-form-item label="" prop="dataSize">
                    <span v-if="disabled">{{ form.dataSize }}</span>
                    <el-input v-else v-model="form.dataSize"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="更新频率"
            >
                <el-form-item label="" prop="dataFrequency">
                    <span v-if="disabled">{{ form.dataFrequency }}</span>
                    <el-input v-else v-model="form.dataFrequency"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据敏感度"
            >
                <el-form-item label="" prop="dataSensitivity">
                    <span v-if="disabled">{{ form.dataSensitivity }}</span>
                    <el-input v-else v-model="form.dataSensitivity"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="保留期限"
            >
                <el-form-item label="" prop="dataRetain">
                    <span v-if="disabled">{{ form.dataRetain }}</span>
                    <el-input v-else v-model="form.dataRetain"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据质量评分"
            >
                <el-form-item label="" prop="dataQualityScore">
                    <span v-if="disabled">{{ form.dataQualityScore }}</span>
                    <el-input v-else v-model="form.dataQualityScore"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2">
                <template #label>
                    主要用途<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="dataPurpose">
                    <span v-if="disabled">{{ form.dataPurpose }}</span>
                    <el-input type="textarea" v-else v-model="form.dataPurpose"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="关键字段"
            >
                <el-form-item label="" prop="keyField">
                    <span v-if="disabled">{{ form.keyField }}</span>
                    <el-input v-else v-model="form.keyField"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="合规要求"
            >
                <el-form-item label="" prop="dataCompliance">
                    <span v-if="disabled">{{ form.dataCompliance }}</span>
                    <el-input v-else v-model="form.dataCompliance"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据安全等级"
            >
                <el-form-item label="" prop="dataSecurityLevel">
                    <span v-if="disabled">{{ form.dataSecurityLevel }}</span>
                    <el-input v-else v-model="form.dataSecurityLevel"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据价值评估"
            >
                <el-form-item label="" prop="dataValuation">
                    <span v-if="disabled">{{ form.dataValuation }}</span>
                    <el-input v-else v-model="form.dataValuation"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据产品URL"
            >
                <el-form-item label="" prop="dataProductUrl">
                    <span v-if="disabled">{{ form.dataProductUrl }}</span>
                    <el-input v-else v-model="form.dataProductUrl"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据服务URL"
            >
                <el-form-item label="" prop="dataServiceUrl">
                    <span v-if="disabled">{{ form.dataServiceUrl }}</span>
                    <el-input v-else v-model="form.dataServiceUrl"></el-input>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed"> 基础属性</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item
                :span="2"
                label="数据资产摘要"
            >
                <el-form-item label="" prop="remark">
                    <span v-if="disabled">{{ form.remark }}</span>
                    <el-input v-else v-model="form.remark" type="textarea"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据资产格式<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="dataType">
                    <!-- <span v-if="disabled">{{ form.dataType }}</span> -->
                    <el-select :disabled="disabled" v-model="form.dataType">
                        <el-option
                            v-for="item in dataTypeList"
                            :key="item.code"
                            :label="item.name"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    共享类型<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="shareType">
                    <!-- <span v-if="disabled">{{ form.shareType }}</span> -->
                    <el-select :disabled="disabled" v-model="form.shareType">
                        <el-option
                            v-for="item in shareTypeData"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据来源系统"
            >
                <el-form-item label="" prop="dataOriginSystem">
                    <span v-if="disabled">{{ form.dataOriginSystem }}</span>
                    <el-input v-else v-model="form.dataOriginSystem"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="数据资产提供方"
            >
                <el-form-item label="" prop="dataProvider">
                    <span v-if="disabled">{{ form.dataProvider }}</span>
                    <el-input v-else v-model="form.dataProvider"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    数据专区<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="dataZone">
                    <el-select :disabled="disabled" v-model="form.dataZone">
                        <el-option
                            v-for="item in dataZoneList"
                            :key="item.code"
                            :label="item.name"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    产品类型<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="productType">
                    <el-select :disabled="disabled" v-model="form.productType">
                        <el-option
                            v-for="item in productTypeList"
                            :key="item.code"
                            :label="item.name"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    应用场景<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="appScenarios">
                    <el-select :disabled="disabled" v-model="form.appScenarios">
                        <el-option
                            v-for="item in appScenariosList"
                            :key="item.code"
                            :label="item.name"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-form-item style="float: right; margin-top: 15px;" v-if="!disabled">
            <el-button type="primary" @click="submitForm(formRef)">保存</el-button>
            <el-button @click="resetForm(formRef)">重置</el-button>
        </el-form-item>
    </el-form>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="saveLoading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue';
import { FormInstance, Action, ElNotification } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDataCode, saveData } from '@/api/pretreat';
import { getOptionValueList } from '@/api/dataAssets/dictionaryOption';
import y9_storage from '@/utils/storage';
import settings from '@/settings';

const emits = defineEmits(['close']);
const props = defineProps({
    categoryId: {
        type: String
    },
    pCode: {
        type: String
    },
    entity: {
        type: Object,
        default: () => null
    },
    disabled: {
        type: Boolean,
        required: false,
        default: () => {
            return false;
        }
    }
});

let formRef = ref<FormInstance>();

let rules = ref({
    name: [{ required: true, message: '数据资产名称不能为空', trigger: ['change', 'blur'] }],
    code: [{ required: true, message: '数据资产编码不能为空', trigger: ['change', 'blur'] }],
    dataPurpose: [{ required: true, message: '主要用途不能为空', trigger: ['change', 'blur'] }],
    shareType: [{ required: true, message: '共享类型不能为空', trigger: ['change', 'blur'] }],
    dataType: [{ required: true, message: '数据资产格式不能为空', trigger: ['change', 'blur'] }],
    appScenarios: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }],
    dataZone: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }],
    productType: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }]
});

const form = ref({ categoryId: props.categoryId });

let saveLoading = ref(false);
const submitForm = async (formEl: FormInstance | undefined) => {
    return new Promise<void>((resolve, reject) => {
        if (!formEl) {
            reject();
            return;
        }
        formEl.validate((valid) => {
            if (valid) {
                saveLoading.value = true;
                let params: any = Object.assign({}, form.value);
                saveData(params)
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
            } else {
                reject();
            }
        });
    });
}

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.resetFields()
}

const genCode = () => {
    if (form.value.categoryId && !form.value.id) {
        getDataCode({categoryId: form.value.categoryId, pCode: props.pCode}).then((res) => {
            form.value.code = res.data;
        });
    }
};

watch(
    () => props.entity,
    (nv, ov) => {
        if (props.entity) {
            form.value = props.entity;
        }
    },
    { immediate: true }
);

onMounted(() => {
    getDataType();
    if (!props.entity) {
        genCode();
    }
});

const setId = (id) => {
    form.value.id = id;
}

let dataTypeList = ref([]);// 数据格式类型
let appScenariosList = ref([]);// 应用场景
let dataZoneList = ref([]);// 数据专区
let productTypeList = ref([]);// 产品类型
const getDataType = () => {
    getOptionValueList('assetsType').then((res) => {
        dataTypeList.value = res.data;
        // 设置默认选中
        //const itemData = dataTypeList.value.find(item => item.defaultSelected === 1);
        //form.value.dataType = itemData.code;
    });
    getOptionValueList('appScenarios').then((res) => {
        appScenariosList.value = res.data;
    });
    getOptionValueList('dataZone').then((res) => {
        dataZoneList.value = res.data;
    });
    getOptionValueList('productType').then((res) => {
        productTypeList.value = res.data;
    });
};

const shareTypeData = [
    {
        value: 0,
        label: '不予共享',
    },
    {
        value: 1,
        label: '有条件共享',
    },
    {
        value: 2,
        label: '无条件共享',
    }
]

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