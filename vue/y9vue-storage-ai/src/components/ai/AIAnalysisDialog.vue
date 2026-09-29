<template>
    <el-dialog
        v-model="visible"
        :title="'AI 文件分析 - ' + fileName"
        width="600px"
        :close-on-click-modal="false"
        destroy-on-close
    >
        <div class="ai-analysis-dialog">
            <!-- 分析类型选择 -->
            <div class="analysis-tabs">
                <el-radio-group v-model="analysisType" @change="handleAnalyze">
                    <el-radio-button label="summary">智能摘要</el-radio-button>
                    <el-radio-button label="keywords">关键词提取</el-radio-button>
                    <el-radio-button label="entity">实体识别</el-radio-button>
                </el-radio-group>
            </div>

            <!-- 加载中 -->
            <div v-if="loading" class="analysis-loading">
                <el-icon class="is-loading" :size="32"><Loading /></el-icon>
                <p>AI 正在分析文件内容...</p>
            </div>

            <!-- 分析结果 -->
            <div v-else-if="result" class="analysis-result">
                <div v-if="analysisType === 'summary'" class="result-section">
                    <h4>📄 内容摘要</h4>
                    <p>{{ result.summary || result }}</p>
                </div>

                <div v-else-if="analysisType === 'keywords'" class="result-section">
                    <h4>🏷️ 关键词</h4>
                    <div class="keyword-tags">
                        <el-tag
                            v-for="(kw, idx) in (result.keywords || result)"
                            :key="idx"
                            :type="['', 'success', 'warning', 'danger', 'info'][idx % 5]"
                            size="default"
                        >
                            {{ typeof kw === 'string' ? kw : kw.word }}
                        </el-tag>
                    </div>
                </div>

                <div v-else-if="analysisType === 'entity'" class="result-section">
                    <h4>🔍 实体识别</h4>
                    <div v-if="result.entities" class="entity-list">
                        <div
                            v-for="(entity, idx) in result.entities"
                            :key="idx"
                            class="entity-item"
                        >
                            <el-tag size="small" type="primary">{{ entity.type }}</el-tag>
                            <span>{{ entity.name }}</span>
                        </div>
                    </div>
                    <p v-else>{{ result }}</p>
                </div>
            </div>

            <!-- 错误 -->
            <div v-else-if="error" class="analysis-error">
                <el-result icon="error" title="分析失败" :sub-title="error">
                    <template #extra>
                        <el-button type="primary" @click="handleAnalyze">重试</el-button>
                    </template>
                </el-result>
            </div>

            <!-- 文档问答区域 -->
            <div v-if="result" class="doc-question-section">
                <el-divider />
                <h4>💬 针对此文档提问</h4>
                <div class="qa-list" v-if="questions.length > 0">
                    <div v-for="(qa, idx) in questions" :key="idx" class="qa-item">
                        <div class="qa-question">
                            <el-icon :size="14"><UserFilled /></el-icon>
                            <span>{{ qa.question }}</span>
                        </div>
                        <div class="qa-answer">
                            <el-icon :size="14"><MagicStick /></el-icon>
                            <span>{{ qa.answer }}</span>
                        </div>
                    </div>
                </div>
                <div class="question-input">
                    <el-input
                        v-model="questionText"
                        placeholder="基于文件内容提问..."
                        @keydown.enter="handleAskQuestion"
                    >
                        <template #append>
                            <el-button
                                :loading="asking"
                                :icon="Promotion"
                                @click="handleAskQuestion"
                            />
                        </template>
                    </el-input>
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script lang="ts" setup>
    import { ref, watch, inject } from 'vue';
    import { ElMessage } from 'element-plus';
    import { Loading, UserFilled, MagicStick, Promotion } from '@element-plus/icons-vue';
    import aiApi from '@/api/storage/ai';

    const props = defineProps<{
        modelValue: boolean;
        fileId: string;
        fileName: string;
    }>();

    const emit = defineEmits(['update:modelValue']);

    const fontSizeObj: any = inject('sizeObjInfo') || {};

    const visible = ref(props.modelValue);
    const analysisType = ref('summary');
    const loading = ref(false);
    const result = ref<any>(null);
    const error = ref('');
    const questionText = ref('');
    const asking = ref(false);
    const questions = ref<Array<{ question: string; answer: string }>>([]);

    watch(() => props.modelValue, (val) => {
        visible.value = val;
        if (val) {
            handleAnalyze();
        }
    });

    watch(visible, (val) => {
        emit('update:modelValue', val);
        if (!val) {
            resetState();
        }
    });

    function resetState() {
        result.value = null;
        error.value = '';
        questionText.value = '';
        questions.value = [];
    }

    async function handleAnalyze() {
        if (!props.fileId) return;
        loading.value = true;
        error.value = '';
        result.value = null;
        questions.value = [];

        try {
            const res = await aiApi.analyzeFile(props.fileId, analysisType.value);
            if (import.meta.env.DEV) {
                console.log('[AI Analyze] 后端响应:', res);
            }
            const isSuccess =
                res?.success === true ||
                res?.code === 200 ||
                res?.code === '200' ||
                res?.code === 0 ||
                (res !== undefined && res !== null && !res?.error);

            if (isSuccess) {
                result.value = res.data || res;
            } else {
                error.value = res?.msg || res?.message || '分析失败';
            }
        } catch (err: any) {
            console.error('[AI Analyze] 异常:', err);
            error.value = err?.response?.data?.msg || err?.message || 'AI 分析服务异常，请稍后再试';
        } finally {
            loading.value = false;
        }
    }

    async function handleAskQuestion() {
        const q = questionText.value.trim();
        if (!q || asking.value) return;

        asking.value = true;
        try {
            const res = await aiApi.docQuestion(props.fileId, q);
            if (import.meta.env.DEV) {
                console.log('[AI DocQuestion] 后端响应:', res);
            }
            const isSuccess =
                res?.success === true ||
                res?.code === 200 ||
                res?.code === '200' ||
                res?.code === 0 ||
                (res !== undefined && res !== null && !res?.error);

            if (isSuccess) {
                const data = res.data || res;
                questions.value.push({
                    question: q,
                    answer: data?.answer || data?.reply || data?.content || data || '无法回答此问题'
                });
                questionText.value = '';
            } else {
                ElMessage.warning(res?.msg || res?.message || '提问失败');
            }
        } catch (err: any) {
            console.error('[AI DocQuestion] 异常:', err);
            ElMessage.error(err?.response?.data?.msg || err?.message || '问答服务异常');
        } finally {
            asking.value = false;
        }
    }
</script>

<style lang="scss" scoped>
    .ai-analysis-dialog {
        .analysis-tabs {
            margin-bottom: 16px;
        }

        .analysis-loading {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 0;
            color: var(--el-text-color-secondary);

            .el-icon {
                color: var(--el-color-primary);
                margin-bottom: 12px;
            }
        }

        .analysis-result {
            .result-section {
                h4 {
                    margin: 0 0 12px 0;
                    font-size: 15px;
                    color: var(--el-text-color-primary);
                }

                p {
                    font-size: 14px;
                    line-height: 1.8;
                    color: var(--el-text-color-regular);
                    margin: 0;
                }

                .keyword-tags {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                }

                .entity-list {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;

                    .entity-item {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        padding: 4px 10px;
                        background: var(--el-fill-color-light);
                        border-radius: 4px;
                        font-size: 13px;
                    }
                }
            }
        }

        .doc-question-section {
            margin-top: 8px;

            h4 {
                margin: 12px 0;
                font-size: 14px;
                color: var(--el-text-color-primary);
            }

            .qa-list {
                margin-bottom: 12px;

                .qa-item {
                    margin-bottom: 12px;
                    padding: 10px;
                    background: var(--el-fill-color-lighter);
                    border-radius: 6px;

                    .qa-question {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        font-size: 13px;
                        font-weight: 500;
                        color: var(--el-text-color-primary);
                        margin-bottom: 6px;
                    }

                    .qa-answer {
                        display: flex;
                        align-items: flex-start;
                        gap: 6px;
                        font-size: 13px;
                        color: var(--el-text-color-regular);

                        .el-icon {
                            color: var(--el-color-primary);
                            flex-shrink: 0;
                            margin-top: 2px;
                        }
                    }
                }
            }

            .question-input {
                margin-top: 8px;
            }
        }
    }
</style>
