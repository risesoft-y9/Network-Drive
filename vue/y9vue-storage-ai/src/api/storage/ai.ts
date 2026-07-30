/*
 * @Description: AI 智能服务 API
 */
import Request from '@/api/lib/request';
import qs from 'qs';

var aiRequest = new Request();

export default {
    /**
     * AI 智能搜索文件 - 自然语言查询
     * @param query 自然语言查询语句，如"帮我找上周上传的财务报表"
     * @param page 页码
     * @param size 每页条数
     */
    aiSearch(query: string, page: number = 1, size: number = 20) {
        return aiRequest.get(
            `/vue/ai/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
        );
    },

    /**
     * AI 对话 - 发送消息
     * @param sessionId 会话ID（新会话不传）
     * @param message 用户消息
     * @param context 上下文信息（如当前所在文件夹、文件列表等）
     */
    aiChat(sessionId: string | null, message: string, context?: any, mentionedFileIds?: string) {
        const params: any = { message };
        if (context) {
            // context 需要 JSON 序列化，避免 qs 深度展开为 context[source]=xxx
            params.context = JSON.stringify(context);
        }
        if (sessionId) {
            params.sessionId = sessionId;
        }
        if (mentionedFileIds) {
            // 文件ID直接作为字符串参数，避免 JSON 嵌套编码问题
            params.mentionedFileIds = mentionedFileIds;
        }
        return aiRequest.post('/vue/ai/chat', qs.stringify(params));
    },

    /**
     * AI 对话 - 流式响应
     * @param sessionId 会话ID
     * @param message 用户消息
     * @param context 上下文信息
     */
    aiChatStream(sessionId: string | null, message: string, context?: any) {
        const params: any = { message, stream: true };
        if (context) {
            params.context = JSON.stringify(context);
        }
        if (sessionId) {
            params.sessionId = sessionId;
        }
        return aiRequest.post('/vue/ai/chat/stream', qs.stringify(params));
    },

    /**
     * 获取 AI 对话历史
     * @param sessionId 会话ID
     */
    getChatHistory(sessionId: string) {
        return aiRequest.get(`/vue/ai/chat/history?sessionId=${sessionId}`);
    },

    /**
     * 获取会话列表
     */
    getSessionList() {
        return aiRequest.get('/vue/ai/chat/sessionList');
    },

    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    deleteSession(sessionId: string) {
        return aiRequest.delete(`/vue/ai/chat/session?sessionId=${sessionId}`);
    },

    /**
     * AI 文件分析 - 对文件内容进行智能分析
     * @param fileId 文件ID
     * @param analysisType 分析类型：summary(摘要)、keywords(关键词)、entity(实体识别)
     */
    analyzeFile(fileId: string, analysisType: string = 'summary') {
        return aiRequest.get(
            `/vue/ai/analyzeFile?fileId=${fileId}&analysisType=${analysisType}`
        );
    },

    /**
     * AI 智能推荐文件
     * @param limit 推荐数量
     */
    aiRecommend(limit: number = 10) {
        return aiRequest.get(`/vue/ai/recommend?limit=${limit}`);
    },

    /**
     * AI 智能标签 - 自动为文件打标签
     * @param fileId 文件ID
     */
    autoTag(fileId: string) {
        return aiRequest.post('/vue/ai/autoTag', qs.stringify({ fileId }));
    },

    /**
     * AI 文档问答 - 基于文件内容进行问答
     * @param fileId 文件ID
     * @param question 问题
     */
    docQuestion(fileId: string, question: string) {
        return aiRequest.post(
            '/vue/ai/docQuestion',
            qs.stringify({ fileId, question })
        );
    },

    /**
     * 搜索用户文件用于 @ 提及选择（支持文件名模糊匹配）
     * @param keyword 搜索关键词
     * @param limit 返回数量上限
     */
    searchFilesForMention(keyword: string, limit: number = 10) {
        return aiRequest.get(
            `/vue/ai/files/mention?keyword=${encodeURIComponent(keyword)}&limit=${limit}`
        );
    }
};
