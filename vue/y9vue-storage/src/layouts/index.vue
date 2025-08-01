<script lang="ts">
    import { computed, ComputedRef, nextTick, onBeforeMount, onMounted, Ref, ref, unref, watch } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { useRouterStore } from '@/store/modules/routerStore';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useRoute } from 'vue-router';
    import {
        BreadcrumbType,
        formatRoutePathTheParents,
        getBreadcrumbRoutes,
        getRouteBelongTopMenu,
        getRouteItem,
        getSelectLeftMenuPath,
        RoutesDataItem
    } from '@/utils/routes';

    import Y9Default from '@/layouts/Y9-default/index.vue';
    import Y9Horizontal from '@/layouts/Y9-horizontal/index.vue';
    import Y9Mobile from '@/layouts/Y9-mobile/index.vue';
    import OrgApi from '@/api/storage/org';
    import type { ElMessage } from 'element-plus';
    import { useI18n } from 'vue-i18n';

    const storageStore = useStorageStore();

    interface IndexLayoutSetupData {
        menuCollapsed: computed<Boolean>;
        tabNavEnable: boolean;
        belongTopMenu: ComputedRef<string>;
        menuData: RoutesDataItem[];
        defaultActive: Ref<string>;
        breadCrumbs: ComputedRef<BreadcrumbType[]>;
        routeItem: ComputedRef<RoutesDataItem>;
        layoutSubName?: Ref<string>;
    }

    export default {
        name: 'indexLayout',
        components: {
            Y9Default,
            Y9Horizontal,
            Y9Mobile
        },
        setup() {
            // PC网站配置
            const { locale } = useI18n();
            const settingStore = useSettingStore();
            const layoutSubName = ref('');
            const layoutName = computed<string>(() => {
                // horizontal布局时，menuCollapsed 必须为false，禁止折叠
                if (settingStore.getLayout === 'Y9Horizontal') {
                    settingStore.$patch({
                        menuCollapsed: false
                    });
                }
                const nameArray = settingStore.getLayout.split(' ');
                nameArray[1] ? (layoutSubName.value = nameArray[1]) : (layoutSubName.value = '');
                return nameArray[0];
            });

            // 主题切换
            const theme = computed(() => settingStore.getThemeName);

            watch(theme, () => {
                document.getElementsByTagName('html')[0].className = theme.value;
                if (document.getElementById('head')) {
                    let themeDom = document.getElementById('head');
                    let pathArray = themeDom.href.split('/');
                    pathArray[pathArray.length - 1] = theme.value + '.css';
                    let newPath = pathArray.join('/');
                    themeDom.href = newPath;
                }
            });

            // 移动端
            if (settingStore.getDevice === 'mobile') {
                settingStore.$patch({
                    layout: 'Y9Mobile',
                    settingWidth: '100%'
                });
            }
            const { toggleDevice } = settingStore;
            let changeDeviceTimeOut;
            const onScreenFunc = () => {
                changeDeviceTimeOut ? clearTimeout(changeDeviceTimeOut) : '';
                changeDeviceTimeOut = setTimeout(() => {
                    toggleDevice();
                }, 250);
            };
            settingStore.$subscribe(onScreenFunc);

            // 收缩左侧
            const menuCollapsed = computed<Boolean>(() => settingStore.getMenuCollapsed);

            // 所有菜单路由
            const routerStore = useRouterStore();
            const menuData: RoutesDataItem[] = routerStore.getPermissionRoutes;

            let route = useRoute();
            // 当前路由 item
            const routeItem = computed<RoutesDataItem>(() => getRouteItem(route.path, menuData));

            // 当前路由的父路由path[]
            const routeParentPaths = computed<string[]>(() => formatRoutePathTheParents(routeItem.value.path));

            // 当前路由的顶部菜单path
            const belongTopMenu = computed<string>(() => getRouteBelongTopMenu(routeItem.value));

            // 左侧选择的菜单
            const defaultActive = ref<string>(getSelectLeftMenuPath(routeItem.value));
            const { addTab } = routerStore;
            watch([routeItem], async () => {
                addTab(unref(routeItem));
                await nextTick();
                defaultActive.value = getSelectLeftMenuPath(routeItem.value);
            });

            // 面包屑导航
            const breadCrumbs = computed<BreadcrumbType[]>(() =>
                getBreadcrumbRoutes(routeItem.value, routeParentPaths.value, menuData)
            );

            onBeforeMount(() => {
                getAllPositionList();
            });

            // 挂载组件后初始化网站设置
            onMounted(() => {
                // 初始化主题
                document.getElementsByTagName('html')[0].className = theme.value;
            });

            const getAllPositionList = () => {
                OrgApi.getPositionList()
                    .then((res) => {
                        if (res.success) {
                            let positionId = sessionStorage.getItem('positionId')!;
                            let currentName = '';

                            res.data.positionList.forEach((item, index) => {
                                if (item.id == positionId) {
                                    currentName = item.name;
                                }
                            });
                            storageStore.$patch({
                                positionList: res.data.positionList,
                                currentPositionName: currentName,
                                tenantId: res.data.tenantId
                            });
                            console.log(storageStore.positionList);
                            sessionStorage.setItem('positionId', positionId);
                            sessionStorage.setItem('positionName', currentName);
                            sessionStorage.setItem('tenantId', res.data.tenantId);
                        }
                    })
                    .catch(() => {
                        ElMessage({ type: 'info', message: '数据加载失败' });
                    });
            };

            // 国际语言切换 仍有bug
            const webLanguage = computed(() => settingStore.getWebLanguage);
            watch(webLanguage, () => {
                // 修复打包后的国际语言切换和主题切换 的问题
                locale.value = webLanguage.value;
            });

            return {
                layoutName,
                layoutSubName,
                menuData,
                menuCollapsed,
                belongTopMenu,
                defaultActive,
                breadCrumbs,
                routeItem,
                getAllPositionList
            };
        }
    };
</script>
<template>
    <component
        :is="layoutName"
        :key="layoutName"
        ref="indexLayoutRef"
        :belongTopMenu="belongTopMenu"
        :breadCrumbs="breadCrumbs"
        :defaultActive="defaultActive"
        :layoutName="layoutName"
        :layoutSubName="layoutSubName"
        :menuCollapsed="menuCollapsed"
        :menuData="menuData"
        :routeItem="routeItem"
    ></component>
</template>
<style lang="scss" scoped>
    :global(.el-icon) {
        color: var(--el-color-primary) !important;
    }

    :global(.el-message__content) {
        color: var(--el-color-primary) !important;
    }
</style>
