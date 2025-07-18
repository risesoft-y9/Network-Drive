<template>
    <div
        id="left"
        :class="{
            narrow: menuCollapsed,
            'sidebar-separate': layoutSubName === 'sidebar-separate' ? true : false,
            'add-backgroundImage': settingStore.getMenuBg ? true : false
        }"
        :style="{ 'background-image': settingStore.getMenuBg ? 'url(' + settingStore.getMenuBg + ')' : '' }"
    >
        <div class="left-logo">
            <router-link class="logo-url" to="/">
                <img v-if="menuCollapsed" alt="y9-logo" src="@/assets/images/yun.png" />
                <span v-if="!menuCollapsed" class="logo-title">
                    <!-- <img alt="y9-logo" src="@/assets/images/yun.png" /> -->
                    <span style="margin-left: 5px; vertical-align: middle">{{ $t('网络硬盘') }}</span>
                </span>
            </router-link>
        </div>
        <div class="left-menu">
            <sider-menu
                :belongTopMenu="belongTopMenu"
                :defaultActive="defaultActive"
                :menuCollapsed="menuCollapsed"
                :menuData="menuData"
            ></sider-menu>
        </div>
        <!-- <div v-if="capacityShow" style="width: 80%;padding: 20px;text-align: center;">
          <el-progress :text-inside="true" :stroke-width="16" :percentage="percentage" />
          <div style="margin-top:10px;color: #606266;">
            {{remainingLength}}/{{capasitySize}}
          </div>
        </div> -->
    </div>
</template>
<script lang="ts" setup>
    import { onMounted, ref } from 'vue';
    import SiderMenu from '@/layouts/components/SiderMenu.vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import CapacityApi from '@/api/storage/capacity';
    // 注入 字体变量
    const fontSizeObj: any = inject('sizeObjInfo');
    const settingStore = useSettingStore();
    let fontMaxSize = settingStore.getMaxFontSize;
    let fontSize = settingStore.getTextFontSize;
    let lineHeight = settingStore.getLineHeight;
    const props = defineProps({
        menuCollapsed: {
            type: Boolean as computed<Boolean>,
            required: true
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
        },
        layoutSubName: {
            type: String as Ref<string>,
            required: true
        }
    });
    const capacityShow = ref(false);
    const capasitySize = ref('');
    const remainingLength = ref('');
    const percentage = ref();
    onMounted(() => {
        //getCapacityLength();
    });

    async function getCapacityLength() {
        let res = await CapacityApi.getCapacitySize();
        console.log(res);

        if (res.data != null) {
            capacityShow.value = true;
            capasitySize.value = res.data.capacitySize;
            remainingLength.value = res.data.remainingLength;
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/theme/global-vars.scss';

    #left .el-menu-item {
        height: v-bind('fontSizeObj.lineHeight') !important;
    }

    $sidebar-separate-margin-top: calc(#{$sidebar-separate-margin-left} + #{$headerHeight});
    $sidebar-separate-menu-height: calc(100vh - #{$sidebar-separate-margin-top});

    #left {
        display: flex;
        height: 100vh;
        flex-direction: column;
        width: $leftSideBarWidth;
        background-color: var(--el-bg-color);
        //background-color: #161b2d;
        //border-right: 1px solid #f8f8f8;
        transition-duration: 0.25s;

        &.sidebar-separate {
            position: absolute;
            z-index: 1;
            left: $sidebar-separate-margin-left;
            top: $sidebar-separate-margin-top;
            height: $sidebar-separate-menu-height;
            border-top-left-radius: 0.25rem;
            border-top-right-radius: 0.25rem;
            box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, 0.06);
        }

        .left-logo {
            width: 100%;
            height: $headerHeight;
            line-height: $headerHeight;
            text-align: center;
            vertical-align: middle;

            .logo-url {
                display: inline-block;
                width: 100%;
                height: 100%;
                overflow: hidden;

                .logo-title {
                    display: inline-block;
                    font-size: v-bind('fontSizeObj.extraLargeFont');
                    font-weight: 500;

                    color: var(--el-color-primary);
                }
            }

            img {
                width: $logoWidth;
                vertical-align: middle;
            }
        }

        .left-menu {
            flex: 1;
            overflow: hidden auto;
            scrollbar-width: none;

            & > ul {
                border-right: none;
                background-color: var(--el-bg-color);
                //background-color: #161b2d;
                :deep(a) {
                    text-decoration: none;

                    & > li {
                        //  font-size: 15px;
                        i {
                            margin-right: 15px;
                            font-size: v-bind('fontSizeObj.largeFontSize');
                        }

                        &.is-active {
                            color: var(--el-color-primary);
                            background-color: $background-color;
                        }
                    }

                    & li:hover {
                        background-color: var(--el-color-primary-light-9);
                        color: var(--el-color-primary-light-3);
                    }
                }
            }

            .left-scrollbar {
                width: 100%;
                height: 100%;
            }
        }

        &.narrow {
            width: $menu-collapsed-width;
        }

        @include scrollbar;
    }

    // 设置菜单背景时 css修改
    #left.add-backgroundImage {
        & > .left-logo {
            & > .logo-url .logo-title {
                color: var(--el-color-white);
            }
        }

        & > .left-menu {
            & > ul {
                background-color: transparent;
                background: transparent;

                :deep(a) {
                    text-decoration: none;

                    & > li {
                        color: var(--el-color-white);

                        &.is-active {
                            color: var(--el-color-primary);
                            background-color: var(--el-color-primary-light-9);
                        }
                    }

                    :hover {
                        color: var(--el-color-primary);
                        background-color: var(--el-color-primary-light-9);
                    }
                }

                :deep(li) {
                    .el-sub-menu__title {
                        color: var(--el-color-white);
                    }

                    div:hover {
                        color: var(--el-color-primary);
                        background-color: var(--el-color-primary-light-9);
                    }

                    ul > a:hover {
                        color: var(--el-color-primary);
                        background-color: var(--el-color-primary-light-9);
                    }
                }
            }
        }
    }
</style>
