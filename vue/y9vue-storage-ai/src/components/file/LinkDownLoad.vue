<template>
    <div class="link-download-page">
        <div class="download-card">
            <!-- 顶部标题区域 -->
            <div class="card-header">
                <div class="header-icon">
                    <i class="ri-download-cloud-2-line"></i>
                </div>
                <h2 class="header-title">{{ $t('文件下载') }}</h2>
                <p class="header-desc">{{ $t('请输入下载码获取文件') }}</p>
            </div>

            <!-- 输入区域 -->
            <div class="card-body">
                <div class="input-group">
                    <el-input
                        v-model="filePassword"
                        :prefix-icon="Lock"
                        :placeholder="$t('请输入下载码')"
                        size="large"
                        clearable
                        maxlength="8"
                        show-password
                        show-word-limit
                        type="password"
                        class="password-input"
                        @keyup.enter="download"
                    />
                    <el-button
                        type="primary"
                        size="large"
                        class="download-btn"
                        @click="download"
                    >
                        <i class="ri-download-line"></i>
                        {{ $t('下载文件') }}
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
    import { onMounted, reactive, toRefs } from 'vue';
    import { Lock } from '@element-plus/icons-vue';
    import { useRoute, useRouter } from 'vue-router';
    import FileApi from '@/api/storage/file';
    import { useI18n } from 'vue-i18n';

    const { t } = useI18n();
    const router = useRouter();
    const currentrRute = useRoute();

    /**
     * 解码 tenantId + linkKey，与 FileLink.vue 的 encodeTenantKey 互为逆操作
     * 规则: URL-safe Base64 → atob → 按 '|' 分割
     */
    function decodeTenantKey(code: string): { tenantId: string; linkKey: string } | null {
        try {
            let encoded = code.replace(/-/g, '+').replace(/_/g, '/');
            while (encoded.length % 4 !== 0) encoded += '=';
            const raw = atob(encoded);
            const idx = raw.indexOf('|');
            if (idx === -1) {
                console.warn('未找到分隔符 |, raw:', raw.substring(0, 50));
                return null;
            }
            return {
                tenantId: raw.substring(0, idx),
                linkKey: raw.substring(idx + 1)
            };
        } catch (e) {
            console.error('解码失败:', e);
            return null;
        }
    }

    // 从路径参数中解码
    const codeParam = currentrRute.params.code as string;
    const decoded = codeParam ? decodeTenantKey(codeParam) : null;

    let data = reactive({
        filePassword: '',
        tenantId: decoded?.tenantId || ''
    });

    let { filePassword, tenantId } = toRefs(data);

    onMounted(async () => {
        console.log('路径参数 code:', codeParam);
        // 验证链接是否存在
        if (codeParam) {
            try {
                const res = await FileApi.checkLink('', codeParam);
                if (!res.data.success) {
                    router.replace({ path: '/linkInvalid' });
                }
            } catch {
                router.replace({ path: '/linkInvalid' });
            }
        } else {
            router.replace({ path: '/linkInvalid' });
        }
    });

    async function download() {
        if (filePassword.value) {
            let res = await FileApi.checkLink(filePassword.value,codeParam);
            ElMessage({ type: res.data.success ? 'success' : 'error', message: res.data.msg, offset: 65 });
            if (res.data.success) {
                let fileId = res.data.fileId;
                window.open(import.meta.env.VUE_APP_CONTEXT + 'link/df/' + fileId + '/' + tenantId.value, '_blank');
            }
        } else {
            ElMessage({ type: 'error', message: t('请输入文件密码'), offset: 65 });
        }
    }
</script>
<style lang="scss" scoped>
    .link-download-page {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100vh;
        min-height: 500px;
        background: linear-gradient(135deg, #eef2f7 0%, #e2e8f0 30%, #edf1f7 60%, #f0f4fa 100%);
    }

    .download-card {
        width: 440px;
        background: #ffffff;
        border-radius: 16px;
        box-shadow:
            0 12px 48px rgba(0, 0, 0, 0.08),
            0 4px 16px rgba(0, 0, 0, 0.04);
        overflow: hidden;
        animation: cardFadeIn 0.5s cubic-bezier(0.22, 0.61, 0.36, 1);

        .card-header {
            padding: 40px 40px 0;
            text-align: center;
            background: #ffffff;

            .header-icon {
                display: inline-flex;
                justify-content: center;
                align-items: center;
                width: 72px;
                height: 72px;
                border-radius: 50%;
                background: linear-gradient(135deg, #e8f4fd, #d0e8fb);
                margin-bottom: 20px;

                i {
                    font-size: 34px;
                    color: #586cb1;
                }
            }

            .header-title {
                font-size: 22px;
                font-weight: 600;
                color: #303133;
                margin: 0 0 8px;
            }

            .header-desc {
                font-size: 14px;
                color: #909399;
                margin: 0 0 4px;
            }
        }

        .card-body {
            padding: 28px 40px 44px;

            .input-group {
                display: flex;
                flex-direction: column;
                gap: 16px;

                .password-input {
                    :deep(.el-input__wrapper) {
                        border-radius: 10px;
                        padding: 4px 12px;
                        box-shadow: 0 0 0 1px #e4e7ed inset;
                        transition: box-shadow 0.25s, border-color 0.25s;

                        &:hover {
                            box-shadow: 0 0 0 1px #c0c4cc inset;
                        }

                        &.is-focus {
                            box-shadow: 0 0 0 1px var(--el-color-primary) inset, 0 0 0 3px rgba(88, 108, 177, 0.12);
                        }
                    }
                }

                .download-btn {
                    border-radius: 10px;
                    font-size: 15px;
                    font-weight: 500;
                    height: 44px;
                    letter-spacing: 1px;
                    transition: all 0.25s;

                    i {
                        margin-right: 6px;
                        font-size: 16px;
                    }

                    &:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 6px 20px rgba(88, 108, 177, 0.35);
                    }

                    &:active {
                        transform: translateY(0);
                    }
                }
            }
        }
    }

    @keyframes cardFadeIn {
        from {
            opacity: 0;
            transform: translateY(24px) scale(0.97);
        }
        to {
            opacity: 1;
            transform: translateY(0) scale(1);
        }
    }
</style>
