<!--
 * @Author: hongzhew
 * @Date: 2022-03-28 09:48:44
 * @LastEditors: hongzhew
 * @LastEditTime: 2022-04-07 17:37:43
 * @Description:
-->
<script lang="ts" setup>
    import SiderMenu from '@/layouts/components/SiderMenu.vue';
    // 注入 字体变量
    const fontSizeObj: any = inject('sizeObjInfo');
    const props = defineProps({
        menuCollapsed: {
            type: Boolean,
            default: false
        },
        belongTopMenu: {
            type: String,
            default: ''
        },
        defaultActive: {
            type: String,
            default: ''
        },
        menuData: {
            type: Array,
            default: () => {
                return [];
            }
        }
    });
</script>

<template>
    <div id="header-menus">
        <sider-menu
            :belongTopMenu="belongTopMenu"
            :defaultActive="defaultActive"
            :menuCollapsed="menuCollapsed"
            :menuData="menuData"
            menuMode="horizontal"
        ></sider-menu>
    </div>
</template>

<style lang="scss" scoped>
    #header-menus {
        width: 100%;
        overflow: auto;
        box-shadow: 2px 2px 2px 1px rgb(0 0 0 / 6%);
        z-index: 2;

        & > ul {
            border-right: none;
            padding-left: 10px;

            :deep(a) {
                text-decoration: none;

                & > li {
                    color: var(--el-text-color-primary);
                    background-color: var(--el-bg-color);
                    height: 61px;

                    i {
                        margin-right: 10px;
                        font-size: v-bind('fontSizeObj.largeFontSize');
                    }
                }

                & > li:hover {
                    background-color: var(--el-color-primary-light-9);
                }

                & > .el-menu-item.is-active {
                    color: var(--el-menu-active-color);
                    background-color: #eff1f7;
                }
            }
        }
    }
</style>

<!-- Workaround bug #6378 -->
<style lang="scss">
    // 精确定位，尽量避开全局污染
    .el-menu--horizontal > ul.el-menu.el-menu--popup.el-menu--popup-bottom-start > a {
        text-decoration: none;

        & > li.el-menu-item {
            text-align: center;
            color: var(--el-text-color-primary);
            background-color: var(--el-bg-color);

            i {
                margin-right: 10px;
            }
        }

        & > li.el-menu-item:hover {
            background-color: var(--el-color-primary-light-9);
        }
    }
</style>
