<!--
 * @Author: your name
 * @Date: 2022-01-11 18:38:31
 * @LastEditTime: 2022-04-13 20:56:34
 * @LastEditors: hongzhew
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: /sz- team-frontend-9.6.x/y9vue-storage/src/layouts/components/SiderMenuItem.vue
-->
<template>
    <template v-if="!item.hidden">
        <template v-if="item.children && Array.isArray(item.children) && hasChildRoute(item.children)">
            <el-sub-menu :hide-timeout="3000000" :index="item.path" class="y9-el-sub-menu">
                <template #title>
                    <i v-if="item.meta.icon" :class="['icon', item.meta.icon]" />
                    <span>{{ $t(item.meta.title) }}</span>
                </template>
                <sider-menu-item
                    v-for="item2 in item.children"
                    :key="item2.path"
                    :belongTopMenu="belongTopMenu"
                    :routeItem="item2"
                ></sider-menu-item>
            </el-sub-menu>
        </template>
        <template v-else>
            <a-link :to="item.path">
                <el-menu-item :index="item.path" @click="toggleCollapsedFunc">
                    <!-- <Icon v-if="item.meta.icon" :type="item.meta.icon" class="icon" /> -->
                    <i v-if="item.meta.icon" :class="['icon', item.meta.icon]" />
                    <template #title>{{ $t(item.meta.title) }}</template>
                </el-menu-item>
            </a-link>
        </template>
    </template>
</template>
<script lang="ts">
    import { computed, ComputedRef, defineComponent, PropType, Ref, toRefs } from 'vue';
    import { getRouteBelongTopMenu, hasChildRoute, RoutesDataItem } from '@/utils/routes';
    import { useSettingStore } from '@/store/modules/settingStore';
    import ALink from '@/layouts/components/ALink/index.vue';
    import Icon from './Icon.vue';

    interface SiderMenuItemSetupData {
        item: Ref;
        topMenuPath: ComputedRef<string>;
        hasChildRoute: (children: RoutesDataItem[]) => boolean;
        toggleCollapsedFunc: () => void;
        fontSizeObj: Object;
    }

    export default defineComponent({
        name: 'SiderMenuItem',
        props: {
            routeItem: {
                type: Object as PropType<RoutesDataItem>,
                required: true
            },
            belongTopMenu: {
                type: String,
                default: ''
            }
        },
        components: {
            ALink,
            Icon
        },
        setup(props): SiderMenuItemSetupData {
            // 注入 字体变量
            const fontSizeObj: any = inject('sizeObjInfo');
            const { routeItem } = toRefs(props);
            const topMenuPath = computed<string>(() => getRouteBelongTopMenu(routeItem.value as RoutesDataItem));

            // console.log(routeItem.value);
            const settingStore = useSettingStore();
            const { toggleCollapsed } = settingStore;
            const toggleCollapsedFunc = () => {
                if (settingStore.getDevice === 'mobile') {
                    toggleCollapsed();
                }
            };

            return {
                item: routeItem,
                topMenuPath: topMenuPath,
                hasChildRoute,
                toggleCollapsedFunc,
                fontSizeObj
            };
        }
    });
</script>

<style lang="scss" scoped>
    .y9-el-sub-menu {
        & > div {
            text-decoration: none;

            i {
                font-size: v-bind('fontSizeObj.largeFontSize');
                margin-right: 15px;
            }

            span {
                font-size: v-bind('fontSizeObj.baseFontSize');
            }
        }
    }

    :deep(.el-menu-item) {
        font-size: v-bind('fontSizeObj.baseFontSize');

        .el-icon {
            font-size: v-bind('fontSizeObj.baseFontSize');
            color: inherit;
            margin-left: -3px;
            padding: 0;
            margin-right: 12px !important;
        }
    }

    .el-teleport,
    .el-popper {
        ul.el-menu {
            & > a {
                text-decoration: none;
            }

            li.el-menu-item {
                & > i {
                    font-size: v-bind('fontSizeObj.largeFontSize');
                    margin-right: 15px;
                }
            }
        }
    }

    .el-menu {
        background-color: none;
    }
</style>
<style>
    .y9-el-sub-menu.el-sub-menu .el-menu {
        background: transparent;
    }
</style>
