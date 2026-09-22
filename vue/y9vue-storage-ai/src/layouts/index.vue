<script lang="ts" setup>
    import { computed, nextTick, onBeforeMount, onMounted, onUnmounted, ref, unref, watch } from 'vue';
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
    import { ElMessage } from 'element-plus';
    import { useI18n } from 'vue-i18n';

    defineOptions({ name: 'indexLayout' });

    // 全局状态初始化
    const settingStore = useSettingStore();
    const routerStore = useRouterStore();
    const storageStore = useStorageStore();
    const route = useRoute();
    const { locale } = useI18n();

    // 布局配置
    const layoutSubName = ref('');
    const layoutName = computed<string>(() => {
        // 水平布局强制禁用菜单折叠
        if (settingStore.getLayout === 'Y9Horizontal') {
            settingStore.menuCollapsed = false;
        }
        const [name, subName = ''] = settingStore.getLayout.split(' ');
        layoutSubName.value = subName;
        return name;
    });

    // 动态组件安全映射，避免直接字符串渲染组件
    const layoutComponent = computed(() => {
        const componentMap: Record<string, typeof Y9Default> = {
            Y9Default,
            Y9Horizontal,
            Y9Mobile
        };
        return componentMap[layoutName.value] || Y9Default;
    });

    // 主题切换逻辑
    const theme = computed(() => settingStore.getThemeName);
    const updateThemeCss = (newTheme: string) => {
        // 安全更新根节点主题类
        document.documentElement.className = newTheme;

        // 安全更新主题样式链接，避免路径拼接错误
        const themeLink = document.getElementById('head') as HTMLLinkElement | null;
        if (!themeLink?.href) return;

        try {
            const url = new URL(themeLink.href, window.location.origin);
            const pathSegments = url.pathname.split('/');
            pathSegments[pathSegments.length - 1] = `${newTheme}.css`;
            url.pathname = pathSegments.join('/');

            // 仅在路径变化时更新，避免不必要的资源重载
            if (themeLink.href !== url.toString()) {
                themeLink.href = url.toString();
            }
        } catch (err) {
            console.error('主题样式更新失败:', err);
        }
    };

    watch(theme, (newTheme) => updateThemeCss(newTheme));

    // 移动端适配逻辑
    if (settingStore.getDevice === 'mobile') {
        settingStore.layout = 'Y9Mobile';
        settingStore.settingWidth = '100%';
    }

    const { toggleDevice } = settingStore;
    let resizeTimer: ReturnType<typeof setTimeout> | null = null;
    const handleScreenResize = () => {
        if (resizeTimer) clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => toggleDevice(), 250);
    };
    const unsubscribeSetting = settingStore.$subscribe(handleScreenResize);

    // 菜单与路由联动逻辑
    const menuCollapsed = computed(() => settingStore.getMenuCollapsed);
    const menuData = computed<RoutesDataItem[]>(() => routerStore.getPermissionRoutes);
    const routeItem = computed<RoutesDataItem>(() => getRouteItem(route.path, menuData.value));
    const routeParentPaths = computed<string[]>(() => formatRoutePathTheParents(routeItem.value.path));
    const belongTopMenu = computed<string>(() => getRouteBelongTopMenu(routeItem.value));
    const defaultActive = ref<string>(getSelectLeftMenuPath(routeItem.value));
    const { addTab } = routerStore;

    watch(
        routeItem,
        async () => {
            addTab(unref(routeItem));
            await nextTick();
            defaultActive.value = getSelectLeftMenuPath(routeItem.value);
        },
        { immediate: true }
    );

    // 面包屑导航
    const breadCrumbs = computed<BreadcrumbType[]>(() =>
        getBreadcrumbRoutes(routeItem.value, routeParentPaths.value, menuData.value)
    );

    // 国际化切换
    const webLanguage = computed(() => settingStore.getWebLanguage);
    watch(
        webLanguage,
        (newLang) => {
            locale.value = newLang;
        },
        { immediate: true }
    );

    // 岗位列表预加载（storage 业务定制：写入 storageStore 与 sessionStorage）
    const getAllPositionList = () => {
        OrgApi.getPositionList()
            .then((res) => {
                if (res.success) {
                    let positionId = sessionStorage.getItem('positionId')!;
                    let currentName = '';

                    res.data.positionList.forEach((item) => {
                        if (item.id == positionId) {
                            currentName = item.name;
                        }
                    });
                    storageStore.$patch({
                        positionList: res.data.positionList,
                        currentPositionName: currentName,
                        tenantId: res.data.tenantId
                    });
                    sessionStorage.setItem('positionId', positionId);
                    sessionStorage.setItem('positionName', currentName);
                    sessionStorage.setItem('tenantId', res.data.tenantId);
                }
            })
            .catch(() => {
                ElMessage({ type: 'info', message: '数据加载失败' });
            });
    };

    onBeforeMount(() => {
        getAllPositionList();
    });

    // 生命周期管理
    onMounted(() => updateThemeCss(theme.value));

    onUnmounted(() => {
        // 清理所有副作用，避免内存泄漏
        unsubscribeSetting();
        if (resizeTimer) clearTimeout(resizeTimer);
    });
</script>

<template>
    <component
        :is="layoutComponent"
        :key="layoutName"
        ref="indexLayoutRef"
        :belong-top-menu="belongTopMenu"
        :bread-crumbs="breadCrumbs"
        :default-active="defaultActive"
        :layout-name="layoutName"
        :layout-sub-name="layoutSubName"
        :menu-collapsed="menuCollapsed"
        :menu-data="menuData"
        :route-item="routeItem"
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
