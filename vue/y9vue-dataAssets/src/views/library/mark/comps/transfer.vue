<template>
    <el-transfer
        v-model="checkValue"
        filterable
        filter-placeholder="请输入搜索内容"
        :data="data"
        :titles="title"
    />
    <el-button class="sbt" type="primary" @click="submitData()">保存</el-button>
</template>

<script lang="ts" setup>
import { getLabelDataList, getLabels, saveAssetsLabel } from '@/api/label';
import { ElNotification } from 'element-plus';
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const emits = defineEmits(['close']);
const props = defineProps({
    entity: {
        type: Object,
        default: () => null
    }
});

const title = ref(['可选列表', '已选列表']);
const data = ref([]);
const checkValue = ref([]);

const generateData = () => {
    // 获取所有标签
    getLabelDataList().then((res) => {
        data.value = res.data;
    });
    // 获取已绑定标签
    getLabels({assetsId: props.entity.id}).then((res) => {
        checkValue.value = res.data;
    });
};

onMounted(() => {
    generateData();
});

const submitData = () => {
    saveAssetsLabel(checkValue.value, props.entity.id).then((res) => {
        if(res.success) {
            emits('close');
        }
        ElNotification({
            title: res.success ? t('成功') : t('失败'),
            message: res.msg,
            type: res.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
    });
}
</script>
<style lang="scss" scoped>
    .sbt {
        float: right;
        margin-top: 15px;
    }
    .el-transfer {
        --el-transfer-panel-width: 300px;
        --el-transfer-panel-body-height: 418px;
    }
    ::v-deep .el-icon {
        color: #cdd0db !important;
    }
</style>