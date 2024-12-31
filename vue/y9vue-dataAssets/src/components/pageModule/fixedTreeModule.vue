<template>
    <div id="fixedDiv" class="fixed">
        <slot name="leftFixed">
            <y9Card>
                <template #header>
                    <y9Filter :itemList="itemList" :filtersValueCallBack="filtersValueCallBack" itemMarginBottom="0px">
                        <template #refresh>
                            <el-button
                                :size="fontSizeObj.buttonSize"
                                :style="{ fontSize: fontSizeObj.baseFontSize }"
                                @click="onRefreshTree"
                                class="global-btn-second"
                            >
                                <i class="ri-refresh-line"></i>
                                <span>{{ $t('刷新') }}</span>
                            </el-button>
                        </template>

                        <template #rightBtn>
                            <slot name="treeHeaderRight"></slot>
                        </template>
                    </y9Filter>
                </template>

                <y9Tree :setting="publicTreeStore.getPublicTreeSetting" :isExpandAll="isExpandAllTree"></y9Tree>
            </y9Card>
        </slot>
    </div>

    <div class="right-container">
        <slot name="rightContainer"></slot>
    </div>
</template>

<script lang="ts" setup>
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    import { useSettingStore } from '@/store/modules/settingStore';
    import { usePublicTreeStore } from '@/store/modules/publicTreeStore';
    import { $dataType, $deepAssignObject, $deeploneObject } from '@/utils/object.ts'; //工具类
    import { onMounted, useCssModule } from 'vue';
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
        setting: {
            //tree的配置
            type: Object
        }
    });

    const emits = defineEmits(['onTreeClick', 'onRemoveTree']);

    const data = reactive({
        itemList: [
            //过滤列表
            {
                type: 'slot',
                slotName: 'refresh',
                span: 4
            },
            {
                type: 'search',
                span: 12,
                key: 'name'
            },
            {
                type: 'slot',
                slotName: 'rightBtn',
                span: 8,
                justify: 'flex-end'
            }
        ],

        filterValue: {}, //当前过滤值

        currTreeNodeInfo: {}, //当前tree节点的信息

        isExpandAllTree: false //是否展开tree
    });

    let { itemList, filterValue, y9TreeRef, currTreeNodeInfo, isExpandAllTree } = toRefs(data);

    // 初始化publicTreeStore
    let publicTreeStore = usePublicTreeStore();

    publicTreeStore.$patch((state) => {
        //初始化配置
        state.publicTreeSetting = $deeploneObject(state.initPublicTreeSetting);
        state.publicTreeSetting.style.li.activeClassName = useCssModule('classes').active;

        if (props.treeApiObj) {
            if (props.treeApiObj.childLevel) {
                state.publicTreeSetting.itemInterface.api = props.treeApiObj.childLevel.api; //子级tree接口

                state.publicTreeSetting.itemInterface.params = props.treeApiObj.childLevel.params; //子级tree接口参数
            }

            if (props.treeApiObj.search) {
                //搜索

                state.publicTreeSetting.events.search.api = props.treeApiObj.search.api; //搜索api
                state.publicTreeSetting.events.search.params = props.treeApiObj.search.params; //搜索参数
                state.publicTreeSetting.events.search.callback = onSearchResultCallback; //搜索结果回调
            }
        }

        state.publicTreeSetting.itemInfo.render.delete_icon.func = onDeleteTree; //删除

        state.publicTreeSetting.itemInfo.render.click.func = onTreeClick; //点击树
    });

    //初始化tree
    async function initTree() {
        try {
            props.treeApiObj && (await publicTreeStore.getTree(props.treeApiObj.topLevel)); //获取一级tree数据

            if (publicTreeStore.getPublicTreeSetting.data.length > 0) {
                onTreeClick({ dataset: JSON.stringify(publicTreeStore.getPublicTreeSetting.data[0]) }); //默认选中第一项

                //默认给第一项添加选中样式
                const hashClassName = publicTreeStore.$state.publicTreeSetting.style.li.activeClassName;

                const treeId = publicTreeStore.$state.publicTreeSetting.treeId;

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
        () => props.setting,
        (newVal) => {
            if (newVal) {
                publicTreeStore.$state.publicTreeSetting = $deepAssignObject(
                    publicTreeStore.$state.publicTreeSetting,
                    newVal
                ); //深度合并
            }
        },
        {
            deep: true,
            immediate: true
        }
    );

    //刷新Tree
    function onRefreshTree() {
        //清空选项样式
        const treeId = publicTreeStore.$state.publicTreeSetting.treeId;

        const div = document.getElementById(treeId);

        for (let i = 0; i < div.childNodes.length; i++) {
            const item = div.childNodes[i];

            for (let j = 0; j < item.childNodes.length; j++) {
                const item2 = item.childNodes[j];

                const node = item.childNodes[0];

                if (node.nodeName == 'LI' && node.className.includes('treeItem')) {
                    node.className = 'treeItem';
                }
            }
        }

        isExpandAllTree.value = false;

        initTree();
    }

    //删除Tree
    function onDeleteTree(e) {
        const data = e.dataset;

        emits('onDeleteTree', data);
    }

    //过滤值回调
    function filtersValueCallBack(filters) {
        filterValue.value = filters;
    }

    //监听过滤值改变，请求搜索接口
    watch(
        () => filterValue.value,
        (newVal, oldVal) => {
            if (newVal.name && newVal.name != '' && props.treeApiObj) {
                publicTreeStore.$state.publicTreeSetting.events.search.params.key = newVal.name;
            } else if (newVal.name != oldVal.name) {
                publicTreeStore.$state.publicTreeSetting.events.search.params.key = '';

                onRefreshTree();
            }
        },
        {
            deep: true
        }
    );

    //搜索结果回调
    async function onSearchResultCallback(data) {
        publicTreeStore.$patch((state) => {
            const deepTraversal = async (itemList) => {
                itemList.forEach((element) => {
                    element.newName = element.name;
                    // console.log(element, "element");

                    switch (element.orgType) {
                        case 'Person':
                            element.title_icon = 'ri-women-line';

                            if (element.sex == 1) {
                                element.title_icon = 'ri-men-line';
                            }
                            if (element.disabled) {
                                element.name = element.name + '[禁用]';
                            }

                            if (!element.original) {
                                if (element.sex == 1) {
                                    element.title_icon = 'ri-men-fill';
                                } else {
                                    element.title_icon = 'ri-women-fill';
                                }
                            }

                            break;
                        case 'Manager': //子域三元
                            element.title_icon = 'ri-women-line';
                            if (element.sex == 1) {
                                element.title_icon = 'ri-men-line';
                            }
                            break;
                        case 'Department':
                            element.title_icon = 'ri-slack-line';
                            break;
                        case 'Position':
                            element.title_icon = 'ri-shield-user-line';
                            break;
                        case 'Organization':
                            element.title_icon = 'ri-stackshare-line';
                            break;
                    }

                    switch (element.resourceType) {
                        case 0: //应用
                            element.title_icon = 'ri-apps-line';
                            break;

                        case 1: //菜单
                            element.title_icon = 'ri-menu-4-line';
                            break;

                        case 2: //按钮
                            element.title_icon = 'ri-checkbox-multiple-blank-line';
                            break;
                    }

                    // 角色
                    switch (element.type) {
                        case 'role': //角色 人员
                            element.title_icon = 'ri-women-line';
                            break;

                        case 'folder': // 文件夹
                            element.title_icon = 'ri-folder-2-line';
                            break;
                    }
                    // console.log(itemList, "itemList");

                    if (element.children) {
                        deepTraversal(element.children);
                    }
                });
            };

            deepTraversal(data);

            state.publicTreeSetting.data = data;
        });
    }

    //点击Tree
    function onTreeClick(data) {
        if ($dataType(data.dataset) == 'string') {
            currTreeNodeInfo.value = JSON.parse(data.dataset);
        } else if ($dataType(data.dataset) == 'object') {
            currTreeNodeInfo.value = data.dataset;
        }

        emits('onTreeClick', currTreeNodeInfo);
    }

    defineExpose({
        onRefreshTree //刷新tree
    });

    //固定模块样式
    // 在 fiexHeader 变化时 监听滚动事件，改变className
    const settingStore = useSettingStore();
    const isFiexHeader = computed(() => settingStore.getFixedHeader);
    function listener() {
        const elementId = 'fixedDiv';
        const scroll_Y = window.scrollY;
        if (scroll_Y > 100 && document.getElementById(elementId)) {
            document.getElementById(elementId).className = 'fixed-after-scroll';
        }
        if (scroll_Y < 100 && document.getElementById(elementId)) {
            document.getElementById(elementId).className = 'fixed';
        }
    }

    watch(isFiexHeader, (newV, oldV) => {
        // console.log(newV, oldV);

        if (!newV) {
            window.addEventListener('scroll', listener, false);
        } else {
            // 移除监听
            window.removeEventListener('scroll', listener, false);
        }
    });

    onMounted(() => {
        const layout = computed(() => settingStore.getLayout);
        if (layout.value === 'Y9Horizontal') {
            document.getElementById('fixedDiv').className = 'fixed fixed-herder-horizontal';
        }
        if (!isFiexHeader.value) {
            window.addEventListener('scroll', listener, false);
        }
        watch(layout, (newV, oldV) => {
            if (newV === 'Y9Horizontal') {
                document.getElementById('fixedDiv').className = 'fixed fixed-herder-horizontal';
            } else {
                document.getElementById('fixedDiv').className = 'fixed';
            }
        });
    });
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';
    @import '@/theme/global-vars.scss';
    @import '@/theme/global.scss';

    //y9Card插槽头部样式
    .y9Card-slot-header {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
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

    .right-container {
        margin-left: calc(26.6vw + 35px);
        :deep(.y9-card) {
            margin-bottom: 35px;
        }

        :deep(.y9-card:last-child) {
            margin-bottom: 0px;
        }
    }

    /* 固定左侧树 */
    .fixed {
        position: fixed;
        width: 26.6vw;
        animation: moveTop-2 0.2s;
        animation-fill-mode: forwards;
        :deep(.y9-card) {
            height: calc(100vh - #{$headerHeight} - #{$headerBreadcrumbHeight} - 35px);
            overflow: hidden;
        }
    }

    .fixed-herder-horizontal {
        margin-top: 60px;
        :deep(.y9-card) {
            height: calc(100vh - #{$headerHeight} - #{$headerBreadcrumbHeight} - 95px);
            overflow: hidden;
        }
    }

    .fixed-after-scroll {
        position: fixed;
        width: 26.6vw;
        animation: moveTop 0.2s;
        animation-fill-mode: forwards;
        :deep(.y9-card) {
            height: calc(100vh - 35px - 35px);
            overflow: auto;
        }
    }
    @keyframes moveTop {
        0% {
            top: calc(#{$headerHeight} + #{$headerBreadcrumbHeight});
        }
        100% {
            top: 35px;
        }
    }
    @keyframes moveTop-2 {
        0% {
            top: 35px;
        }
        100% {
            top: calc(#{$headerHeight} + #{$headerBreadcrumbHeight});
        }
    }
</style>

<style lang="scss" module="classes">
    /* 点击选中的activeClass */
    .active {
        background-color: var(--el-color-primary-light-4) !important;
        color: var(--el-color-white) !important;

        /* 改变icon的颜色，没有效果 */
        & > .info > :last-child > div > i {
            color: var(--el-color-white) !important;
        }
    }
</style>
