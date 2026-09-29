<template>
    <div class="link-invalid-page">
        <div class="invalid-card">
            <!-- 图标区域 -->
            <div class="icon-wrapper">
                <div class="icon-circle">
                    <i class="ri-link-unlink"></i>
                </div>
            </div>
            <!-- 文字区域 -->
            <h1 class="error-title">{{ $t('链接不存在') }}</h1>
            <p class="error-desc">{{ $t('抱歉，您访问的下载链接已失效或不存在') }}</p>
            <p class="error-hint">{{ $t('请联系分享者重新获取有效链接') }}</p>
            <!-- 操作按钮 -->
            <!-- <div class="action-buttons">
                <el-button type="primary" size="large" @click="$router.push('/')">
                    <i class="ri-home-4-line"></i>
                    {{ $t('返回首页') }}
                </el-button>
            </div> -->
        </div>
    </div>
</template>
<script lang="ts" setup>
</script>
<style lang="scss" scoped>
    .link-invalid-page {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100vh;
        min-height: 400px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 50%, #f0f2f5 100%);
    }

    .invalid-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 60px 80px 50px;
        background: #ffffff;
        border-radius: 16px;
        box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
        max-width: 480px;
        width: 90%;
        text-align: center;
        animation: fadeInUp 0.5s ease-out;

        .icon-wrapper {
            margin-bottom: 28px;

            .icon-circle {
                width: 90px;
                height: 90px;
                border-radius: 50%;
                background: linear-gradient(135deg, #fef0f0, #fde2e2);
                display: flex;
                justify-content: center;
                align-items: center;
                animation: pulse 2s ease-in-out infinite;

                i {
                    font-size: 42px;
                    color: #e6a23c;
                }
            }
        }

        .error-title {
            font-size: 26px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 16px;
            letter-spacing: 1px;
        }

        .error-desc {
            font-size: 15px;
            color: #909399;
            margin: 0 0 8px;
            line-height: 1.6;
        }

        .error-hint {
            font-size: 13px;
            color: #c0c4cc;
            margin: 0 0 36px;
        }

        .action-buttons {
            :deep(.el-button--large) {
                padding: 12px 32px;
                font-size: 15px;
                border-radius: 8px;
                font-weight: 500;

                i {
                    margin-right: 6px;
                }
            }
        }
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @keyframes pulse {
        0%,
        100% {
            box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.2);
        }
        50% {
            box-shadow: 0 0 0 12px rgba(230, 162, 60, 0);
        }
    }
</style>
