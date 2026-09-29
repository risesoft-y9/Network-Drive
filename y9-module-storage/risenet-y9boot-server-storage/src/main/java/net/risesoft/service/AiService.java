package net.risesoft.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.risesoft.entity.ChatMessage;
import net.risesoft.entity.FileNode;

/**
 * AI 智能服务接口
 */
public interface AiService {

    // ==================== AI 搜索 ====================

    /**
     * AI 智能搜索文件 — 自然语言查询
     *
     * @param query 自然语言查询语句
     * @param page  页码
     * @param size  每页条数
     * @return 搜索结果
     */
    Map<String, Object> aiSearch(String query, int page, int size);

    // ==================== AI 对话 ====================

    /**
     * AI 对话 - 非流式
     *
     * @param sessionId 会话ID（新会话传 null）
     * @param message   用户消息
     * @param context   上下文信息
     * @return 对话结果（含回复和会话ID）
     */
    Map<String, Object> aiChat(String sessionId, String message, Map<String, Object> context);

    /**
     * AI 对话 - 流式响应（SSE）
     *
     * @param sessionId 会话ID（新会话传 null）
     * @param message   用户消息
     * @param context   上下文信息
     * @param response  HttpServletResponse
     */
    void aiChatStream(String sessionId, String message, Map<String, Object> context, HttpServletResponse response);

    /**
     * 获取对话历史
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> getChatHistory(String sessionId);

    /**
     * 获取用户会话列表
     *
     * @return 会话列表
     */
    List<Map<String, Object>> getSessionList();

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    // ==================== 文件分析 ====================

    /**
     * AI 文件分析
     *
     * @param fileId       文件ID
     * @param analysisType 分析类型：summary、keywords、entity
     * @return 分析结果
     */
    Map<String, Object> analyzeFile(String fileId, String analysisType);

    // ==================== 智能推荐 ====================

    /**
     * AI 智能推荐文件
     *
     * @param limit 推荐数量
     * @return 推荐结果
     */
    List<Map<String, Object>> aiRecommend(int limit);

    // ==================== 智能标签 ====================

    /**
     * AI 自动为文件打标签
     *
     * @param fileId 文件ID
     * @return 标签结果
     */
    Map<String, Object> autoTag(String fileId);

    // ==================== 文档问答 ====================

    /**
     * AI 文档问答 - 基于文件内容进行问答
     *
     * @param fileId   文件ID
     * @param question 问题
     * @return 问答结果
     */
    Map<String, Object> docQuestion(String fileId, String question);

    // ==================== 上传索引 ====================

    /**
     * 文件上传后，将文件内容喂给大模型建立索引
     * <p>
     * 用于后续的智能搜索、内容对比、数据提取等功能。
     * 建议异步调用，避免阻塞上传流程。
     *
     * @param fileNode 已保存的文件节点
     */
    void indexUploadedFile(FileNode fileNode);

    // ==================== 文件提及搜索（@ 选择文件） ====================

    /**
     * 搜索用户文件用于 @ 提及选择（支持文件名模糊匹配）
     *
     * @param keyword 搜索关键词
     * @param limit   返回数量上限
     * @return 匹配的文件列表
     */
    List<Map<String, Object>> searchFilesForMention(String keyword, int limit);

    // ==================== 文件加密 ====================

    /**
     * 为指定文件设置链接密码
     *
     * @param fileId   文件ID
     * @param password 链接密码
     * @return 操作结果
     */
    Map<String, Object> setFileEncryption(String fileId, String password);

    /**
     * 获取用户可加密/已加密文件列表
     *
     * @param keyword 搜索关键词
     * @param limit   返回数量
     * @return 文件列表
     */
    List<Map<String, Object>> getEncryptableFiles(String keyword, int limit);
}
