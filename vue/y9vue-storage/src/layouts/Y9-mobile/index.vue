<template>
    <div id="indexlayout">
        <el-drawer v-model="menuDrawer" :direction="direction" :size="size" z-index="2000" @close="toggleCollapsedFunc">
            <template #default>
                <Left
                    :belongTopMenu="belongTopMenu"
                    :defaultActive="defaultActive"
                    :layoutSubName="layoutSubName"
                    :menuCollapsed="!menuCollapsed"
                    :menuData="menuData"
                />
            </template>
        </el-drawer>
        <div id="indexlayout-right" class="fiexd-header">
            <RightTop :menuCollapsed="menuCollapsed" />
            <div
                :class="{
                    'indexlayout-right-main': true
                }"
            >
                <BreadCrumbs :list="breadCrumbs"></BreadCrumbs>
                <router-view></router-view>
            </div>
        </div>
        <SettingsMobile />
    </div>
    <!-- <component :is="settingStore.getLockScreen ? Lock : ''"></component> -->
    <Lock v-show="settingStore.getLockScreen" />
    <Search />
</template>

<script lang="ts" setup>
    import { computed } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import Left from './Left.vue';
    import RightTop from './RightTop.vue';
    import SettingsMobile from '@/layouts/components/SettingsMobile.vue';
    import BreadCrumbs from '@/layouts/components/BreadCrumbs/index.vue';
    import Lock from '@/layouts/components/Lock/index.vue';
    import Search from '@/layouts/components/search/index.vue';

    const props = defineProps({
        layoutSubName: {
            type: String as Ref<string>,
            required: true
        },
        menuData: {
            type: Object as RoutesDataItem[],
            required: true
        },
        menuCollapsed: {
            type: Boolean,
            required: true
        },
        belongTopMenu: {
            type: String as ComputedRef<string>,
            required: true
        },
        defaultActive: {
            type: String as Ref<string>,
            required: true
        },
        breadCrumbs: {
            type: Array as ComputedRef<BreadcrumbType[]>,
            required: true
        },
        routeItem: {
            type: Object as ComputedRef<RoutesDataItem>,
            required: true
        }
    });

    // 菜单
    const settingStore = useSettingStore();
    const direction = computed(() => settingStore.getMenuAnimation);
    const size = computed(() => settingStore.getMenuWidth);
    const menuDrawer = computed(() => settingStore.getMenuCollapsed);
    const toggleCollapsedFunc = () => {
        settingStore.$patch({
            menuCollapsed: !settingStore.getMenuCollapsed
        });
    };
</script>

<style lang="scss" scoped>
    @import '@/theme/global-vars.scss';

    #indexlayout {
        display: flex;
        height: 100vh;
        overflow: hidden;
    }

    #indexlayout-right {
        position: relative;
        flex: 1;
        overflow: auto;
        background-color: var(--bg-color);

        &.fiexd-header {
            display: flex;
            flex-direction: column;

            .indexlayout-right-main {
                flex: 1;
                overflow: auto;
                background-color: var(--el-color-primary-light-9);
                padding: $mobile-main-padding;
                padding-top: 0;

                // &.sidebar-separate {
                //     padding-left: calc(
                //         #{$leftSideBarWidth} + #{$sidebar-separate-margin-left} + #{$main-padding}
                //     );
                // }
                // &.sidebar-separate-menuCollapsed {
                //     padding-left: calc(
                //         54px + #{$sidebar-separate-margin-left} + #{$main-padding}
                //     );
                //     transition-duration: 0.2s;
                // }
                & > .breadcrumbs {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    height: $headerBreadcrumbHeight;
                }
            }
        }
    }

    .indexlayout-main-conent {
        margin: 24px;
        position: relative;
    }
</style>
