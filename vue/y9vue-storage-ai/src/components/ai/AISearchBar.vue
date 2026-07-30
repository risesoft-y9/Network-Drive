<template>
    <div class="ai-search-bar">
        <div class="search-input-wrapper">
            <el-icon class="ai-icon" :size="18"><MagicStick /></el-icon>
            <el-input
                v-model="queryText"
                placeholder="AI 智能搜索：用自然语言描述你要找的文件..."
                clearable
                @keydown.enter="handleSearch"
                @clear="handleClear"
            >
                <template #append>
                    <el-button
                        :loading="searching"
                        :icon="Search"
                        @click="handleSearch"
                    >
                        搜索
                    </el-button>
                </template>
            </el-input>
        </div>

        <!-- 搜索结果展示 -->
        <div v-if="showResult" class="search-result-panel">
            <div class="result-header">
                <div class="result-summary">
                    <el-icon :size="16"><MagicStick /></el-icon>
                    <span>AI 解读：{{ aiSummary }}</span>
                </div>
                <el-button text size="small" @click="showResult = false">收起</el-button>
            </div>

            <div v-if="results.length > 0" class="result-list">
                <div
                    v-for="item in results"
                    :key="item.id"
                    class="result-item"
                    @click="$emit('fileClick', item)"
                >
                    <el-icon :size="20"><Document /></el-icon>
                    <div class="result-info">
                        <span class="result-name">{{ item.name }}</span>
                        <span class="result-desc">{{ item.description || item.path }}</span>
                    </div>
                    <span class="result-relevance">{{ item.relevanceScore || '' }}</span>
                </div>
            </div>

            <div v-else-if="!searching" class="result-empty">
                <span>未找到匹配的文件，请尝试换个说法搜索</span>
            </div>

            <!-- 追问 -->
            <div v-if="results.length > 0" class="result-suggestions">
                <span class="suggest-label">你可能还想问：</span>
                <el-button
                    v-for="s in suggestions"
                    :key="s"
                    link
                    size="small"
                    type="primary"
                    @click="queryText = s; handleSearch()"
                >
                    {{ s }}
                </el-button>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
    import { ref, inject } from 'vue';
    import { ElMessage } from 'element-plus';
    import { MagicStick, Search, Document } from '@element-plus/icons-vue';
    import aiApi from '@/api/storage/ai';

    const emit = defineEmits(['fileClick', 'searchResult']);

    const fontSizeObj: any = inject('sizeObjInfo') || {};

    const queryText = ref('');
    const searching = ref(false);
    const showResult = ref(false);
    const results = ref<any[]>([]);
    const aiSummary = ref('');
    const suggestions = ref<string[]>([]);

    async function handleSearch() {
        const query = queryText.value.trim();
        if (!query) return;

        searching.value = true;
        showResult.value = true;

        try {
            const res = await aiApi.aiSearch(query);

            // 开发环境打印响应，方便调试对接
            if (import.meta.env.DEV) {
                console.log('[AI Search] 后端响应:', res);
            }

            // 兼容多种响应格式
            const isSuccess =
                res?.success === true ||
                res?.code === 200 ||
                res?.code === '200' ||
                res?.code === 0 ||
                (res !== undefined && res !== null && !res?.error);

            if (isSuccess) {
                // 兼容不同字段名: list/files/rows/records
                const data = res.data || res;
                results.value = data?.list || data?.files || data?.rows || data?.records || [];
                aiSummary.value = data?.summary || data?.msg || `共找到 ${results.value.length} 个相关文件`;
                suggestions.value = data?.suggestions || [];
            } else {
                const errMsg = res?.msg || res?.message || '搜索失败，请重试';
                console.warn('[AI Search] 请求失败:', errMsg, res);
                ElMessage.warning(errMsg);
                results.value = [];
                aiSummary.value = errMsg;
            }
        } catch (error: any) {
            console.error('[AI Search] 异常:', error);
            let errMsg = 'AI 搜索服务异常';
            if (error?.response?.data) {
                const d = error.response.data;
                errMsg = d.msg || d.message || errMsg;
            } else if (error?.message) {
                errMsg = error.message;
            }
            ElMessage.error(errMsg);
            results.value = [];
            aiSummary.value = errMsg;
        } finally {
            searching.value = false;
        }
    }

    function handleClear() {
        showResult.value = false;
        results.value = [];
        aiSummary.value = '';
        suggestions.value = [];
    }
</script>

<style lang="scss" scoped>
    .ai-search-bar {
        .search-input-wrapper {
            display: flex;
            align-items: center;
            gap: 8px;

            .ai-icon {
                color: var(--el-color-primary);
                flex-shrink: 0;
            }

            :deep(.el-input-group__append) {
                .el-button {
                    background: var(--el-color-primary);
                    color: #fff;
                    border: none;

                    &:hover {
                        background: var(--el-color-primary-light-3);
                    }
                }
            }
        }

        .search-result-panel {
            margin-top: 12px;
            background: var(--el-bg-color);
            border: 1px solid var(--el-border-color-light);
            border-radius: 8px;
            padding: 16px;
            max-height: 400px;
            overflow-y: auto;

            .result-header {
                display: flex;
                align-items: flex-start;
                justify-content: space-between;
                margin-bottom: 12px;
                padding-bottom: 12px;
                border-bottom: 1px solid var(--el-border-color-lighter);

                .result-summary {
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    font-size: 14px;
                    color: var(--el-text-color-primary);
                    flex: 1;

                    .el-icon {
                        color: var(--el-color-primary);
                        flex-shrink: 0;
                    }
                }
            }

            .result-list {
                display: flex;
                flex-direction: column;
                gap: 8px;

                .result-item {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 10px 12px;
                    border-radius: 6px;
                    cursor: pointer;
                    transition: background 0.2s;

                    &:hover {
                        background: var(--el-fill-color-light);
                    }

                    .el-icon {
                        flex-shrink: 0;
                        color: var(--el-color-primary);
                    }

                    .result-info {
                        flex: 1;
                        min-width: 0;
                        display: flex;
                        flex-direction: column;

                        .result-name {
                            font-size: 14px;
                            color: var(--el-text-color-primary);
                            font-weight: 500;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }

                        .result-desc {
                            font-size: 12px;
                            color: var(--el-text-color-secondary);
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }
                    }

                    .result-relevance {
                        font-size: 12px;
                        color: var(--el-color-primary);
                        flex-shrink: 0;
                    }
                }
            }

            .result-empty {
                text-align: center;
                padding: 30px 0;
                color: var(--el-text-color-secondary);
                font-size: 14px;
            }

            .result-suggestions {
                margin-top: 12px;
                padding-top: 12px;
                border-top: 1px solid var(--el-border-color-lighter);
                display: flex;
                flex-wrap: wrap;
                align-items: center;
                gap: 8px;

                .suggest-label {
                    font-size: 12px;
                    color: var(--el-text-color-secondary);
                }
            }
        }
    }
</style>
