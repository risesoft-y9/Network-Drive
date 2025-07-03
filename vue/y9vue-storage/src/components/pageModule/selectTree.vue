<template>
    <slot v-if="showHeader" name="header">
        <y9Filter :filtersValueCallBack="filtersValueCallBack" :itemList="filterItemList" showBorder></y9Filter>
        <!-- 	<div class="select-tree-header">
                
                <div class="header-left">
                    
    
                    
                    <el-input
                        v-model="searchTreeStr"
                        size="small"
                        class="w-50 m-2"
                        placeholder="请输入"
                        autocomplete="on"
                        clearable
                        @keyup.enter="onSearch"
                        @input="onSearchChange" 
                    >
                        <template #suffix>
                            <i @click="onSearch" class="ri-search-line"></i>
                        </template>
                    </el-input>
                </div>
                    
                <div class="header-right">
                    
                    <slot name="headerRight">
                        
                    </slot>
                
                </div>
            </div> -->
    </slot>

    <y9Tree :isExpandAll="isExpandAllTree" :setting="selectTreeStore.getSelectTreeSetting"></y9Tree>
</template>

<script lang="ts" setup>
    import { useSelectTreeStore } from '@/store/modules/selectTreeStore';
    import { $dataType, $deepAssignObject, $deeploneObject } from '@/utils/object'; //工具类
    import { useCssModule } from 'vue';

    const props = defineProps({
        treeApiObj: {
            //tree接口对象,参数名称请严格按照以下注释进行传参。
            /**
         {
         topLevel:()=>{//顶级（一级）tree接口
         return treeInterface(params)
         },
         childLevel:{//子级（二级及二级以上）tree接口
         api:getTreeItemById,
         params:{}
         },
         search: {//搜索接口及参数
         api:getTreeItemById,
         params:{}
         }
         }
         */

            type: Object
        },

        selectField: {
            //设置需要选择的字段
            type: Array,
            default: () => {
                return [
                    {
                        fieldName: 'orgType',
                        value: ['Organization', 'Person', 'Position']
                    },
                    {
                        fieldName: 'disabled',
                        value: false
                    }
                ];
            }
        },

        selectedData: {
            //选择的数据，可以通过双向绑定的方式获取选择的数据
            type: [Array, Object]
        },

        setting: {
            //tree的配置
            type: Object
        },

        showHeader: {
            //是否显示头部
            type: Boolean,
            default: true
        }
    });

    const emits = defineEmits(['onTreeClick', 'update:onCheckBox', 'change']);

    const data = reactive({
        filterItemList: [
            //过滤配置
            {
                type: 'search',
                key: 'name',
                value: '',
                span: 12
            }
        ],
        filterValue: {}, //当前过滤值

        searchTreeStr: '', //搜索tree关键词

        currTreeNodeInfo: {}, //当前tree节点的信息

        isExpandAllTree: false, //是否展开tree

        newSelectedData: []
    });

    let { filterItemList, filterValue, y9TreeRef, searchTreeStr, currTreeNodeInfo, isExpandAllTree, newSelectedData } =
        toRefs(data);

    // 初始化selectTreeStore
    let selectTreeStore = useSelectTreeStore();

    selectTreeStore.$patch((state) => {
        //初始化配置
        state.selectTreeSetting = $deeploneObject(state.initSelectTreeSetting);
        state.selectTreeSetting.style.li.activeClassName = useCssModule('classes').active;

        if (props.treeApiObj) {
            if (props.treeApiObj.childLevel) {
                state.selectTreeSetting.itemInterface.api = props.treeApiObj.childLevel.api; //子级tree接口

                state.selectTreeSetting.itemInterface.params = props.treeApiObj.childLevel.params; //子级tree接口参数
            }

            if (props.treeApiObj.search) {
                //搜索

                state.selectTreeSetting.events.search.api = props.treeApiObj.search.api; //搜索api
                state.selectTreeSetting.events.search.params = props.treeApiObj.search.params; //搜索参数
                state.selectTreeSetting.events.search.callback = onSearchResultCallback; //搜索结果回调
            }
        }

        state.selectTreeSetting.itemInfo.render.click.func = onTreeClick; //点击树

        state.selectTreeSetting.itemInfo.render.checkbox.func = onCheckBox; //点击复选框

        state.selectTreeSetting.itemInterfaceCallBack = treeInterfaceCallBack; // 二（三）级节点接口 数据的回调函数
        state.selectTreeSetting.topLevelInterfaceCallBack = treeInterfaceCallBack; // 一）级节点接口 数据的回调函数
    });

    //初始化tree
    async function initTree() {
        try {
            props.treeApiObj && (await selectTreeStore.getTree(props.treeApiObj.topLevel)); //获取一级tree数据

            if (selectTreeStore.getSelectTreeSetting.data.length > 0) {
                // onTreeClick({ dataset: JSON.stringify(selectTreeStore.getSelectTreeSetting.data[0]) });//默认选中第一项

                // //默认给第一项添加选中样式
                const hashClassName = selectTreeStore.$state.selectTreeSetting.style.li.activeClassName;

                const treeId = selectTreeStore.$state.selectTreeSetting.treeId;

                document.getElementById(treeId).childNodes[1].childNodes[0].className += ' ' + hashClassName;

                // 默认展开第一个子节点
                Array.from(document.getElementById(treeId).childNodes[1].childNodes[0].children).map((item) => {
                    if (item.dataset && item.dataset.itemEvent === 'clickDropDownEvent') {
                        return item.click();
                    }
                });
            }
        } catch (err) {
            console.log(err);
        }
    }

    initTree(); //初始化tree

    watch(
        () => props.selectedData,
        (newVal) => {
            if (newVal) {
                newSelectedData.value = newVal;
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    watch(
        () => props.setting,
        (newVal) => {
            if (newVal) {
                selectTreeStore.$state.selectTreeSetting = $deepAssignObject(
                    selectTreeStore.$state.selectTreeSetting,
                    newVal
                ); //深度合并
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    //过滤值回调
    function filtersValueCallBack(filters) {
        filterValue.value = filters;
    }

    //监听过滤值改变，请求搜索接口
    watch(
        () => filterValue.value,
        (newVal, oldVal) => {
            if (newVal.name && newVal.name != '' && props.treeApiObj) {
                selectTreeStore.$state.selectTreeSetting.events.search.params.key = newVal.name;
            } else if (newVal.name != oldVal.name) {
                selectTreeStore.$state.selectTreeSetting.events.search.params.key = '';

                onRefreshTree();
            }
        },
        {
            deep: true
        }
    );

    //刷新Tree
    function onRefreshTree() {
        isExpandAllTree.value = false;

        initTree();
    }

    //搜索结果回调
    function onSearchResultCallback(data) {
        selectTreeStore.$patch((state) => {
            const deepTraversal = async (itemList) => {
                itemList.forEach((element) => {
                    if (element.orgType == 'Person') {
                        element.title_icon = 'ri-women-line';
                        if (element.sex == 1) {
                            element.title_icon = 'ri-men-line';
                        }
                        element.checkbox = true;
                    } else if (element.orgType == 'Department') {
                        element.title_icon = 'ri-slack-line';
                    } else if (element.orgType == 'Position') {
                        element.title_icon = 'ri-shield-user-line';
                        element.checkbox = true;
                    } else if (element.orgType == 'Organization') {
                        element.title_icon = 'ri-stackshare-line';
                    }
                    if (element.children) {
                        deepTraversal(element.children);
                    }
                });
            };
            deepTraversal(data);
            state.selectTreeSetting.data = diguiDataAddCheck(data);
        });
    }

    //递归tree数据，加上check复选框
    const diguiDataAddCheck = (data) => {
        for (let i = 0; i < data.length; i++) {
            const item = data[i];
            const result = props.selectField.every((item2) => {
                if ($dataType(item2.value) == 'array') {
                    return item2.value.includes(item[item2.fieldName]);
                } else {
                    return item[item2.fieldName] == item2.value;
                }
            });

            if (result) {
                item.checkbox = true;
            } else {
                item.checkbox = false;
            }

            if (item.children && item.children.length > 0) {
                diguiDataAddCheck(item.children);
            }
        }
        return data;
    };

    //点击Tree
    function onTreeClick(data) {
        if ($dataType(data.dataset) == 'string') {
            currTreeNodeInfo.value = JSON.parse(data.dataset);

            emits('onTreeClick', currTreeNodeInfo);
        }
    }

    // 二（三）级节点接口 数据的回调函数，用于字段过滤判断是否可以选择
    function treeInterfaceCallBack(resData) {
        resData.forEach((item) => {
            const result = props.selectField.every((item2) => {
                if ($dataType(item2.value) == 'array') {
                    return item2.value.includes(item[item2.fieldName]);
                } else {
                    return item[item2.fieldName] == item2.value;
                }
            });

            if (result) {
                item.checkbox = true;
            } else {
                item.checkbox = false;
            }
        });

        return resData;
    }

    //点击复选框
    function onCheckBox(data) {
        if (data.checked) {
            newSelectedData.value.push(data.dataset);
        } else {
            newSelectedData.value.forEach((item, index) => {
                if (item.id == data.dataset.id) {
                    newSelectedData.value.splice(index, 1);
                }
            });
        }

        const ids = newSelectedData.value.map((item) => {
            return item.id;
        });
        emits('update:onCheckBox', newSelectedData);
        emits('change', newSelectedData);
    }

    defineExpose({
        onRefreshTree, //刷新tree
        currTreeNodeInfo
    });
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';
    @import '@/theme/global-vars.scss';
    @import '@/theme/global.scss';

    //头部样式
    .select-tree-header {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        margin-bottom: 20px;

        .header-left {
            display: flex;

            :deep(.el-input) {
                .el-input__inner,
                .ri-search-line {
                    line-height: 30px !important;
                    height: 30px !important;
                }

                .el-input__clear {
                    margin-left: 4px;
                }
            }
        }
    }

    //tree样式
    :deep(.treeItem) {
        border: none;
        //padding: 0;

        img {
            width: 20px;
        }
    }
</style>
<style module="classes">
    /*  */
    .active {
        background-color: #e7eced !important;
        font-size: 14px !important;
    }
</style>
