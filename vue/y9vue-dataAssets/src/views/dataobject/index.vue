<template>
    <sourceTreeModule
        ref="fixedTreeRef"
        :treeApiObj="treeApiObj"
        :showNodeDelete="false"
        :showNodeAdd="false"
        @onNodeClick="onNodeClick"
    >
        <!-- 右边卡片 -->
        <template v-slot:rightContainer v-if="currNode.id">
            <div v-if="currNode.$level === 1">
                <y9Card :title="`${currNode.name}`">
                    <dataSourceType
                        :flxedTree="fixedTreeRef"
                        :currNode="currNode"
                        :changeLoading="changeLoading"
                    ></dataSourceType>
                </y9Card>
            </div>
            <div v-else-if="currNode.$level === 2">
                <y9Card>
                    <template #title>
                        <div class="card-title">
                            {{ currNode.name }}
                            <span class="card-title-status" v-loading="statusLoading">
                                <i class="ri-check-line" v-if="dataStatus"></i>
                                <i class="ri-close-line" v-else></i>
                            </span>
                        </div>
                    </template>
                    <DataSource
                        :flxedTree="fixedTreeRef"
                        :currNode="currNode"
                        :changeLoading="changeLoading"
                    ></DataSource>
                </y9Card>
            </div>
            <div v-else-if="currNode.$level === 3">
                <y9Card>
                    <template #title>
                        <div class="card-title">
                            {{ currNode.name }}
                            <span class="card-title-status">
                                <i class="ri-check-line"></i>
                            </span>
                        </div>
                    </template>
                    <TableData :currNode="currNode" />
                </y9Card>
            </div>
        </template>
    </sourceTreeModule>

    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
    import {
        getDataSourceType,
        getTablesByBaseType,
        searchDataSource,
        checkDataStatus,
    } from '@/api/dataSource';
    import { FormType } from './enums';
    import { inject, ref, reactive } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import DataSource from '@/views/datasource/comps/dataSource/dataForm.vue';
    import dataSourceType from '@/views/datasource/comps/dataSourceType/index.vue';
    import TableData from './comps/tableData.vue';
    import { useI18n } from 'vue-i18n';
    const { t } = useI18n();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');

    const loading = ref(false);
    const changeLoading = (status: boolean) => {
        console.log('status==', status);
        loading.value = status;
    };

    //固定树组件实例
    const fixedTreeRef = ref(null);

    //弹窗组件实例
    const dialogRef = ref(null);

    //tree接口对象
    const treeApiObj = reactive({
        topLevel: getDataSourceType,
        childLevel: {
            api: getTablesByBaseType,
            paramsUseNodeField: {
                category: 'name',
            }
        },
        search: {
            api: searchDataSource,
            searchKeyName: 'name'
        },
        flag: 'dataSource'
    });

    //当前节点数据
    const currNode = ref({});

    

   

    //点击节点时
    const onNodeClick = (node) => {
        currNode.value = node;
        if (currNode.value.$level === 2) {
            getDataStatus();
        }else if (node.type === 3) {
            currNode.value.$level = 3;
        }
    }; 

    let dataStatus = ref();
    let statusLoading = ref(false);
    async function getDataStatus() {
        statusLoading.value = true;
        let result = await checkDataStatus({ sourceId: currNode.value.id });
        if (result.code == 0) dataStatus.value = result.data;
        statusLoading.value = false;
    }
</script>

<style lang="scss" scoped>
    .card-title {
        display: flex;
        align-items: center;
        .card-title-status {
            display: inline-block;
            width: 20px;
            height: 20px;
            color: var(--el-color-white);
            line-height: 20px;
            text-align: center;
            margin-left: 6px;
            > i {
                border-radius: 50%;
            }
            :deep(.el-loading-mask) {
                .el-loading-spinner .circular {
                    width: 25px;
                }
            }
        }
        .ri-check-line {
            background-color: var(--el-color-success);
        }
        .ri-close-line {
            background-color: var(--el-color-danger);
        }
    }
</style>
