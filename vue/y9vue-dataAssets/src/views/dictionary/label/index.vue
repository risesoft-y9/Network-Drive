<template>
    <div>
        <fixedTreeModule
            ref="fixedTreeRef"
            :treeApiObj="treeApiObj"
            nodeLabel="name"
            showNodeDelete=true
            @onDeleteTree="dataCatalogRemove"
            @onTreeClick="handlerTreeClick"
        >
            <template v-slot:treeHeaderRight>
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    type="primary"
                    @click="showAddDialog()"
                >
                    <i class="ri-add-line"></i>
                    <span>{{ $t('分类') }}</span>
                </el-button>
            </template>
            <template v-slot:rightContainer>
                <!-- 右边卡片 -->
                <div v-if="currData.id">
                    <BasicInfo
                        :currTreeNodeInfo="currData"
                        :findNode="findNode"
                        :getTreeData="getTreeData"
                        :getTreeInstance="getTreeInstance"
                        :handClickNode="handClickNode"
                        :postNode="postNode"
                    />

                    <!-- 列表 -->
                    <LabelTable :currTreeNodeInfo="currData" v-if="!currData.isChild" />
                </div>
            </template>
        </fixedTreeModule>

        <y9Dialog v-model:config="dialogConfig">
            <y9Form ref="ruleRef" :config="formSystem"></y9Form>
        </y9Dialog>

        <!-- 制造loading效果 -->
        <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
    </div>
</template>

<script lang="ts" setup>
    import y9_storage from '@/utils/storage';
    import { computed, inject, onMounted, ref } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import {
        dataCatalogTree,
        deleteDataCatalog,
        saveDataCatalog
    } from '@/api/label';
    import BasicInfo from './comps/BasicInfo.vue';
    import LabelTable from './comps/LabelTable.vue';
    import { useI18n } from 'vue-i18n';
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    // loading
    let loading = ref(false);

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
            return dataCatalogTree({parentId: 0});
        }, //顶级（一级）tree接口,
        childLevel: {
            //子级（二级及二级以上）tree接口
            api: dataCatalogTree,
            params: {}
        },
        search: {
            //搜索接口及参数
            api: dataCatalogTree,
            params: {}
        }
    });

    onMounted(() => {
        
    });

    const ruleRef = ref();
    let dataCatalogForm = ref({});
    const formSystem = ref({
        model: dataCatalogForm.value,
        rules: {
            name: [{ required: true, message: computed(() => t('请输入目录名称')), trigger: 'blur' }]
        },
        labelWidth: '120px',
        itemList: [
            {
                type: 'input',
                props: {
                    type: 'text'
                },
                label: t('目录名称'),
                prop: 'name',
                required: true
            },
            {
                type: 'input',
                props: {
                    type: 'text'
                },
                label: t('排列序号'),
                prop: 'tabIndex'
            },
            {
                type: 'textarea',
                props: {
                    type: 'textarea',
                    row: 3
                },
                label: t('描述'),
                prop: 'description'
            }
        ],
        descriptionsFormConfig: {
            labelWidth: '200px',
            labelAlign: 'center'
        }
    });

    let dialogConfig = ref({
        show: false,
        title: '新增数据分类',
        width: '40%',
        onOkLoading: true,
        type: '',
        onOk: (newConfig) => {
            return new Promise(async (resolve, reject) => {
                const ruleFormRef = ruleRef.value.elFormRef;
                if (!ruleFormRef) return;
                await ruleFormRef.validate(async (valid, fields) => {
                    if (valid) {
                        let res = { success: false, msg: '' } as any;

                        let dataCatalog = {
                            ...ruleRef.value.model,
                            level: 1,
                            parentId: '0'
                        };
                        res = await saveDataCatalog(dataCatalog);
                        if (res.success) {
                            fixedTreeRef.value.onRefreshTree();
                        }
                        // 清空表单 数据
                        dataCatalogForm.value = { enabled: true };
                        ElNotification({
                            title: res.success ? t('成功') : t('失败'),
                            message: res.success ? t('保存成功') : res.msg,
                            type: res.success ? 'success' : 'error',
                            duration: 2000,
                            offset: 80
                        });
                        resolve();
                    } else {
                        reject();
                    }
                });
            });
        }
    });

    function showAddDialog() {
        dialogConfig.value.show = true;
    }

    // 删除
    function dataCatalogRemove(data) {
        ElMessageBox.confirm(`${t('是否删除')}【${data.name}】?`, t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
            .then(async () => {
                loading.value = true;
                // 进行 删除 操作 --
                let result = await deleteDataCatalog(data.id);

                if (result.success) {
                    /**
                     * 对树进行操作
                     */
                    //1.删除前，需要手动点击的节点信息，如果有父节点则默认点击父节点，没有则点击tree数据的第一个节点
                    const treeData = getTreeData(); //获取tree数据
                    let clickNode = null;
                    if (data.parentId) {
                        clickNode = findNode(treeData, data.parentId); //找到父节点的信息
                        fixedTreeRef.value?.y9TreeRef?.remove(data); //删除此节点
                    } else if (treeData.length > 0) {
                        fixedTreeRef.value?.y9TreeRef?.remove(data); //删除此节点
                        clickNode = treeData[0];
                    }
                    if (clickNode) {
                        handClickNode(clickNode); //手动设置点击当前节点
                    } else {
                        currData.value = { id: null };
                    }
                }

                loading.value = false;
                ElNotification({
                    message: result.msg,
                    type: result.success ? 'success' : 'error',
                    duration: 2000,
                    offset: 80
                });
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: t('已取消删除'),
                    offset: 65
                });
            });
    }

    //获取tree数据
    function getTreeData() {
        return fixedTreeRef.value.getTreeData();
    }

    //获取树的实例
    function getTreeInstance() {
        return fixedTreeRef.value.y9TreeRef;
    }

    //请求某个节点，返回格式化好的数据
    function postNode(node) {
        return new Promise((resolve, reject) => {
            fixedTreeRef.value.onTreeLazyLoad(node, (data) => {
                resolve(data);
            });
        });
    }

    //在树数据中根据id找到对应的节点并返回
    function findNode(treeData, targetId) {
        return fixedTreeRef.value.findNode(treeData, targetId);
    }

    /**手动点击树节点
     * @param {Boolean} isExpand 是否展开节点
     */
    function handClickNode(node, isExpand?) {
        fixedTreeRef.value?.handClickNode(node, isExpand);
    }
</script>
<style lang="scss" scoped>
    
</style>
