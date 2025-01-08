<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-15 17:23:12
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-06 17:54:27
 * @FilePath: \vue\y9vue-dataAssets\src\views\collect\recordList.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <catalogTree ref="catalogTreeRef" :treeApiObj="treeApiObj" @onTreeClick="onTreeClick" @onNodeExpand="onNodeExpand">
        <template #rightContainer>
            <template v-if="Object.keys(currTreeNodeInfo).length > 0">
                <RecordList v-if="menuType=='record'" :currTreeNodeInfo="currTreeNodeInfo" :parentCatalog="parentCatalog"/>
                <PreArchingList v-if="menuType=='preArchiving'" :currTreeNodeInfo="currTreeNodeInfo"/>
            </template>
        </template>
    </catalogTree>
</template>
<script lang="ts" setup>
    import { ref, reactive } from 'vue';
    import RecordList from '@/views/library/manage/list.vue';
    import PreArchingList from '@/views/collect/beforehand/preArchingList.vue';
    import { getCatelogTree } from '@/api/dataAssets/catalog';

    const props = defineProps({
        menuType: String
    });

    watch(() => props.menuType,
        (newVal, oldVal) => {
            catalogTreeRef.value.onRefreshTree();
        },
        {deep: true}
    );

    const data = reactive({
        catalogTreeRef: '', //tree实例
        parentCatalog: '',
        treeApiObj: {
            //tree接口对象
            topLevel: getCatelogTree,
            childLevel: {
                //子级（二级及二级以上）tree接口
                api: getCatelogTree
            },
            search: {
                //搜索接口及参数
                api: getCatelogTree,
                params: {}
            }
        },
        currTreeNodeInfo: {}, //当前tree节点的信息
        formData: {
            name: '',
            sex: '',
            age: '',
            birth: '',
            addr: ''
        },
        rules: {
            name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
            sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
            age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
        }
    });

    let { catalogTreeRef, treeApiObj, currTreeNodeInfo, parentCatalog,formData, rules } = toRefs(data);

    //点击tree的回调
    async function onTreeClick(currTreeNode) {
        if(currTreeNode.parentId == null){
            let catalog = currTreeNode.customId;
            parentCatalog.value = catalog;
            sessionStorage.setItem(
                    'parentCustomId',catalog);
        }
        if(currTreeNode.parentId != null && currTreeNode.customId==null){
            parentCatalog.value = sessionStorage.getItem('parentCustomId');
        }
        currTreeNodeInfo.value = currTreeNode;
    }

    async function onNodeExpand(currTreeNode) {
        if(currTreeNode.parentId == null){
            let catalog = currTreeNode.customId;
            parentCatalog.value = catalog;
            sessionStorage.setItem(
                    'parentCustomId',catalog);
        }
        if(currTreeNode.parentId != null && currTreeNode.customId==null){
            parentCatalog.value = sessionStorage.getItem('parentCustomId');
        }
        currTreeNodeInfo.value = currTreeNode;
    }
</script>
<style lang="scss"></style>
