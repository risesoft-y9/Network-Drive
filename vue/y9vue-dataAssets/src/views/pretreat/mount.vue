<template>
    <div>
        <fixedTreeModule
            ref="fixedTreeRef"
            :treeApiObj="treeApiObj"
            nodeLabel="name"
            @onTreeClick="handlerTreeClick"
        >
            <template v-slot:rightContainer>
                <!-- 右边卡片 -->
                <div v-if="currData.id">
                    <!-- 列表 -->
                    <MountTable :currTreeNodeInfo="currData" />
                </div>
            </template>
        </fixedTreeModule>
    </div>
</template>

<script lang="ts" setup>
    import y9_storage from '@/utils/storage';
    import { computed, inject, onMounted, ref } from 'vue';
    import {
        dataCatalogTree,
        searchCatelogTree
    } from '@/api/pretreat';
    import MountTable from './comps/MountTable.vue';
    import { useI18n } from 'vue-i18n';
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    // 点击树节点 对应数据的载体
    let currData: any = ref({ id: null });

    // 节点的 基本信息 获取
    function handlerTreeClick(data) {
        // 将拿到的节点信息 储存起来
        currData.value = data;
    }

    // 树 ref
    const fixedTreeRef = ref();
    // 树的一级 子级的请求接口函数
    const treeApiObj = ref({
        topLevel: async () => {
            return dataCatalogTree({parentId: null});
        }, //顶级（一级）tree接口,
        childLevel: {
            //子级（二级及二级以上）tree接口
            api: dataCatalogTree,
            params: {}
        },
        search: {
            //搜索接口及参数
            api: searchCatelogTree,
            params: {}
        }
    });
</script>
<style lang="scss" scoped>
    
</style>
