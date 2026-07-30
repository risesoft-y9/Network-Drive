<template>
    <div class="ai-page" :style="pageStyle">
        <!-- 左侧面板 - AI 对话 -->
        <div class="ai-left-panel" :class="{ collapsed: showRight }">
            <AIChat @close="togglePanel" />
        </div>

        <!-- 切换按钮（移动端/窄屏） -->
        <div class="panel-toggle" @click="togglePanel">
            <el-tooltip :content="showRight ? '显示对话' : '显示搜索结果'" placement="top">
                <el-button circle :icon="showRight ? ChatDotRound : Search" />
            </el-tooltip>
        </div>
    </div>
</template>

<script lang="ts" setup>
    import { ref, computed, inject } from 'vue';
    import { ChatDotRound, Search } from '@element-plus/icons-vue';
    import AIChat from '@/components/ai/AIChat.vue';

    const fontSizeObj: any = inject('sizeObjInfo') || {};

    const showRight = ref(false);

    const pageStyle = computed(() => ({
        fontSize: fontSizeObj?.baseFontSize || '14px'
    }));

    function togglePanel() {
        showRight.value = !showRight.value;
    }

    function handleFileClick() {
        // 点击文件时的处理
        showRight.value = true;
    }
</script>

<style lang="scss" scoped>
    .ai-page {
        display: flex;
        height: 100%;
        gap: 0;
        position: relative;

        .ai-left-panel {
            flex: 1;
            min-width: 400px;
            max-width: 100%;
            min-height: 0;
            transition: all 0.3s ease;
        }

        .panel-toggle {
            position: fixed;
            bottom: 24px;
            right: 24px;
            z-index: 100;

            :deep(.el-button) {
                box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
            }
        }
    }

    @media (max-width: 768px) {
        .ai-page {
            .ai-left-panel {
                min-width: 100%;
            }
        }
    }
</style>
