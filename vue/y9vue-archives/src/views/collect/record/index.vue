<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-15 17:23:12
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-01 17:35:36
 * @FilePath: \vue\y9vue-archives\src\views\collect\recordList.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <catalogTree ref="catalogTreeRef" :treeApiObj="treeApiObj" @onTreeClick="onTreeClick">
        <template #rightContainer>
            <template v-if="Object.keys(currTreeNodeInfo).length > 0">
                <RecordList :currTreeNodeInfo="currTreeNodeInfo"></RecordList>
            </template>
        </template>
    </catalogTree>
    <!-- <form ref="formRef" :model="formData" :rules="rules">
            <table>
                <tr>
                    <td>
                        <el-form-item label="类别代码" prop="name">
                            <el-select v-model="formData.categoryCode" placeholder="请选择类别代码">
                                <el-option label="文书类" value="WS" />
                                <el-option label="照片类" value="ZP" />
                                <el-option label="录音类" value="LY" />
                                <el-option label="录像类" value="LX" />
                            </el-select>
                        </el-form-item>
                    </td>
                    <td>
                        <el-form-item label="全宗名称" prop="name">
                            <el-input
                                type="number"
                                style="width: 200px; margin-left: 15px"
                                ref="nameSign"
                                v-model="formData.capacitySize"
                                clearable
                            />
                        </el-form-item>
                    </td>
                </tr>
            </table>
        </form> -->
</template>
<script lang="ts" setup>
    import { ref, reactive } from 'vue';
    import RecordList from './recordList.vue';
    import { getCatalogList } from '@/api/archives/collect';
    import { getCatelogTree } from '@/api/archives/catalog';

    const data = reactive({
        catalogTreeRef: '', //tree实例
        treeApiObj: {
            //tree接口对象
            topLevel: getCatelogTree,
            childLevel: {
            //子级（二级及二级以上）tree接口
            api: getCatelogTree,
            params: {
            }
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

    let { catalogTreeRef, treeApiObj, currTreeNodeInfo, formData, rules } = toRefs(data);

    //点击tree的回调
    async function onTreeClick(currTreeNode) {
        // let res = await getProcessDefinitionList(currTreeNode.workflowGuid);
        // if (res.success) {
        //     processDefinitionList.value = res.data;
        //     if (processDefinitionList.value.length > 0) {
        //         maxVersion.value = processDefinitionList.value[0].version;
        //         selectVersion.value = processDefinitionList.value[0].version;
        //         processDefinitionId.value = processDefinitionList.value[0].id;
        //     }
        // }
        currTreeNodeInfo.value = currTreeNode;
    }
</script>
<style lang="scss"></style>
