<template>
    <el-row :gutter="20">
        <el-col :span="15" class="left-tree">
            <div>
                <div class="tree-toolbar">
                    <el-input
                        v-model="searchName"
                        :placeholder="$t('请输入人员名称按回车键搜索')"
                        :prefix-icon="Search"
                        clearable
                        @keyup.enter.native="search"
                    >
                        <template #append>
                            <el-button
                                :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }"
                                v-on:click="refresh"
                                ><i class="ri-refresh-line"></i>
                            </el-button>
                        </template>
                    </el-input>
                </div>
                <div>
                    <div class="tree-div">
                        <Tree :isExpandAll="isExpandAllRef" :setting="setting"></Tree>
                    </div>
                </div>
            </div>
        </el-col>
        <el-col :span="9" class="right-list">
            <div>
                <el-table :cell-style="{ padding: 2 + 'px' }" :data="checkNodes" height="390" style="width: 100%">
                    <el-table-column :label="$t('名称')">
                        <template #default="scope">
                            <div>
                                <i v-bind:class="scope.row.title_icon"></i>
                                <span>{{ scope.row.name }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" width="80">
                        <template #default="scope">
                            <i class="ri-close-line" @click="deleteCheckedNode(scope.$index, scope.row)"></i>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-col>
    </el-row>
    <!-- <span slot="footer" class="dialog-footer">
      <el-button :size="fontSizeObj.buttonSize"
:style="{ fontSize: fontSizeObj.baseFontSize }" type="primary" @click="addOrgUnit">确 定</el-button>
    </span> -->
</template>

<script lang="ts" setup>
    import { defineExpose, defineProps, inject, reactive, toRefs } from 'vue';
    import { Search } from '@element-plus/icons-vue';
    import Tree from '@/components/tree/y9Tree.vue';
    import OrgApi from '@/api/storage/org';

    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const props = defineProps({
        dialogVisible: {
            type: Boolean
        },
        moveFileIds: {
            type: Array
        }
    });

    let data = reactive({
        searchName: '',
        checkNodes: []
    });

    let { searchName, checkNodes } = toRefs(data);
    const emits = defineEmits(['org-click']);
    const setting = reactive({
        treeId: 'org', // 树id
        itemInterface: {
            api: getChildById,
            params: {},
            callback: (data) => {
                // 二（三）级节点接口 数据的回调函数
                return data; // 如果有回调，则需要返回data - [Array类型]
            }
        },
        itemGroupPrefix: 'itemGroup-', // 有 children节点的 ID 前缀（ 完整示例：'itemGroup-' + item.id ）
        data: [], // 一级接口接口返回的源数据
        itemInfo: {
            keys: {
                // 数据字段映射
                id: 'id', // id
                parentId: 'parentId', // parentId
                name: 'name', // name
                children: 'children', // children
                hasChild: 'hasChild', // haschild
                title: 'name', // title 映射接口数据的 name 示例
                subTitle: 'name', // subtitle 映射接口数据的 name 示例
                // 以下字段 如果有，则显示映射的值，否则不显示
                checkbox: 'checkbox', // 复选框 实现每个 item 个性化显示复选框
                select_icon: 'select_icon', // 实现每个 item 个性化显示 查 事件
                title_icon: 'title_icon', // 实现每个 item 个性化显示 icon
                click_title_event: true
            },
            render: {
                checkbox: {
                    // 复选框
                    func: (data) => {
                        // 复选框事件
                        //console.log("点击了复选框", data, data.checked?"勾选状态":"取消勾选状态")
                        if (data.checked) {
                            checkNodes.value.push(data.dataset);
                            emits('org-click', checkNodes);
                        } else {
                            for (let index = 0; index < checkNodes.value.length; index++) {
                                if (checkNodes.value[index].id == data.dataset.id) {
                                    checkNodes.value.splice(index, 1);
                                }
                            }
                            emits('org-click', checkNodes);
                        }
                    }
                },
                mouse_over: {
                    // 鼠标悬停事件
                    func: (e) => {
                        const li = e.originalTarget;
                        //if (li.className === "treeItem") {
                        //    li.style.backgroundColor = "#E7ECED";
                        //  }
                    }
                },
                mouse_leave: {
                    // 鼠标离开事件
                    func: (e) => {
                        //const li = e.originalTarget;
                        //li.style.backgroundColor = "";
                    }
                },
                click: {
                    // 单击事件
                    func: (data) => {}
                },
                dbl_click: {
                    // 双击事件
                    func: (data) => {
                        // console.log('双击事件', data);
                        const nodes = JSON.parse(data.dataset);
                        //if(nodes.orgType=='Person'){
                        emits('org-click', nodes);
                        //}
                    }
                }
            }
        },
        events: {
            search: {
                // 搜索功能
                api: '',
                params: {},
                callback: (data) => {
                    /*
                    设置搜索结果的数据 注意保存原有数据
                */
                    treeSearchCallback(data);
                    //setting.data = data;
                }
            }
        },
        style: {
            li: {
                // li activeClass
                activeClassName: useCssModule('classes').active
            },
            animation: {
                in: '',
                out: ''
            }
        }
    });

    getTree();
    // 树组件 - 一级节点接口数据
    // axios API 使用一级节点渲染树组件
    async function getTree() {
        const tree = await OrgApi.getOrganization();
        tree.data.map((item) => {
            item.hasChild = true;
            if (item.orgType == 'Organization') {
                item.title_icon = 'ri-stackshare-line';
                item.checkbox = true;
            } else if (item.orgType == 'Department') {
                item.checkbox = true;
                item.title_icon = 'ri-slack-line';
            } else if (item.orgType == 'Person') {
                item.hasChild = false;
                item.checkbox = true;
                item.title_icon = 'ri-women-line';
                if (item.sex == 1) {
                    item.title_icon = 'ri-men-line';
                }
            } else if (item.orgType == 'Position') {
                item.title_icon = 'ri-shield-user-line';
                item.checkbox = true;
                item.hasChild = false;
            } else if (item.orgType == 'customGroup') {
                item.checkbox = true;
                item.title_icon = 'ri-shield-star-line';
            }
        });
        setting.data = tree.data;
        setTimeout(() => {
            // 默认展开第一个子节点
            Array.from(document.getElementById('org').childNodes[1].childNodes[0].children).map((item) => {
                if (item.dataset && item.dataset.itemEvent === 'clickDropDownEvent') {
                    return item.click();
                }
            });
        }, 500);
    }

    async function getChildById(params) {
        const child = await OrgApi.getTree(params.parentId, '');
        child.data.forEach((element) => {
            element.hasChild = true;
            if (element.orgType == 'Department') {
                element.checkbox = true;
                element.title_icon = 'ri-slack-line';
            } else if (element.orgType == 'Person') {
                element.hasChild = false;
                element.checkbox = true;
                element.title_icon = 'ri-women-line';
                if (element.sex == 1) {
                    element.title_icon = 'ri-men-line';
                }
            } else if (element.orgType == 'Position') {
                element.title_icon = 'ri-shield-user-line';
                element.checkbox = true;
                element.hasChild = false;
            } else if (element.orgType == 'customGroup') {
                element.checkbox = true;
                element.title_icon = 'ri-shield-star-line';
            }
        });
        return child;
    }

    async function treeSearchCallback(data) {
        setting.data = [];
        const deepTraversal = async (itemList) => {
            itemList.forEach((element) => {
                if (element.orgType == 'Organization') {
                    element.title_icon = 'ri-stackshare-line';
                    element.checkbox = true;
                } else if (element.orgType == 'Department') {
                    element.checkbox = true;
                    element.title_icon = 'ri-slack-line';
                } else if (element.orgType == 'Person') {
                    element.hasChild = false;
                    element.checkbox = true;
                    element.title_icon = 'ri-women-line';
                    if (element.sex == 1) {
                        element.title_icon = 'ri-men-line';
                    }
                } else if (element.orgType == 'Position') {
                    element.title_icon = 'ri-shield-user-line';
                    element.checkbox = true;
                    element.hasChild = false;
                } else if (element.orgType == 'customGroup') {
                    element.checkbox = true;
                    element.title_icon = 'ri-shield-star-line';
                }
                if (element.children) {
                    deepTraversal(element.children);
                }
            });
        };
        deepTraversal(data);
        setting.data = data;
    }

    function deleteCheckedNode(index, row) {
        checkNodes.value.splice(index, 1);
    }

    function search() {
        setting.events.search.api = OrgApi.getSearchTree;
        setting.events.search.params = {
            name: searchName.value.trim()
        };
    }

    function refresh() {
        searchName.value = '';
        getTree();
    }

    function isExpandAllfunc() {
        if (!isExpandAllRef.value) {
            isExpandAllRef.value = true;
        } else {
            isExpandAllRef.value = false;
        }
    }

    function collapseAllFunc() {
        if (isExpandAllRef.value) {
            isExpandAllRef.value = false;
        }
    }

    defineExpose({
        checkNodes
    });
</script>

<style lang="scss" scoped>
    //tree样式
    :deep(.treeItem) {
        border: none;
        //padding: 0;
        font-size: v-bind('fontSizeObj.baseFontSize');

        img {
            width: 20px;
        }
    }

    .el-input-group__append .el-button {
        display: contents;
        margin: -10px -20px;
    }

    .left-tree {
        border: 1px solid #f2f2f2;
        height: 400px;
    }

    .refresh {
        /*font-size: 30px;*/
        /*cursor: pointer;*/
    }

    .right-list {
        border: 1px solid #f2f2f2;
        height: 400px;

        :deep(.el-table) {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }

    .tree-toolbar {
        padding: 10px 0;

        :deep(.el-input) {
            font-size: v-bind('fontSizeObj.baseFontSize');
        }
    }

    .tree-div {
        width: 100%;
        height: 335px;
        overflow-y: auto;
    }
</style>
