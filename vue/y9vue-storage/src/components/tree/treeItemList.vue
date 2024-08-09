<script lang="ts" setup>
import { ref } from "vue";
import TreeItem from "./treeItem.vue";
const props = defineProps({
    treeId: {
        type: String,
        default: ""
    },
    itemList: Array,
    itemInterface: {
        type: Object,
        default: () => { }
    },
    itemGroupPrefix: {
        type: String,
        default: 'itemGroup-'
    },
    keys: {
        type: Object,
        default: () => { }
    },
    render: {
        type: Object,
        default: () => { }
    },
    switchItemEvents: {
        type: Object,
        default: () => { }
    },
    hasChildDom: {      // 默认false    true 有真实的子节点
        type: Boolean,
        default: false
    },
    itemStyle: {
        type: Object,
        default: () => { }
    },
    dragEvent: {
        type: Object,
        default: () => { 
            return {
                draggable: false
            }
        }
    },
    animation: {
        type: Object,
        default: () => {
            return {
                in: "fadeInLeftBig",
                out: "fadeOutRight"
            }
        }
    }
})

const gap = props.itemStyle.li.gap?props.itemStyle.li.gap:"3px";
const pding = props.itemStyle.li.padding?props.itemStyle.li.padding:"15px";

// onMounted(() => {
//     console.log(pding);
    
// })
// emit 再续传给父
// const emit = defineEmits(['addTreeItem'])
// function addTreeItemFunc(data) {
//     emit('addTreeItem', {...data, itemGroupPrefix: props.itemGroupPrefix})
// }

// 利用maosedown事件判断，动态初始化可拖放对象
// function mouseDownFunc(e) {
//     console.log("mousedown ----> ",e.target.parentNode);
//     e.target.parentNode.setAttribute("draggable", true)
//     e.target.parentNode.addEventListener("ondragstart", ondragstart(e))
//     e.target.parentNode.addEventListener("ondrag", ondrag(e))
//     e.target.parentNode.addEventListener("ondragend", ondragend(e))
// }



// 拖放功能
// 当前被拖放的dom节点对象
const dragObj = reactive({
    item: "",
    item_parentElement: "",                  // 当前被拖放的dom节点的父节点
    item_preElement: "",                 // 被拖放的dom节点的前一个节点
    item_preElement_parentNode: "",      // 被拖放的dom节点的前一个节点的父节点
    item_nextElement: "",                // 被拖放的dom节点的下一个节点
    item_nextElement_parentNode: "",     // 被拖放的dom节点的下一个节点的父节点
    item_clientHeight: "",                // 拖放元素的高度（理论上也是每个item的高度）
    item_topBorder_pageY: "",             // 被拖放元素的上边距和页面可视区域顶端的距离
    item_bottomBorder_pageY: "",           // 被拖放元素的下边距和页面可视区域顶端的距离
    theTreeTopHeight: 0,                    // 整颗树的上边界高度
    theTreeBottomHeight: 0,                  // 整颗树的下边界高度
    theTreeLeftWidth: 0,                    // 整颗树的左边界距离
    theTreeRightWidth: 0,                   // 整颗树的右边界距离
    theTree: ""                             // 整颗树节点
})


// 判断下一个节点的高度视觉高度(即是否展开)
function checkElementNodeHeight(element) {
    const height = 38   // 树节点高度
    if (element.clientHeight > height) {
        return true
    } else {
        return false
    }
}


// 判断方向
const directionRef = ref('')   // 存储判定的方向
let lastX = null,   // 移动鼠标后的X坐标参数
    lastY = null    // 移动鼠标后的X坐标参数
function checkDirection(event) {
    let curX = event.pageX, // 当前鼠标位置的X坐标
        curY = event.pageY  // 当前鼠标位置的Y坐标
    // 初始化坐标
    if (lastX == null || lastY == null) {
        lastX = curX
        lastY = curY
        return;
    }
    if (event.pageX !== 0 && curX > lastX) {
        directionRef.value = "right"
        // console.log('right')
    } else if (event.pageX !== 0 && curX < lastX) {
        directionRef.value = "left"
        // console.log('left')
    } else {
        directionRef.value = "X-Center"
        // console.log('X-center')
    }
    if (event.pageY !== 0 && curY > lastY) {
        directionRef.value = "down"
        // console.log('down')
    } else if (event.pageY !== 0 && curY < lastY) {
        directionRef.value = "up"
        // console.log('up')
    } else {
        // Y方向未移动时
        directionRef.value = "Y-Center"
        // console.log('Y-center')
    }
    lastX = curX
    lastY = curY
}

// 递归获取下一个节点
function getUpNextNode(nextNode) {
    if (checkElementNodeHeight(nextNode.children[1].lastElementChild)) {
        return getUpNextNode(nextNode.children[1].lastElementChild)
    } else {
        return nextNode.children[1].lastElementChild
    }
}


// 向上移动
function upMoving(e) {
    // 向上，遇到伪节点
    try {
        if (e.target.previousSibling.nodeName !== "#text") {
            dragObj.item_preElement = e.target.previousSibling
            dragObj.item_preElement_parentNode = e.target.previousSibling.parentNode
        } else {
            dragObj.item_preElement = null
        }
        // 向上，遇到已经展开的节点（需要递归遍历，否则祖孙节点的关系时，会报错）
        if (dragObj.item_preElement && checkElementNodeHeight(dragObj.item_preElement)) {
            dragObj.item_preElement = getUpNextNode(dragObj.item_preElement)
            dragObj.item_preElement_parentNode = dragObj.item_preElement.parentNode
            // dragObj.item_preElement_parentNode = dragObj.item_preElement.children[1]
            // dragObj.item_preElement = dragObj.item_preElement_parentNode.lastElementChild
            // console.log("向上，遇到已经展开的节点 =====", dragObj.item_preElement_parentNode);
        }
        // 向上，遇到伪节点时
        if (!dragObj.item_preElement) {
            dragObj.item_preElement = e.target.parentNode.parentNode
            dragObj.item_preElement_parentNode = dragObj.item_preElement.parentNode
            // console.log("向上，遇到伪节点时", dragObj.item_preElement);
        }
    } catch (error) {
        console.log("upMoving");
    }

}

// 判断是否伪节点 向上递归
function iterationSpecialNode(node) {
    if (node.nodeName === "#text" || !node.nextSibling || node.nextSibling.nodeName === "#text") {
        iterationSpecialNode(node.parentNode)
    } else {
        dragObj.item_nextElement = node.nextSibling
        dragObj.item_nextElement_parentNode = dragObj.item_nextElement.parentNode
        exchangeElement(dragObj.item_nextElement_parentNode, dragObj.item, dragObj.item_nextElement)
        // console.log(node.nextSibling)
    }
}
// 向下移动
const count_fakeDom = ref(0), specialNode = ref('')
function downMoving(e) {
    // 向下，遇到伪节点时
    try {
        if (e.target.nextSibling.nodeName !== "#text") {
            dragObj.item_nextElement = e.target.nextSibling.nextSibling
        } else {
            // 向上递归
            iterationSpecialNode(e.target.nextSibling)
            // dragObj.item_nextElement = e.target.nextSibling
        }


        // 向下 遇到已经展开的节点（孙-爷 节点的关系时，会报错）
        if (directionRef.value === "down" && checkElementNodeHeight(dragObj.item_nextElement.previousSibling)) {
            dragObj.item_nextElement_parentNode = dragObj.item_nextElement.previousSibling.children[1]
            dragObj.item_nextElement = dragObj.item_nextElement_parentNode.firstElementChild
        } else {
            dragObj.item_nextElement_parentNode = dragObj.item_nextElement.parentNode
        }


    } catch (error) {
        console.log("downMoving");
        // exchangeElement(specialNode.value.parentNode, dragObj.item, specialNode.value.nextSibling)
        // console.log(dragObj.item_nextElement_parentNode, dragObj.item, dragObj.item_nextElement, specialNode.value);
    }
}

// 初始化参数
const sessionAnimationStyle = {}
function initDragParams(e) {
    console.log("initDragParams ----> ")
    if (e.target) {
        dragObj.item = e.target
        dragObj.item_clientHeight = e.target.clientHeight
        dragObj.item_topBorder_pageY = e.pageY - e.offsetY
        dragObj.item_bottomBorder_pageY = dragObj.item_topBorder_pageY + e.target.clientHeight
        dragObj.item.className = "activedrapClass"
    }

    // 设置当前节点的父节点
    if (dragObj.item.parentNode.id === props.treeId) {
        dragObj.item_parentElement = dragObj.item.parentNode
    }else{
        dragObj.item_parentElement = dragObj.item.parentNode.parentNode
    }

    // console.log(dragObj);
    // 将子集元素的入场动画缓存（报错：Indexed property setter is not supported）
    // if (dragObj.item.children[1].children.length) {
        // for (const key in dragObj.item.style) {
        //     if (Object.prototype.hasOwnProperty.call(dragObj.item.style, key)) {
        //         sessionAnimationStyle[key] = dragObj.item.style[key];
        //     }
        // }
        // sessionAnimationStyle["0"] = dragObj.item.style["0"]
        // sessionAnimationStyle["1"] = dragObj.item.style["1"]
        // dragObj.item.style["0"] = "_000_"
        // dragObj.item.style["1"] = "_111"
        // console.log(dragObj.item.style, sessionAnimationStyle);
    // }
    // console.log(dragObj);

    switch (directionRef.value) {
        case "up":
            // up方向 - 同级节点逻辑处理
            upMoving(e)
            break;
        case "down":
            // down方向 - 同级节点逻辑处理
            downMoving(e)
            break;
        default:
            break;
    }

}


// 交换元素
function exchangeElement(parentNode, item, next) {
    try {
        parentNode.insertBefore(item, next)
    } catch (error) {
        console.log("exchangeElement error");
    }

}
// 拖动时
function ondrag(e) {
    // 判断方向
    checkDirection(e)
    initDragParams(e, directionRef.value)

    // 展开子节点的状态时，高度是包含所有子节点的高度，调整为单个节点的高度
    if (dragObj.item_clientHeight > 38) {
        dragObj.item_clientHeight = 38
    }

    // 什么时候进行Dom节点交换 && 确定拖动边界
    let up_PageY = 0, down_PageY = 0, over = false
    // console.log(dragObj);
    if (e.pageX > dragObj.theTreeLeftWidth && e.pageX < dragObj.theTreeRightWidth && e.pageY > dragObj.theTreeTopHeight && e.pageY < dragObj.theTreeBottomHeight) {
        up_PageY = dragObj.item_topBorder_pageY - dragObj.item_clientHeight
        down_PageY = dragObj.item_bottomBorder_pageY + dragObj.item_clientHeight
    } else {
        up_PageY = 0
        down_PageY = 0
    }


    // 向上移动元素，交换位置
    if (directionRef.value === "up" && up_PageY && e.pageY < up_PageY) {
        // console.log("向上移动元素，交换位置", dragObj.item_preElement, dragObj.item_preElement_parentNode.children);
        exchangeElement(dragObj.item_preElement_parentNode, dragObj.item, dragObj.item_preElement)
        return;
    }


    // 向下移动元素，交换位置
    if (directionRef.value === "down" && down_PageY && e.pageY > down_PageY) {
        // console.log("向下移动元素，交换位置", dragObj.item_nextElement);
        exchangeElement(dragObj.item_nextElement_parentNode, dragObj.item, dragObj.item_nextElement)
        return;
    }

    // 超出下边界
    if (!down_PageY && e.pageY !== 0 && e.pageY > dragObj.theTreeBottomHeight && !over) {
        dragObj.item_preElement = dragObj.theTree.lastElementChild
        dragObj.theTree.appendChild(dragObj.item)
        over = true
        // console.log("超出下边界", dragObj);
    }
    // 超出上边界
    if (!up_PageY && e.pageY !== 0 && e.pageY < dragObj.theTreeTopHeight && !over) {
        dragObj.item_nextElement = dragObj.theTree.firstElementChild
        dragObj.theTree.insertBefore(dragObj.item, dragObj.theTree.firstElementChild)
        over = true
        // console.log("超出上边界", dragObj);
    }

}

// 拖动开始
function ondragstart(e) {
    // console.log("start ----> ")
    dragObj.theTree = document.getElementById(props.treeId)
    dragObj.theTreeTopHeight = dragObj.theTree.offsetTop
    dragObj.theTreeBottomHeight = dragObj.theTree.offsetTop + dragObj.theTree.offsetHeight
    dragObj.theTreeLeftWidth = dragObj.theTree.offsetLeft
    dragObj.theTreeRightWidth = dragObj.theTree.offsetLeft + dragObj.theTree.offsetWidth
}
// 拖动结束
function ondragend(e) {
    // console.log("end ----> ")
    dragObj.item.className = ""
    // 将子集元素的入场动画缓存（报错：Indexed property setter is not supported）
    // for (const key in sessionAnimationStyle) {
    //     if (Object.prototype.hasOwnProperty.call(sessionAnimationStyle, key)) {
    //         dragObj.item.style[key] = sessionAnimationStyle[key];
    //     }
    // }
    // dragObj.item.style["0"] = sessionAnimationStyle["0"]
    // dragObj.item.style["1"] = sessionAnimationStyle["1"]

    let params = {}
    // 先判断是否在树一级节点
    params.itemInfo = dragObj.item.children[0].dataset.itemInfo
    // 父节点
    if (dragObj.item_parentElement.id !== props.treeId) {
        params.parentItemInfo = dragObj.item_parentElement.children[0].dataset.itemInfo
    }else{
        params.parentItemInfo = null
    }
    // 前一个节点
    if (dragObj.item_preElement.children?.length) {
        params.preItemInfo = dragObj.item_preElement.children[0].dataset.itemInfo
    }else{
        params.preItemInfo = null
    }
    // 后一个节点
    if (dragObj.item_nextElement.children?.length) {
        params.nextItemInfo = dragObj.item_nextElement.children[0].dataset.itemInfo
    }else{
        params.nextItemInfo = null
    }
    

    // 封装接口传输拖放元素最新位置信息
    if (props.dragEvent.dragendCallback) {
        props.dragEvent.dragendCallback(params)
    }


}
</script>

<template>
    <template v-for="item in itemList" :key="item[keys?.id]">
        <ol
            :draggable="dragEvent.draggable"
            @dragstart.stop="ondragstart"
            @drag.stop="ondrag"
            @dragend.stop="ondragend"
        >
            <TreeItem
                :item="item"
                :key="item[keys?.id]"
                :itemGroupId="itemGroupPrefix + item[keys?.id]"
                :itemInterface="itemInterface"
                :keys="keys"
                :render="render"
                :switchItemEvents="switchItemEvents"
                :hasChildDom="item[keys?.children]?.length > 0 ? true : false"
                :itemStyle="itemStyle"
            ></TreeItem>
            <ol :id="itemGroupPrefix + item[keys?.id]">
                <treeItemList
                    v-if="item[keys?.children]"
                    :treeId="treeId"
                    :key="treeId"
                    :itemList="item[keys?.children]"
                    :keys="keys"
                    :render="render"
                    :switchItemEvents="switchItemEvents"
                    :search="search"
                    :hasChildDom="item[keys?.children]?.length > 0 ? true : false"
                    :itemStyle="itemStyle"
                    :dragEvent="dragEvent"
                ></treeItemList>
            </ol>
        </ol>
    </template>
</template>

<!-- <style>
ol {
    
    list-style-type: none;
}
</style> -->
<style lang="scss" scoped>
ol {
    margin-block-start: v-bind(gap);
    margin-block-end: v-bind(gap);
    padding-inline-start: 0px;
    list-style-type: none;
    ol {
        padding-inline-start: 15px;
        // padding-inline-start: v-bing(pding);
        // padding-inline-start: 15px;
    }
}
.expandAnimation-fadeInLeft {
    // animation: expandAnimation 1s;
    // animation-fill-mode: forwards;
    animation: v-bind(
        "animation.in"
    ); /* referring directly to the animation's @keyframe declaration */
    animation-duration: 1s;
}
.collapseAnimation-fadeOutRight {
    // animation: expandAnimation 1s;
    // animation-fill-mode: forwards;
    animation: v-bind(
        "animation.out"
    ); /* referring directly to the animation's @keyframe declaration */
    animation-duration: 1s;
}

.activedrapClass {
    :first-child {
        border: 1px dashed blue;
        background-color: blue !important;
        color: blue;
    }
}
</style>
