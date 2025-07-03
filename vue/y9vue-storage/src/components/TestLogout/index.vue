<!-- ClickPopup.vue 子组件 -->
<template>
    <Teleport to="body">
        <div class="popup-overlay">
            <div class="popup-content">
                <button @click="handleClick">点击我</button>
            </div>
        </div>
    </Teleport>
</template>

<script setup>
    import { $y9_SSO } from '@/main';
    const handleClick = () => {
        console.log('点击了子组件的按钮');
        let obj = {};
        // $y9_SSO.ssoLogout(obj);
        // $y9_SSO.clearCurrentSessionStorage();
    };
</script>

<style scoped>
    .popup-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .popup-content {
        background: white;
        padding: 2rem;
        border-radius: 8px;
    }

    button {
        padding: 8px 16px;
        font-size: 16px;
        cursor: pointer;
    }
</style>
