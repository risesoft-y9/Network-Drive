<template>
    <el-input
        v-model="query"
        style="padding-bottom: 10px;"
        placeholder="请输入关键词"
        @input="onQueryChanged"
    />
    <el-tree-v2
        ref="treeRef"
        :data="data"
        :props="propsData"
        :filter-method="filterMethod"
        :height="500"
        show-checkbox
        :check-strictly="true"
        :default-checked-keys="checkValue"
        :default-expanded-keys="checkValue"
        :indent="20"
    />
    <el-button class="sbt" type="primary" @click="submitData()">保存</el-button>
</template>

<script lang="ts" setup>
import { getAssetsTable, getTableSelectTree, saveAssetsTable } from '@/api/pretreat';
import { ElNotification, ElMessage } from 'element-plus';
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TreeNodeData, TreeV2Instance } from 'element-plus';

const { t } = useI18n();
const emits = defineEmits(['close']);
const props = defineProps({
    assetsId: {
        type: String,
        default: () => ''
    }
});

const query = ref('');
const treeRef = ref<TreeV2Instance>();
const data = ref([]);
const propsData = {
    value: 'value',
    label: 'label',
    children: 'children',
}
const onQueryChanged = (query: string) => {
    treeRef.value!.filter(query)
}
const filterMethod = (query: string, node: TreeNodeData) => node.label!.includes(query)

const checkValue = ref([]);

const generateData = () => {
    // 获取所有数据表
    getTableSelectTree('table').then((res) => {
        data.value = res.data;
    });
    // 获取已绑定数据
    getAssetsTable(props.assetsId).then((res) => {
        checkValue.value = res.data;
    });
};

onMounted(() => {
    generateData();
});

const submitData = () => {
    const checkedKeys = treeRef.value!.getCheckedKeys();
    if(checkedKeys.length == 0) {
        ElMessage({
            type: 'info',
            message: '请选择数据',
            offset: 65
        });
        return;
    }

    saveAssetsTable(checkedKeys.join(','), props.assetsId).then((res) => {
        ElNotification({
            title: res.success ? t('成功') : t('失败'),
            message: res.msg,
            type: res.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
        if(res.success) {
            emits('close');
        }
    });
}
</script>
<style lang="scss" scoped>
    .sbt {
        float: right;
        margin-top: 15px;
    }
    ::v-deep .el-icon {
        color: #cdd0db !important;
    }
</style>