<script lang="ts" setup>
    import { compare } from '@/utils/index';

    const props = defineProps({
        item: {
            type: Object,
            default: () => {}
        },
        itemGroupId: String,
        itemInterface: {
            type: Function,
            default: () => {}
        },
        keys: {
            type: Object,
            default: () => {}
        },
        render: {
            type: Object,
            default: () => {}
        },
        switchItemEvents: {
            type: Object,
            default: () => {}
        },
        hasChildDom: {
            type: Boolean,
            default: false
        },
        itemStyle: {
            type: Object,
            default: () => {}
        },
        draggable: {
            type: Boolean,
            default: false
        }
    });

    const lineHeight = props.itemStyle.li.lineHeight ? props.itemStyle.li.lineHeight : '26px';
    // onMounted(() => {
    //     console.log("-----",props.item);
    // })

    // button文本
    // const addText = props.render.add_icon?.text ? props.render.add_icon?.text : ''
    // const deleteText = props.render.delete_icon?.text ? props.render.delete_icon?.text : ''
    // const editText = props.render.edit_icon?.text ? props.render.edit_icon?.text : ''
    // const selectText = props.render.select_icon?.text ? props.render.select_icon?.text : ''

    // event
    const itemEvents = {
        clickCheckboxEvent: props.switchItemEvents.clickCheckboxEvent,
        clickDropDownEvent: props.switchItemEvents.clickDropDownEvent,
        clickAddEvent: props.switchItemEvents.clickAddEvent,
        clickDeleteEvent: props.switchItemEvents.clickDeleteEvent,
        clickEditEvent: props.switchItemEvents.clickEditEvent,
        clickSelectEvent: props.switchItemEvents.clickSelectEvent,
        clickTitleEvent: props.switchItemEvents.clickTitleEvent
    };

    // 渲染并使增删改查事件可排序
    const eventsArray = [];
    // 增
    if (props.keys.add_icon && props.keys.add_icon != false) {
        eventsArray.push({
            text: props.render.add_icon?.text ? props.render.add_icon?.text : '',
            sort: props.render.add_icon?.sort ? props.render.add_icon?.sort : eventsArray.length,
            icon: props.render.add_icon?.icon ? props.render.add_icon?.icon : '',
            eventName: itemEvents.clickAddEvent
        });
    }
    // 删
    if (props.keys.delete_icon && props.keys.delete_icon != false) {
        eventsArray.push({
            text: props.render.delete_icon?.text ? props.render.delete_icon?.text : '',
            sort: props.render.delete_icon?.sort ? props.render.delete_icon?.sort : eventsArray.length,
            icon: props.render.delete_icon?.icon ? props.render.delete_icon?.icon : '',
            eventName: itemEvents.clickDeleteEvent
        });
    }
    // 改
    if (props.keys.edit_icon && props.keys.edit_icon != false) {
        eventsArray.push({
            text: props.render.edit_icon?.text ? props.render.edit_icon?.text : '',
            sort: props.render.edit_icon?.sort ? props.render.edit_icon?.sort : eventsArray.length,
            icon: props.render.edit_icon?.icon ? props.render.edit_icon?.icon : '',
            eventName: itemEvents.clickEditEvent
        });
    }
    // 查
    if (props.keys.select_icon && props.keys.select_icon != false) {
        eventsArray.push({
            text: props.render.select_icon?.text ? props.render.select_icon?.text : '',
            sort: props.render.select_icon?.sort ? props.render.select_icon?.sort : eventsArray.length,
            icon: props.render.select_icon?.icon ? props.render.select_icon?.icon : '',
            eventName: itemEvents.clickSelectEvent
        });
    }
    eventsArray.sort(compare('sort'));

    // 所有的emit
    // const emit = defineEmits(["addTreeItem"])

    // // 点击icon 展开和关闭
    // const iStatus = ref(false)
    // const expand = "expand"
    // const collapse = "collapse"
    // function expandFunc(e, id) {
    //     const classArray = e.target.className.split(" ")
    //     const itemGroupId = document.getElementById(props.itemGroupId)

    //     if (classArray[1] === 'expand') {
    //         // console.log(itemGroupId.style);
    //         iStatus.value = false
    //         itemGroupId.style.display = "none"
    //     }
    //     if (classArray[1] === 'collapse') {
    //         getTreeItemById(id)
    //         iStatus.value = true
    //         itemGroupId.style.display = ""
    //     }
    // }

    // // 点击当前item左边的下拉按钮图标（二级树节点接口数据）
    // async function getTreeItemById(parentId) {
    //     console.log(parentId);
    //     let items = await props.itemInterface(parentId)
    //     // if 容错
    //     if (items) {
    //         emit("addTreeItem", {data: items.data, parentId})
    //     }
    // }

    // 鼠标悬停事件（css的伪元素hover有bug）
    function mouseOverFunc(e) {
        props.render.mouse_over?.func(e);
    }

    function mouseLeaveFunc(e) {
        props.render.mouse_leave?.func(e);
    }

    // 鼠标点击事件（单击 & 双击）
    let clickCount = 0,
        clickTimer;

    function clickFunc(e) {
        // 处理 active - class
        if (props.itemStyle && props.itemStyle.li) {
            activeLiStyleFunc(e);
        }
        clickCount += 1;
        if (clickTimer) {
            clearTimeout(clickTimer);
        }
        let eventName = e.target.dataset.itemEvent || e.target.parentElement.dataset.itemEvent,
            dataset =
                e.target.dataset.itemInfo ||
                e.target.parentElement.dataset.itemInfo ||
                e.target.parentElement.parentElement.dataset.itemInfo ||
                e.target.parentElement.parentElement.parentElement.dataset.itemInfo ||
                e.target.parentElement.parentElement.parentElement.parentElement.dataset.itemInfo,
            itemGroupId =
                e.target.dataset.itemGroupId ||
                e.target.parentElement.dataset.itemGroupId ||
                e.target.parentElement.parentElement.parentElement.dataset.itemGroupId,
            id =
                e.target.dataset.id ||
                e.target.parentElement.dataset.id ||
                e.target.parentElement.parentElement.parentElement.dataset.id;

        if (!itemGroupId || !id) {
            console.log(e.target, e.target.dataset.itemGroupId, e.target.dataset.id);
        }
        let data = { e, eventName, dataset, itemGroupId, id };
        clickTimer = setTimeout(() => {
            if (clickCount === 1) {
                // console.log("区分 - 单击事件")
                props.render.click?.func(data);
            } else {
                // console.log("区分 - 双击事件")
                props.render.dbl_click?.func(data);
            }
            clickCount = 0;
        }, 250);
    }

    // Li - active - class
    function activeLiStyleFunc(e) {
        if (document.getElementsByClassName('treeItem')) {
            Array.from(document.getElementsByClassName('treeItem')).forEach((li) => {
                if (Array.from(li.classList).some((className) => className === props.itemStyle?.li?.activeClassName)) {
                    li.className = 'treeItem';
                }
            });
        }

        let li = null;
        if (e.target.tagName === 'LI') {
            li = e.target;
        } else if (e.target.parentElement.tagName === 'LI') {
            li = e.target.parentElement;
        } else if (e.target.parentElement.parentElement.tagName === 'LI') {
            li = e.target.parentElement.parentElement;
        } else if (e.target.parentElement.parentElement.parentElement.tagName === 'LI') {
            li = e.target.parentElement.parentElement.parentElement;
        } else if (e.target.parentElement.parentElement.parentElement.parentElement.tagName === 'LI') {
            li = e.target.parentElement.parentElement.parentElement.parentElement;
        } else {
            console.error('这有可能是个bug，请反馈');
        }

        if (li) {
            li.className = 'treeItem ' + props.itemStyle.li?.activeClassName;
        }
    }

    // onMounted(() => {
    //     console.log(props.hasChildDom);
    // })
</script>

<template>
    <li
        :id="item[keys.id]"
        :data-id="item[keys?.id]"
        :data-item-group-id="itemGroupId"
        :data-item-info="JSON.stringify(item)"
        class="treeItem"
        @click="clickFunc"
        @mouseleave="mouseLeaveFunc"
        @mouseover="mouseOverFunc"
    >
        <!-- 下拉 按钮 -->
        <!-- data-item-groupId-id 大写字母用‘-’连接符，否则都转为小写 -->
        <i
            v-if="item[keys?.hasChild] || item[keys?.children]"
            :class="[hasChildDom ? 'expand' : '']"
            :data-item-event="itemEvents?.clickDropDownEvent"
            class="ri-arrow-right-s-line collapse"
        ></i>
        <input
            v-if="keys.checkbox ? item[keys.checkbox] : false"
            :data-item-event="itemEvents?.clickCheckboxEvent"
            class="item-checkbox"
            type="checkbox"
        />
        <!-- 图标 -->
        <template v-if="keys.title_icon ? item[keys.title_icon] : false">
            <img v-if="item[keys.title_icon].search(/\.png|\.jpg|\.jpeg/) >= 0" :src="item[keys?.title_icon]" />
            <i v-else :class="item[keys?.title_icon]"></i>
        </template>
        <div class="info">
            <span>
                <!-- title -->
                <a :data-item-event="itemEvents?.clickTitleEvent">{{ item[keys?.title] }}</a>
                <!-- subTitle -->
                <span v-if="show_subTitle">
                    [
                    <span>{{ item[keys?.subTitle] }}</span>
                    ]
                </span>
            </span>
            <!-- 增 - 删 - 改 - 查 按钮 -->
            <span>
                <div v-for="item in eventsArray" :data-item-event="item.eventName">
                    <i :class="item.icon"></i>
                    <span>{{ item.text }}</span>
                </div>
            </span>
        </div>
    </li>
</template>

<style lang="scss" scoped>
    .treeItem {
        display: flex;
        align-items: center;
        box-sizing: border-box;
        letter-spacing: 0.14px;
        list-style-type: none;
        word-wrap: break-word;
        padding: 5px 5px;
        border: 1px solid rgba(231, 236, 237); /* 每一个 li 标签（即item）的边框颜色 */
        border-radius: 0.25rem;
        line-height: v-bind(lineHeight);
        color: var(--el-text-color-primary); /* 每一个 li 标签（即item）的文本颜色 */
        /* background-color: #fff; 每一个 li 标签（即item）的背景颜色 */
        font-size: 13px;
        cursor: pointer;

        & > i {
            margin-right: 3px;

            &:first-child {
                /* i图标[下拉按钮] */
                font-size: 18px;
            }
        }

        & > .item-checkbox {
            margin-right: 5px;
        }

        & > .info {
            display: flex;
            width: 100vw;
            justify-content: space-between;

            & > :first-child {
                & > a {
                    font-weight: bolder;
                }

                & > span {
                    & > span {
                        color: var(--el-color-primary); /* 每一个 li 标签（即item）的 subTitle 文本颜色 */
                    }
                }
            }

            & > :last-child {
                display: flex;
                color: var(--el-color-primary); /* 每一个 li 标签（即item）的 增删改查 文本颜色 */
                & div {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;

                    & span {
                        margin-right: 8px;
                    }
                }
            }
        }

        .collapse {
            animation: collapse 1s;
            animation-fill-mode: forwards;
        }

        .expand {
            animation: expand 1s;
            animation-fill-mode: forwards;
        }

        /* 活动状态的(li) - 测试 */
        /* &.active{
        background-color: #f40;
    } */
    }

    @keyframes expand {
        from {
            transform: rotate(0deg);
        }
        to {
            transform: rotate(90deg);
        }
    }

    @keyframes collapse {
        from {
            transform: rotate(90deg);
        }
        to {
            transform: rotate(0deg);
        }
    }
</style>
