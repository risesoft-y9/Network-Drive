<script lang="ts" setup>
    import { nextTick, reactive, ref } from 'vue';
    import { cloneDeep } from 'lodash';
    import treeItemList from './treeItemList.vue';
    import { isBlank } from '@/utils/validate';

    const props = defineProps({
        setting: {
            type: Object,
            default: () => {}
        },
        isExpandAll: Boolean
    });

    onMounted(() => {
        // console.log(props.setting.data)
    });

    // 树 - 数据渲染处理
    async function addTreeItemFunc(data) {
        const keys = props.setting.itemInfo.keys;

        let itemChildren = [];

        if (data.data && Object.prototype.toString.call(data.data) == '[object Promise]') {
            itemChildren = await data.data;
        } else {
            itemChildren = data.data;
        }
        const currentItemParentId = data[keys.parentId];
        const itemGroupId = data.itemGroupId;

        // item增加所有子数据 - 遍历 - 深度优先
        function forEachItem(array) {
            // 利用 map 的返回值可以赋值给原对象对应的item特性
            return array.map((item, index) => {
                if (item[keys.id] == currentItemParentId) {
                    item[keys.children] = itemChildren;
                    return item;
                }
                if (item[keys.children] && item[keys.children].length) {
                    forEachItem(item[keys.children]);
                }
            });
        }

        // 传入当前树组件的所有数据，根据当前item的 ParentId 递归匹配数据
        forEachItem(props.setting.data);
        // 所有子数据中，记录有children的 item - 遍历 - 深度优先
        const itemIdsArray = [];

        function forEachItemChildren(array) {
            array.forEach((item, index) => {
                if (item[keys.children] && item[keys.children].length) {
                    itemIdsArray.push(item[keys.id]);
                    forEachItemChildren(item[keys.children]);
                }
            });
        }

        forEachItemChildren(itemChildren);

        nextTick(() => {
            // 有children的 item 添加下拉符号的class样式
            itemIdsArray.forEach((id) => {
                // console.log(document.getElementById(itemGroupId).style)
                const target = document.getElementById(itemGroupId);
                const classArray = target.previousSibling.children[0].className.split(' ');
                if (classArray[1] === 'collapse') {
                    target.style.display = 'none';
                }
            });
        });
    }

    // 点击icon 展开和关闭
    const iStatus = ref(false);
    const expand = 'expand';
    const collapse = 'collapse';
    const alreadyExpanded = reactive({}); // 以{key: value} 形式存储已展开的树节点
    function expandItemFunc(item) {
        const id = item.id;
        const itemGroupId = item.itemGroupId;
        const classArray = item.e.target.className.split(' ');
        const itemElement = document.getElementById(itemGroupId);

        if (classArray[classArray.length - 1] === 'expand') {
            iStatus.value = false;
            itemElement.className = 'collapseAnimation-fadeOutRight';
            setTimeout(() => {
                itemElement.style.display = 'none';
            }, 1000);
        }
        if (classArray[classArray.length - 1] === 'collapse') {
            getTreeItemById(id, itemGroupId);
            iStatus.value = true;
            itemElement.style.display = '';
            // console.log(itemElement.parentNode.className);
            itemElement.className = 'expandAnimation-fadeInLeft';
            // 以{key: value} 形式存储已展开的树节点（未完成此功能 ---> 做个标记）
            // 重载整棵树的数据，然后根据这里的存储记录展开相关节点
            alreadyExpanded[id] = itemGroupId;
        }
        item.e.target.className = iStatus.value ? classArray[0] + ' ' + expand : classArray[0] + ' ' + collapse;
    }

    // 点击当前item左边的下拉按钮图标（二级树节点接口数据）
    async function getTreeItemById(parentId, itemGroupId) {
        let params = props.setting.itemInterface.params;
        params.parentId = parentId;
        let items = await props.setting.itemInterface.api(params);

        // 容错后端二级接口数据没有的情况
        if (items.data && !items.data.length) {
            // visibility
            let opacityValue = 1;
            let timer: any = null;
            timer = setInterval(() => {
                opacityValue -= 0.1;
                // console.log(String(opacityValue.value).substring(0,3));
                document.getElementById(parentId).children[0].style.opacity = opacityValue;
                if (String(opacityValue).substring(0, 3) === '0.3') {
                    document.getElementById(parentId).children[0].style.visibility = 'hidden';
                    clearInterval(timer);
                }
            }, 50);
        }
        // if 容错
        if (items) {
            // emit("addTreeItem", {data: items.data, parentId})
            // 接口数据添加到树组件
            let callback = props.setting.itemInterface.callback ? props.setting.itemInterface.callback : '';
            if (callback) {
                await addTreeItemFunc({ data: callback(items.data), parentId, itemGroupId });
            } else {
                await addTreeItemFunc({ data: items.data, parentId, itemGroupId });
            }
        }
    }

    // 模拟点击treeId 里的每个item的icon事件
    function clickTreeEachItemIcon(treeId, action) {
        const ols = document.getElementById(treeId).children;
        for (const ol of ols) {
            const li = ol.children[0];
            if (li.children[0] && li.children[0].nodeName === 'I') {
                let className = li.children[0].className.split(' ');
                // action解决的bug 当某一个item已经展开（收缩）过
                if (className[1] === 'collapse' || action === 'collapseAllFunc') {
                    // 方式1:模拟点击 会和点击事件产生耦合
                    // li.children[0].click()

                    // 方式2: 直接展开
                    let id = li.dataset.id,
                        itemGroupId = li.dataset.itemGroupId,
                        itemElement = document.getElementById(itemGroupId);
                    getTreeItemById(id, itemGroupId);
                    itemElement.style.display = '';
                    li.children[0].className = className[0] + ' ' + expand;

                    // 修复模拟点击后的bug，会将最后一个模拟点击的 item 变为活动状态，即 activeClass
                    li.className = 'treeItem';
                }
            }
        }
    }

    const isExpandAllRef = computed(() => props.isExpandAll);
    watch(isExpandAllRef, (newV, oldV) => {
        console.log('展开树一级节点', newV);
        if (newV) {
            clickTreeEachItemIcon(props.setting.treeId, 'expandAllFunc');
        } else {
            clickTreeEachItemIcon(props.setting.treeId, 'collapseAllFunc');
        }
    });
    // // 展开树所有子节点
    // function expandAllFunc() {
    //     if (!props.isExpandAll) {
    //         props.isExpandAll = true
    //         clickTreeEachItemIcon(props.setting.treeId, "expandAllFunc")
    //     }

    // }
    // // 收缩树所有子节点
    // function collapseAllFunc() {
    //     if (props.isExpandAll) {
    //         props.isExpandAll = false
    //         clickTreeEachItemIcon(props.setting.treeId, "collapseAllFunc")
    //     }

    // }

    // 所有item 点击事件 统一处理
    // 增 - 删 - 改 - 查
    const checkedEvent = props.setting.itemInfo.render.checkbox?.func;
    const addEvent = props.setting.itemInfo.render.add_icon?.func;
    const deleteEvent = props.setting.itemInfo.render.delete_icon?.func;
    const editEvent = props.setting.itemInfo.render.edit_icon?.func;
    const selectEvent = props.setting.itemInfo.render.select_icon?.func;
    const clickTitleEvent = props.setting.itemInfo.render.click_title_event?.func;

    const switchItemEvents = {
        clickCheckboxEvent: 'clickCheckboxEvent',
        clickDropDownEvent: 'clickDropDownEvent',
        clickAddEvent: 'clickAddEvent',
        clickDeleteEvent: 'clickDeleteEvent',
        clickEditEvent: 'clickEditEvent',
        clickSelectEvent: 'clickSelectEvent',
        clickTitleEvent: 'clickTitleEvent'
    };

    function clickItemFunc(e) {
        // console.log(e.path[0].dataset.itemEvent);
        let eventName, liDataset, dataset, itemGroupId, id;
        if (e.path) {
            eventName = e.path[0].dataset.itemEvent || e.path[1].dataset.itemEvent || e.path[2].dataset.itemEvent;
            liDataset = () => {
                const obj = {};
                e.path?.forEach((element) => {
                    if (element.nodeName === 'LI') {
                        for (const key in element.dataset) {
                            obj[key] = element.dataset[key];
                        }
                        return true;
                    }
                });
                return obj;
            };
            //获取 li 的dataset
            dataset = liDataset().itemInfo;
            itemGroupId = liDataset().itemGroupId;
            id = liDataset().id;
        } else {
            // console.log(e);
            eventName = e.target.dataset.itemEvent || e.target.parentElement.dataset.itemEvent;
            dataset =
                e.target.parentElement.dataset.itemInfo ||
                e.target.parentElement.parentElement.parentElement.dataset.itemInfo ||
                e.target.parentElement.parentElement.parentElement.parentElement.dataset.itemInfo;
            itemGroupId =
                e.target.parentElement.dataset.itemGroupId ||
                e.target.parentElement.parentElement.parentElement.dataset.itemGroupId;
            id = e.target.parentElement.dataset.id || e.target.parentElement.parentElement.parentElement.dataset.id;
        }

        // console.log(data)
        const itemInfo = {
            e: e,
            id: id,
            itemGroupId: itemGroupId,
            eventName: eventName,
            checked: e.target.checked,
            dataset: dataset ? JSON.parse(dataset) : ''
        };
        switch (eventName) {
            case switchItemEvents.clickCheckboxEvent:
                checkedEvent ? checkedEvent(itemInfo) : '';
                break;
            case switchItemEvents.clickDropDownEvent:
                expandItemFunc(itemInfo);
                break;
            case switchItemEvents.clickAddEvent:
                addEvent ? addEvent(itemInfo) : '';
                break;
            case switchItemEvents.clickDeleteEvent:
                deleteEvent ? deleteEvent(itemInfo) : '';
                break;
            case switchItemEvents.clickEditEvent:
                editEvent ? editEvent(itemInfo) : '';
                break;
            case switchItemEvents.clickSelectEvent:
                selectEvent ? selectEvent(itemInfo) : '';
                break;
            case switchItemEvents.clickTitleEvent:
                clickTitleEvent ? clickTitleEvent(itemInfo) : '';
                break;
            default:
                break;
        }
    }

    // 搜索功能
    // function isBlank(value) {
    //     return (
    //         value === null ||
    //         false ||
    //         value === '' ||
    //         value.trim() === '' ||
    //         value.toLocaleLowerCase().trim() === 'null'
    //     )
    // }
    const params = computed(() => props.setting.events?.search?.params);
    let cloneSettingData = null;
    const noSearchData = ref(false);
    watch(
        () => params,
        async (newV, oldV) => {
            // console.log(newV.value, oldV.value);
            // 深拷贝原数据
            if (!cloneSettingData) {
                cloneSettingData = cloneDeep(props.setting.data);
            }
            // params对象的任意key不为空
            let isSearch = false,
                obj = newV.value;
            for (const key in obj) {
                if (Object.prototype.hasOwnProperty.call(obj, key)) {
                    const value = obj[key];
                    if (!isBlank(value)) {
                        isSearch = true;
                    }
                }
            }
            if (isSearch) {
                // props.setting.events.search.params = newV.value
                // const params = props.setting.events.search.params
                // let params = {key: ""}
                // params.key = newV.value
                // const result = await props.setting.events.search.api(params)
                const result = await props.setting.events.search.api(newV.value);
                const data = result.data;
                if (data && data.length) {
                    noSearchData.value = false;
                    const handleResult = handleSearchData(data);
                    props.setting.events.search.callback(handleResult);
                    return true;
                } else {
                    noSearchData.value = true;
                    props.setting.events.search.callback([]);
                    return false;
                }
            } else {
                props.setting.data = cloneSettingData;
                noSearchData.value = false;
                cloneSettingData = null;
            }
        },
        { deep: true }
    );

    // 搜索接口的数据封装成 树组件要求的数据结构
    function handleSearchData(result) {
        const result_Arr = [],
            result_otherArr = [],
            maxDeep = ref(0);
        // 1、获得一级节点、树的最大深度
        let length = 0,
            isEqual = false;
        // 返回数据 id 的数组
        let idList = result.map((item) => {
            return item.id;
        });
        //返回数据 中 每个item 的guidPath 数组 集合
        let allList: any = [];
        // 找到 parentId 与 id相同的 所有数据
        result.map((item) => {
            if (!item.guidPath) {
                // 没有item.guidPath
                let list = [];
                idList.map((i) => {
                    if (item.parentId === i) {
                        // item当前不是第一级
                        list.push(item.parentId);
                        list.push(item.id);
                    }
                });
                // 集合到 一个大数组中
                allList.push(list);
            }
        });
        // 将 有三级或以上的 再添加
        if (allList.length) {
            handlerCallBack(allList);
        }

        function handlerCallBack(allList) {
            for (let i = 0; i < allList.length - 1; i++) {
                for (let j = 1; j < allList.length; j++) {
                    if (allList[i][0] === allList[j][1] && allList[j][0]) {
                        allList[i].unshift(allList[j][0]);
                    }
                }
            }
        }

        result.map((item, index) => {
            // 根据数据 赋值对应的guidPath
            if (allList.length) {
                item.guidPath = allList[index].join(',');
            }

            item.guidePathArr = item.guidPath?.split(',');
            length = item.guidePathArr && item.guidePathArr.length;
            length > maxDeep.value ? (maxDeep.value = length) : '';
            item.deep = length;

            // 2、查出来的数据里面为什么会有完全相同的多条数据？
            if (item.guidePathArr && item.guidePathArr.length === 1) {
                // API数据去重（只判断了ID，树组件要求每一个Item的ID都必须是 uuid）
                result_Arr.map((it) => {
                    it.id === item.id ? (isEqual = true) : '';
                });
                isEqual ? (isEqual = false) : result_Arr.push(item);
            } else {
                result_otherArr.map((it) => {
                    it.id === item.id ? (isEqual = true) : '';
                });
                isEqual ? (isEqual = false) : result_otherArr.push(item);
            }
        });
        // 3、没有深度
        if (maxDeep.value <= 1) {
            return result;
        }
        // 4、组织整颗树的数据结构（算法：树多深则遍历多少次，每次必须先找出所有的同深度item）
        for (let index = 2; index <= maxDeep.value; index++) {
            result_otherArr.map((result_otherArr_item) => {
                // 4-1、
                if (result_otherArr_item.guidePathArr.length === index) {
                    deepTraversal(result_Arr, result_otherArr_item, index - 2);
                }
            });
        }
        // console.log(result_Arr, result_otherArr);
        // console.log("result_Arr = ",result_Arr);
        return result_Arr;
    }

    function deepTraversal(array, item, deepIndex) {
        if (!array.length) {
            return false;
        }
        for (let index = 0; index < array.length; index++) {
            const arrayItem = array[index];
            if (arrayItem.id === item.guidePathArr[deepIndex]) {
                if (arrayItem.children) {
                    arrayItem.children.push(item);
                } else {
                    arrayItem.hasChild = true;
                    arrayItem.children = [];
                    arrayItem.children.push(item);
                }
                return array;
            }
            if (arrayItem.children) {
                deepTraversal(arrayItem.children, item, deepIndex);
            }
        }
        return false;
    }
</script>

<template>
    <div :id="setting.treeId" class="treeComp" @click="clickItemFunc">
        <treeItemList
            v-show="!noSearchData && setting.data.length > 0"
            :key="setting.treeId"
            :animation="setting.style.animation"
            :dragEvent="setting.events?.dragEvent"
            :itemGroupPrefix="setting.itemGroupPrefix"
            :itemInterface="setting.itemInterface"
            :itemList="setting.data"
            :itemStyle="setting.style"
            :keys="setting.itemInfo.keys"
            :render="setting.itemInfo.render"
            :search="setting.events?.search"
            :switchItemEvents="switchItemEvents"
            :treeId="setting.treeId"
        ></treeItemList>
        <div v-show="noSearchData || setting.data.length == 0" :key="setting.treeId" class="no-data"
            >{{ $t('暂无数据') }}
        </div>
    </div>
</template>

<style lang="scss" scoped>
    .treeComp {
        // height: 100%;
    }

    //暂无数据-样式
    .no-data {
        display: flex;
        justify-content: center;
        align-items: center;
        color: var(--el-text-color-secondary);
        margin-top: 50px;
    }
</style>
