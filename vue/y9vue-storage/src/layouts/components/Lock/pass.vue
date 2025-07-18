<script lang="ts" setup>
    import { useSettingStore } from '@/store/modules/settingStore';
    import { computed, ref, watch } from 'vue-demi';
    import y9_storage from '@/utils/storage';

    const settingStore = useSettingStore();
    const unlockScreenPwd = computed(() => settingStore.getUnlockScreenPwd);
    const lockStatus = computed(() => settingStore.getLockScreen);
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    // 绑定输入数据
    const inputPwd = ref('');
    // 密码错误提示
    const showError = ref(false);
    // 头像
    let avatar = ref('');

    // 密码验证
    const checkPwdFunc = () => {
        if (inputPwd.value === unlockScreenPwd.value) {
            settingStore.$patch({
                lockScreen: false
            });
        } else {
            showError.value = true;
        }
    };

    if (lockStatus.value) {
        getInfoAvatar();
    }
    watch(lockStatus, () => {
        getInfoAvatar();
    });

    function getInfoAvatar() {
        // 个人信息 —— 头像
        let userInfo = y9_storage.getObjectItem('ssoUserInfo');
        if (userInfo.avator) {
            checkImgExists(userInfo.avator)
                .then(() => {
                    avatar.value = userInfo.avator;
                })
                .catch(() => {
                    avatar.value = 'https://i.gtimg.cn/club/item/face/img/2/16022_100.gif';
                });
        } else {
            avatar.value = 'https://i.gtimg.cn/club/item/face/img/2/16022_100.gif';
        }
    }

    function checkImgExists(imgUrl) {
        return new Promise(function (resolve, reject) {
            var imgObj = new Image();
            imgObj.src = imgUrl;
            imgObj.onload = function (res) {
                resolve(res);
            };
            imgObj.onerror = function (err) {
                reject(err);
            };
        });
    }

    //
    const showInput = ref('hidden');
    // 更换网络背景图片
    const imageUrl = ref('');
    const changeImageUrlFunc = () => {
        settingStore.$patch({
            lockScreenImage: imageUrl.value
        });
        showInput.value = 'hidden';
    };
</script>

<template>
    <div class="lock-pane"></div>
    <div class="content">
        <img :src="avatar" alt="头像" />
        <!-- <i class="ri-lock-2-fill"></i> -->
        <span> {{ $t('屏幕已锁定') }}</span>
        <div class="form">
            <span :class="{ showErrorText: showError }">{{ $t('密码错误') }}</span>
            <el-input
                v-model="inputPwd"
                placeholder="Please input password"
                show-password
                type="password"
                @change="checkPwdFunc"
                @focus="showError = false"
            />
            <el-button
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                type="primary"
                @click="checkPwdFunc"
                @mousemove="toopTipsVisible = false"
            >
                <i class="ri-lock-unlock-line"></i>{{ $t('解锁') }}
            </el-button>
        </div>
        <!-- <a @click="showInput='visible'">{{ $t('更换网络图片作为锁屏背景') }}...</a> -->
        <!-- <el-input 
            :style="{'visibility': showInput}"
            v-model="imageUrl" 
            @change="changeImageUrlFunc"
            :placeholder="`jpg png svg ... Ctrl+C & V ${$t('粘贴')}URL`"
        ></el-input> -->
    </div>
</template>

<style lang="scss" scoped>
    .lock-pane {
        width: 400px;
        height: 400px;
        text-align: center;
        padding: 50px;
        border-radius: 15px;
        color: var(--el-text-color-primary);
        background-color: var(--el-bg-color);
        opacity: 0.6;
        box-sizing: border-box;
    }

    .content {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        position: absolute;

        & > img {
            width: 180px;
            height: 180px;
            border-radius: 50%;
            overflow: hidden;
            // margin-bottom: 25px;
        }

        & > span {
            margin: 25px 0;
        }

        & > div.form {
            display: flex;
            justify-content: center;
            align-items: center;

            & > button {
                line-height: 25px;
                border: none;
                border-top-left-radius: 0;
                border-bottom-left-radius: 0;
                position: relative;
                left: -2px;

                & > i {
                    position: relative;
                    top: 2px;
                    margin-right: 3px;
                }
            }

            & > span {
                display: none;
                font-size: 14px;
                letter-spacing: 1px;
                color: #f40;
                position: absolute;
                z-index: 1;

                &.showErrorText {
                    display: block;
                }
            }
        }

        & > a {
            margin-top: 30px;
            line-height: 30px;
            font-size: 12px;
            letter-spacing: 1px;
            color: var(--el-color-primary-light-3);
            text-decoration: underline;
            cursor: pointer;
        }
    }
</style>
