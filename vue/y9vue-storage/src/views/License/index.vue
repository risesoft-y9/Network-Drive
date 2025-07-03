<!--
 * @Author: error: git config user.name && git config user.email & please set dead value or install git
 * @Date: 2022-08-02 10:51:50
 * @LastEditors: error: git config user.name && git config user.email & please set dead value or install git
 * @LastEditTime: 2022-08-02 11:12:06
 * @Description: 无权限
-->

<template>
    <div class="login">
        <div class="form">
            <h1 class="title">{{ $t('License过期') }} </h1>
            <p class="msg">{{ $t('License过期') }}</p>
        </div>
    </div>
</template>
<script setup>
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n'
    const { t } = useI18n();
    const infoKey = ref("License过期");
    onMounted(() => {
        window.location.href.split("?")[1].split("&").forEach((item) => {
            if (item.startsWith("info=")) {
                infoKey.value = item.split("=")[1];
            }
        });
        /**
         * 当过期的时候，修复开发环境下，没有及时清除缓存的bug
         * 但是生产环境，如果 domainURL 和单点登录服务的domainURL不一致时，可能同样会遇到这个问题
         */ 
        localStorage.removeItem("lastCheckDate");
    });
    

</script>
<style scoped>
    .login {
        display: flex;
        width: 100%;
        height: 100vh;
        min-height: 300px;
        overflow: auto;
        /* background-image: url("../../assets/images/bg.jpg"); */
        background-color: #f5f7f9;
        background-repeat: no-repeat;
        background-position: center center;
        background-attachment: fixed;
        background-size: cover;
        align-items: center;
    }

    .form {
        flex: none;
        width: 420px;
        padding: 60px;
        margin: 0 auto;
        border-radius: 4px;
        background-color: rgba(255, 255, 255, 0);
        text-align: center;
        color: rgba(0, 0, 0, 0.5);
    }

    .title {
        font-weight: 300;
    }

    .msg {
        padding: 8px;
    }
</style>
