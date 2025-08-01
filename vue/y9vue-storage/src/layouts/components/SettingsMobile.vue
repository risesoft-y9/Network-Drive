<script lang="ts" setup>
    import { computed, onMounted, reactive, ref } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';

    // 数据响应
    const settingStore = useSettingStore();
    const webSettingVisible = ref(false);
    const settingPageAnimationdirection = computed(() => settingStore.getSettingAnimation);
    const size = computed(() => settingStore.getSettingWidth);
    const device = computed(() => settingStore.getDevice);

    // 监听设置事件
    onMounted(() => {
        setTimeout(() => {
            document.getElementsByClassName('web-setting')[0].addEventListener('click', () => {
                if (!webSettingVisible.value) {
                    webSettingVisible.value = true;
                }
            });
        }, 500);
    });

    const form = reactive({
        allPcLayout: settingStore.getAllPcLayout,
        pcLayout: settingStore.getLayout,
        webName: settingStore.getWebName,
        logoSvgName: settingStore.getLogoSvgName,
        webLanguage: settingStore.getWebLanguage,
        fontSize: settingStore.getFontSize,
        themeName: settingStore.getThemeName,
        menuAnimation: settingStore.menuAnimation,
        menuStyle: settingStore.getMenuStyle,
        menuWidth: settingStore.getMenuWidth,
        showLabel: settingStore.getShowLabel,
        showLabelIcon: settingStore.getShowLabelIcon,
        labelStyle: settingStore.getLabelStyle,
        // separateStyle: settingStore.getSeparateStyle,
        fixedHeader: settingStore.getFixedHeader,
        progress: settingStore.getProgress,
        refresh: settingStore.getRefresh,
        search: settingStore.getSearch,
        notify: settingStore.getNotify,
        fullScreeen: settingStore.getFullScreeen,
        lock: settingStore.getLock,
        lockScreen: settingStore.getLockScreen,
        unlockScreenPwd: settingStore.getUnlockScreenPwd,
        pageAnimation: settingStore.getPageAnimation,
        settingPageStyle: settingStore.getSettingPageStyle,
        settingAnimation: settingStore.getSettingAnimation,
        settingWidth: settingStore.getSettingWidth
    });
    // 布局 value-实际选中值 全局或者单一
    const allLayoutOptions = [
        { value: '全局模块', label: 'globalModule' },
        { value: '单一模块', label: 'singleModule' }
    ];
    const layoutOptions = [
        { value: 'Y9Default', label: '左右' },
        { value: 'Y9Horizontal', label: '上下' },
        { value: 'Y9Default sidebar-separate', label: '浮动' }
    ];

    // 国际化
    const webLanguageOptions = [
        { value: '简体中文', label: 'zh' },
        { value: 'English', label: 'en' }
    ];

    // 字号
    const fontSizeOptions = [
        { value: '小', label: 'small' },
        { value: '中', label: 'default' },
        { value: '大', label: 'large' }
    ];

    // 主题
    const themeOptions = [
        { value: 'theme-default', label: '默认' },
        { value: 'theme-green', label: '绿' },
        { value: 'theme-blue', label: '蓝' }
    ];

    // 菜单移入方向控制
    const menuAnimationOptions = [
        { value: 'ltr', label: '左移入效果' },
        { value: 'rtl', label: '右移入效果' },
        { value: 'ttb', label: '下拉效果' },
        { value: 'btt', label: '上拉效果' }
    ];

    // 菜单背景
    let menuBgs = [
        new URL('../../assets/images/menu-bg1.png', import.meta.url).href,
        new URL('../../assets/images/menu-bg2.png', import.meta.url).href,
        new URL('../../assets/images/menu-bg3.png', import.meta.url).href,
        new URL('../../assets/images/menu-bg4.png', import.meta.url).href
    ];
    // 把已选中的背景排在第一位
    const currentMenuBg = computed(() => settingStore.getMenuBg);

    function sortImgs() {
        if (currentMenuBg.value) {
            menuBgs.unshift(currentMenuBg.value);
            menuBgs = [...new Set(menuBgs)];
        }
    }

    sortImgs();
    const menuBgOptions = [
        { key: 'bg1', src: menuBgs[0] },
        { key: 'bg2', src: menuBgs[1] },
        { key: 'bg3', src: menuBgs[2] },
        { key: 'bg4', src: menuBgs[3] }
    ];
    const menuBgChange = () => {
        settingStore.$patch({
            menuBg: form.menuBg
        });
    };

    // 菜单宽度
    const menuWidthOptions = [
        { value: '60%', label: '60%' },
        { value: '70%', label: '70%' },
        { value: '80%', label: '80%' },
        { value: '90%', label: '90%' },
        { value: '100%', label: '100%' }
    ];

    // 标签风格
    const labelStyleOptions = [
        { value: 'left', label: '左边' },
        { value: 'right', label: '右边' },
        { value: 'top', label: '上面' }
    ];
    // const labelStyleChange = () => {
    //     settingStore.$patch({
    //         labelStyle: form.labelStyle
    //     })
    // }

    // 分栏风格
    // const separateStyleOptions = [
    //     { value: '分栏风格1', label: '分栏风格1' },
    //     { value: '分栏风格2', label: '分栏风格2' },
    //     { value: '分栏风格3', label: '分栏风格3' }
    // ]

    // 设置风格
    const settingPageStyleOptions = [
        { value: 'Dcat', label: 'Dcat' },
        { value: 'Admin-plus', label: 'Admin-plus' }
    ];

    // 设置移入方向控制
    const settingAnimationOption = [
        { value: 'ltr', label: '左移入效果' },
        { value: 'rtl', label: '右移入效果' },
        { value: 'ttb', label: '下拉效果' },
        { value: 'btt', label: '上拉效果' }
    ];

    // setting宽度
    const settingWidthOptions = [
        { value: '15%', label: '15%' },
        { value: '20%', label: '20%' },
        { value: '25%', label: '25%' },
        { value: '30%', label: '30%' }
    ];

    // 修改设置
    const settingChange = (key) => {
        if (key == 'pcLayout') {
            let getAllPcLayoutList: any = settingStore.getAllPcLayoutList || [];
            let name = import.meta.env.VUE_APP_NAME;
            let obj: any = { name, value: form.pcLayout };
            let found = false;
            for (let i = 0; i < getAllPcLayoutList.length; i++) {
                let item = getAllPcLayoutList[i];
                if (item.name == name) {
                    found = true;
                    getAllPcLayoutList[i] = obj;
                    break;
                }
            }
            if (!found) {
                getAllPcLayoutList.push(obj);
            }
            settingStore.$patch({
                allLayoutList: getAllPcLayoutList
            });
        }
        settingStore.$patch({
            [key]: form[key]
        });
    };
    // 重置表单
    const resetFunc = () => {};
    // 提交表单
    const submit = () => {
        settingStore.$patch({
            allPcLayout: form.allPcLayout,
            webName: form.webName,
            fontSize: settingStore.getFontSize,
            logoSvgName: form.logoSvgName,
            webLanguage: form.webLanguage,
            themeName: form.themeName,
            menuAnimation: form.menuAnimation,
            menuStyle: form.menuStyle,
            menuWidth: form.menuWidth,
            showLabel: form.showLabel,
            showLabelIcon: form.showLabelIcon,
            labelStyle: form.labelStyle,
            // separateStyle: form.separateStyle,
            fixedHeader: form.fixedHeader,
            progress: form.progress,
            refresh: form.refresh,
            search: form.search,
            notify: form.notify,
            fullScreeen: form.fullScreeen,
            lock: form.lock,
            pageAnimation: form.pageAnimation,
            settingPageStyle: form.settingPageStyle,
            settingAnimation: form.settingAnimation,
            settingWidth: form.settingWidth
        });
        webSettingVisible.value = false;
    };

    function setMenuBackGroundImageFunc(e) {
        const targetElement = e.target;
        if (!Array.from(targetElement.classList).includes('selected')) {
            const url = targetElement.style.backgroundImage.split('"')[1];
            settingStore.$patch({
                menuBg: url ? url : ''
            });
        }
    }
</script>

<template>
    <el-drawer
        v-model="webSettingVisible"
        :direction="settingPageAnimationdirection"
        :size="size"
        :z-index="12"
        class="index-layout-setting-list"
        custom-class="indexlayout-settings"
        title="网站设置"
    >
        <el-form id="webSettingForm">
            <el-form-item
                :rules="[
                    {
                        required: true
                    }
                ]"
                label="布局"
            >
                <el-select
                    v-model="form.pcLayout"
                    :disabled="device === 'mobile' ? true : false"
                    placeholder="选择"
                    @change="settingChange('pcLayout')"
                >
                    <el-option
                        v-for="item in layoutOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                :rules="[
                    {
                        required: true
                    }
                ]"
                label="语言"
            >
                <el-radio-group v-model="form.webLanguage" @change="settingChange('webLanguage')">
                    <el-radio v-for="item in webLanguageOptions" :label="item.label" size="large"
                        >{{ item.value }}
                    </el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item
                :rules="[
                    {
                        required: true
                    }
                ]"
                label="字号"
            >
                <el-radio-group v-model="form.fontSize" @change="settingChange('fontSize')">
                    <el-radio v-for="item in fontSizeOptions" :key="item.label" :label="item.label" size="large"
                        >{{ item.value }}
                    </el-radio>
                </el-radio-group>
            </el-form-item>

            <el-form-item
                :rules="[
                    {
                        required: true
                    }
                ]"
                label="主题"
            >
                <el-select v-model="form.themeName" placeholder="选择" @change="settingChange('themeName')">
                    <el-option v-for="item in themeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>

            <el-form-item label="菜单背景">
                <template v-slot:default>
                    <el-scrollbar>
                        <div class="menu-bg-content" @click="setMenuBackGroundImageFunc">
                            <div
                                :class="{
                                    item: true,
                                    'item-off': currentMenuBg ? true : false,
                                    selected: currentMenuBg === ''
                                }"
                            >
                            </div>
                            <div
                                v-for="item in menuBgOptions"
                                :key="item.key"
                                :class="{ item: true, selected: currentMenuBg === item.src }"
                                :style="{ backgroundImage: 'url(' + item.src + ')', backgroundSize: 'cover' }"
                            ></div>
                        </div>
                    </el-scrollbar>
                </template>
            </el-form-item>
            <el-form-item label="菜单动画">
                <el-select v-model="form.menuAnimation" placeholder="选择" @change="settingChange('menuAnimation')">
                    <el-option
                        v-for="item in menuAnimationOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="菜单宽度">
                <el-select v-model="form.menuWidth" placeholder="选择" @change="settingChange('menuWidth')">
                    <el-option
                        v-for="item in menuWidthOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <!-- <el-form-item label="标签显示" >
                <el-switch v-model="form.showLabel" @change="settingChange('showLabel')" />
            </el-form-item> -->
            <!-- <el-form-item label="标签图标">
                <el-switch v-model="form.showLabelIcon" @change="settingChange('showLabelIcon')" />
            </el-form-item> -->
            <!-- <el-form-item label="标签位置">
                <el-select
                    v-model="form.labelStyle"
                    placeholder="选择"
                    @change="settingChange('labelStyle')"
                >
                    <el-option
                        v-for="item in labelStyleOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item> -->
            <!-- <el-form-item label="分栏风格">
                <el-select v-model="form.separateStyle" placeholder="选择">
                    <el-option
                        v-for="item in separateStyleOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>-->
            <!-- <el-form-item label="设置风格">
                <el-select
                    v-model="form.settingPageStyle"
                    placeholder="选择"
                    :disabled="device === 'mobile' ? true : false"
                    @change="settingChange('settingPageStyle')"
                >
                    <el-option
                        v-for="item in settingPageStyleOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item> -->
            <el-form-item label="设置动画">
                <el-select
                    v-model="form.settingAnimation"
                    placeholder="选择"
                    @change="settingChange('settingAnimation')"
                >
                    <el-option
                        v-for="item in settingAnimationOption"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="设置宽度">
                <el-select
                    v-model="form.settingWidth"
                    :disabled="device === 'mobile' ? true : false"
                    placeholder="选择"
                    @change="settingChange('settingWidth')"
                >
                    <el-option
                        v-for="item in settingWidthOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="头部固定">
                <el-switch v-model="form.fixedHeader" @change="settingChange('fixedHeader')" />
            </el-form-item>
            <el-form-item label="进度条">
                <el-switch v-model="form.progress" @change="settingChange('progress')" />
            </el-form-item>

            <el-form-item label="刷新">
                <el-switch v-model="form.refresh" @change="settingChange('refresh')" />
            </el-form-item>
            <!-- <el-form-item label="搜索">
                <el-switch v-model="form.search" @change="settingChange('search')" />
            </el-form-item>
            <el-form-item label="通知">
                <el-switch v-model="form.notify" @change="settingChange('notify')" />
            </el-form-item> -->
            <el-form-item label="全屏">
                <el-switch v-model="form.fullScreeen" @change="settingChange('fullScreeen')" />
            </el-form-item>
            <el-form-item label="页面动画">
                <el-switch v-model="form.pageAnimation" @change="settingChange('pageAnimation')" />
            </el-form-item>

            <el-form-item label="锁屏功能">
                <el-switch v-model="form.lock" @change="settingChange('lock')" />
            </el-form-item>
            <el-form-item label="锁屏">
                <el-switch v-model="form.lockScreen" @change="settingChange('lockScreen')" />
            </el-form-item>
            <el-form-item label="锁屏密码">
                <el-input
                    v-model="form.unlockScreenPwd"
                    :placeholder="form.unlockScreenPwd"
                    :style="{ width: '60%' }"
                    show-password
                    type="password"
                    @change="settingChange('unlockScreenPwd')"
                ></el-input>
            </el-form-item>
        </el-form>
        <el-divider />
        <el-row class="mb-4" style="justify-content: end">
            <el-button style="background-color: var(--el-color-primary); color: #fff" @click="submit()">保存</el-button>
            <el-button @click="resetFunc()">恢复默认</el-button>
        </el-row>
    </el-drawer>
</template>

<style lang="scss">
    #webSettingForm {
        .el-form-item--default .el-form-item__content {
            justify-content: end;
        }

        .el-form-item {
            align-items: center;
        }

        .menu-bg-content {
            display: flex;
            align-items: center;
            padding-top: 35px;

            .item {
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100px;
                height: 100px;
                margin: 10px;
                text-align: center;
                border-radius: 4px;
                background: var(--el-color-danger-light-9);
                color: var(--el-color-danger);

                &:hover {
                    cursor: pointer;
                }
            }

            .item-off {
                border: 1px solid var(--el-color-primary-light-5);

                &::before {
                    content: '无';
                    color: var(--el-color-primary-light-5);
                    font-weight: bold;
                }
            }

            .selected {
                box-shadow: 0 0 2px 2px #1890ff;

                &::before {
                    content: 'Current';
                    color: var(--el-color-primary-light-9);
                    opacity: 0.65;
                    font-weight: bold;
                    text-shadow: 5px 5px 2px var(--el-color-primary-light-1);
                }
            }
        }
    }
</style>
