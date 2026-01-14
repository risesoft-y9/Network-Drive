<template>
    <el-form ref="formRef" :model="form" :rules="rules">
        <el-descriptions border :column="2">
            <el-descriptions-item
                :span="2"
                label-align="center"
            >
                <template #label>
                    需求标题<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="title">
                    <el-input v-model="form.title" maxlength="100" show-word-limit v-if="type === 'add' || type === 'edit'"></el-input>
                    <el-input v-model="form.title" v-else readonly></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                :span="2"
                label-align="center"
            >
                <template #label>
                    需求描述<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="description">
                    <el-input v-model="form.description" type="textarea" rows="4" v-if="type === 'add' || type === 'edit'"></el-input>
                    <el-input v-model="form.description" type="textarea" rows="4" v-else readonly></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                label="联系方式"
                :span="2"
                label-align="center"
            >
                <el-input v-model="form.contact" v-if="type === 'add' || type === 'edit'"></el-input>
                <el-input v-model="form.contact" v-else readonly></el-input>
            </el-descriptions-item>

            <el-descriptions-item
                label="反馈信息"
                :span="2"
                label-align="center"
                v-if="type === 'examine' || type === 'look'"
            >
                <el-input v-model="form.feedback" type="textarea" rows="4" v-if="type === 'examine'"></el-input>
                <el-input v-model="form.feedback" v-else readonly></el-input>
            </el-descriptions-item>
        </el-descriptions>

        <el-form-item style="float: right; margin-top: 15px;" v-if="type !== 'look'">
            <el-button type="primary" @click="submitForm(formRef)">保存</el-button>
            <el-button @click="resetForm(formRef)">重置</el-button>
        </el-form-item>
    </el-form>

    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="saveLoading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { FormInstance, ElNotification, ElMessage } from 'element-plus';
import { saveDemandData } from '@/api/demand';

const emits = defineEmits(['close']);
const props = defineProps({
    entity: {
        type: Object
    },
    type: {
        type: String
    },
});

let formRef = ref<FormInstance>();

const form = ref({
    title: '',
    description: '',
    contact: '',
    feedback: ''
});

let rules = ref({
    title: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }],
    description: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }]
});

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
                saveDemandData(params)
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

onMounted(() => {
    Object.assign(form.value, props.entity);
});
</script>

<style lang="scss" scoped>
.el-form-item {
    margin-bottom: 0px !important;
}

:deep(.el-input__wrapper) {
    box-shadow: none !important;
}

:deep(.el-textarea__inner) {
    box-shadow: none !important;
}
</style>