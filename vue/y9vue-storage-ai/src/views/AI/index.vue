<template>
    <div class="ai-page" :style="pageStyle" @click="handlePageClick">
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
    import { useRouter } from 'vue-router';
    import { ChatDotRound, Search } from '@element-plus/icons-vue';
    import AIChat from '@/components/ai/AIChat.vue';

    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const router = useRouter();

    const showRight = ref(false);

    const pageStyle = computed(() => ({
        fontSize: fontSizeObj?.baseFontSize || '14px'
    }));

    function togglePanel() {
        showRight.value = !showRight.value;
    }

    /**
     * 事件委托：处理 AI 回复中的文件操作菜单点击
     * 菜单项由 ai.ts 在 AI 回复末尾自动追加，格式为：
     * <span class="operation-menu-item" data-route="/my/fileList/all" data-title="我的文件">...</span>
     */
    function handlePageClick(event: MouseEvent) {
        const target = (event.target as HTMLElement).closest('.operation-menu-item') as HTMLElement;
        if (!target) return;

        const route = target.dataset.route;
        if (route) {
            router.push(route);
        }
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

<!-- 全局样式：AI 回复中的文件操作菜单（v-html 渲染，需非 scoped 样式） -->
<style lang="scss">
    .menu-prompt {
        margin: 8px 0 0 0;
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
    }

    .operation-menus {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 12px;
        padding: 12px;
        background: #f5f7fa;
        border-radius: 8px;
        border: 1px solid #e4e7ed;
    }

    .operation-menu-item {
        display: inline-flex;
        align-items: center;
        padding: 6px 14px;
        background: #fff;
        border: 1px solid #dcdfe6;
        border-radius: 6px;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s ease;
        user-select: none;

        &:hover {
            border-color: var(--el-color-primary, #409eff);
            background: var(--el-color-primary-light-9, #ecf5ff);
            color: var(--el-color-primary, #409eff);
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
        }

        &:active {
            transform: scale(0.97);
        }
    }
</style>
