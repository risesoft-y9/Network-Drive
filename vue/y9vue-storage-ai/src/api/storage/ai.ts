/*
 * @Description: AI 智能服务 API
 */
import Request from '@/api/lib/request';
import qs from 'qs';
import { buildMenuContext, buildMenuHTML, detectOperationKeys, getMenuPrompt } from '@/composables/useFileOperations';

var aiRequest = new Request();

/**
 * 检测消息是否涉及文件操作，并返回匹配的操作 key 列表
 * 返回空数组表示不涉及文件操作
 */
function getOperationKeys(message: string): string[] {
    return detectOperationKeys(message);
}

/** 将操作菜单 HTML 追加到 AI 回复文本中（自动去重，避免重复追加） */
function appendMenuToReply(res: any, menuHTML: string): void {
    if (!res || !menuHTML) return;

    // 工具函数：若目标字符串中已包含菜单标记，则不追加
    const appendIfMissing = (target: string): boolean => {
        if (typeof target !== 'string') return false;
        if (target.includes('class="operation-menus"')) return false;
        return true;
    };

    // 匹配 extractReply 的查找顺序，优先追加到第一个匹配的字段
    if (res.data?.reply && appendIfMissing(res.data.reply)) {
        res.data.reply += menuHTML;
    } else if (res.data?.content && appendIfMissing(res.data.content)) {
        res.data.content += menuHTML;
    } else if (res.data?.answer && appendIfMissing(res.data.answer)) {
        res.data.answer += menuHTML;
    } else if (typeof res.data === 'string' && appendIfMissing(res.data)) {
        res.data += menuHTML;
    } else if (res.reply && appendIfMissing(res.reply)) {
        res.reply += menuHTML;
    } else if (res.content && appendIfMissing(res.content)) {
        res.content += menuHTML;
    } else if (res.answer && appendIfMissing(res.answer)) {
        res.answer += menuHTML;
    } else if (typeof res === 'string' && appendIfMissing(res)) {
        // res 本身是字符串（不常见但也处理）
        (res as any)._menuHTML = menuHTML;
    } else if (res.success && res.msg && appendIfMissing(res.msg)) {
        res.msg += menuHTML;
    } else if (res.data && typeof res.data === 'object') {
        if (res.data.reply && appendIfMissing(res.data.reply)) res.data.reply += menuHTML;
        else if (res.data.content && appendIfMissing(res.data.content)) res.data.content += menuHTML;
        else if (res.data.answer && appendIfMissing(res.data.answer)) res.data.answer += menuHTML;
        else res.data._menuHTML = menuHTML;
    }
}

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
    async aiChat(sessionId: string | null, message: string, context?: any, mentionedFileIds?: string) {
        const params: any = { message };

        // 注入文件操作菜单上下文，让 AI 知道有哪些可用菜单
        const mergedContext = {
            fileOperationMenus: JSON.parse(buildMenuContext()),
            ...(context || {})
        };
        params.context = JSON.stringify(mergedContext);

        if (sessionId) {
            params.sessionId = sessionId;
        }
        if (mentionedFileIds) {
            params.mentionedFileIds = mentionedFileIds;
        }

        const res = await aiRequest.post('/vue/ai/chat', qs.stringify(params));

        // 按用户输入的操作意图过滤菜单，只显示有对应操作权限的菜单
        const opKeys = getOperationKeys(message);
        if (opKeys.length > 0) {
            const promptText = getMenuPrompt(opKeys);
            const menuHTML = buildMenuHTML(opKeys, promptText);
            appendMenuToReply(res, menuHTML);
        }

        // 将操作意图标记附加到响应上，供 AIChat 组件检测是否自动弹出操作弹窗
        (res as any)._operationKeys = opKeys;

        return res;
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
