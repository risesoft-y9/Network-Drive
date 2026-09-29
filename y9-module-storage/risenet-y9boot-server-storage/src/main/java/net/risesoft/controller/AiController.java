package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.AiService;
import net.risesoft.service.FileNodeService;

/**
 * AI 智能服务 Controller
 * 
 * @author yihong
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/vue/ai")
public class AiController {

    private final AiService aiService;

    private final FileNodeService fileNodeService;

    /**
     * AI 智能搜索文件 — 自然语言查询
     */
    @RiseLog(operationName = "AI智能搜索")
    @GetMapping("/search")
    public Y9Result<Map<String, Object>> aiSearch(@RequestParam String query,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        if (StringUtils.isBlank(query)) {
            return Y9Result.failure("请输入搜索内容");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        }
        Map<String, Object> result = aiService.aiSearch(query, page, size);
        return Y9Result.success(result);
    }

    /**
     * AI 对话 - 非流式
     */
    @RiseLog(operationName = "AI对话")
    @PostMapping("/chat")
    public Y9Result<Map<String, Object>> aiChat(@RequestParam(required = false) String sessionId,
        @RequestParam String message, @RequestParam(required = false) String context,
        @RequestParam(required = false) String mentionedFileIds) {
        if (StringUtils.isBlank(message)) {
            return Y9Result.failure("请输入消息内容");
        }
        // HTML 反转义，解决框架层对 form 参数的转义问题（" → &ldquo; 等）
        String cleanMessage = org.springframework.web.util.HtmlUtils.htmlUnescape(message);
        Map<String, Object> contextMap = parseContextJson(context, mentionedFileIds);
        LOGGER.debug("[AI Chat] contextMap={}, mentionedFileIds={}", contextMap, mentionedFileIds);
        Map<String, Object> result = aiService.aiChat(sessionId, cleanMessage, contextMap);
        return Y9Result.success(result);
    }

    /**
     * AI 对话 - 流式响应（SSE）
     */
    @RiseLog(operationName = "AI流式对话")
    @PostMapping("/chat/stream")
    public void aiChatStream(@RequestParam(required = false) String sessionId, @RequestParam String message,
        @RequestParam(defaultValue = "true") boolean stream, @RequestParam(required = false) String context,
        HttpServletResponse response) {
        if (StringUtils.isBlank(message)) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "消息内容不能为空");
            } catch (Exception ignored) {
            }
            return;
        }
        // HTML 反转义，解决框架层对 form 参数的转义问题
        String cleanMessage = org.springframework.web.util.HtmlUtils.htmlUnescape(message);
        Map<String, Object> contextMap = parseContextJson(context, null);
        aiService.aiChatStream(sessionId, cleanMessage, contextMap, response);
    }

    /**
     * 解析 context JSON 字符串为 Map，同时合并 mentionedFileIds 作为备份通道
     * <p>
     * 某些环境下 form 参数值会被 HTML 转义（如 " → &amp;quot;），需要先反转义再解析 JSON。
     */
    private Map<String, Object> parseContextJson(String context, String mentionedFileIds) {
        Map<String, Object> result = null;
        if (StringUtils.isNotBlank(context)) {
            try {
                // 先做 HTML 反转义，解决框架层对 form 参数的转义问题（" → &quot;）
                String cleanContext = org.springframework.web.util.HtmlUtils.htmlUnescape(context);
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(cleanContext, new TypeReference<Map<String, Object>>() {});
                LOGGER.debug("[AI Chat] context JSON 解析成功, keys={}", result.keySet());
            } catch (Exception e) {
                LOGGER.debug("[AI Chat] context JSON 解析失败（已尝试 HTML 反转义），将使用 mentionedFileIds 备份通道。context前100字符: {}",
                    StringUtils.abbreviate(context, 100));
            }
        }
        // 如果 context 解析失败或缺少 mentionedFiles，使用 mentionedFileIds 作为备份
        if (StringUtils.isNotBlank(mentionedFileIds)) {
            if (result == null) {
                result = new java.util.HashMap<>();
            }
            if (!result.containsKey("mentionedFiles")) {
                // 从逗号分隔的 ID 列表构建 mentionedFiles
                String[] ids = mentionedFileIds.split(",");
                java.util.List<Map<String, Object>> fileList = new java.util.ArrayList<>();
                for (String id : ids) {
                    String trimmedId = id.trim();
                    if (StringUtils.isNotBlank(trimmedId)) {
                        Map<String, Object> fileInfo = new java.util.HashMap<>();
                        fileInfo.put("id", trimmedId);
                        fileList.add(fileInfo);
                    }
                }
                if (!fileList.isEmpty()) {
                    result.put("mentionedFiles", fileList);
                    LOGGER.info("[AI Chat] 通过 mentionedFileIds 备份通道成功构建 mentionedFiles: {} 个文件 (ID: {})",
                        fileList.size(), mentionedFileIds);
                }
            }
        }
        return result;
    }

    /**
     * 获取 AI 对话历史
     */
    @RiseLog(operationName = "获取对话历史")
    @GetMapping("/chat/history")
    public Y9Result<Object> getChatHistory(@RequestParam String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return Y9Result.failure("会话ID不能为空");
        }
        return Y9Result.success(aiService.getChatHistory(sessionId));
    }

    /**
     * 获取会话列表
     */
    @RiseLog(operationName = "获取会话列表")
    @GetMapping("/chat/sessionList")
    public Y9Result<Object> getSessionList() {
        return Y9Result.success(aiService.getSessionList());
    }

    /**
     * 删除会话
     */
    @RiseLog(operationName = "删除会话")
    @DeleteMapping("/chat/session")
    public Y9Result<Object> deleteSession(@RequestParam String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return Y9Result.failure("会话ID不能为空");
        }
        aiService.deleteSession(sessionId);
        return Y9Result.success("删除成功");
    }

    /**
     * AI 文件分析
     */
    @RiseLog(operationName = "AI文件分析")
    @GetMapping("/analyzeFile")
    public Y9Result<Map<String, Object>> analyzeFile(@RequestParam String fileId,
        @RequestParam(defaultValue = "summary") String analysisType) {
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("文件ID不能为空");
        }
        Map<String, Object> result = aiService.analyzeFile(fileId, analysisType);
        return Y9Result.success(result);
    }

    /**
     * AI 智能推荐文件
     */
    @RiseLog(operationName = "AI智能推荐")
    @GetMapping("/recommend")
    public Y9Result<Object> aiRecommend(@RequestParam(defaultValue = "10") int limit) {
        if (limit < 1) {
            limit = 10;
        }
        return Y9Result.success(aiService.aiRecommend(limit));
    }

    /**
     * AI 智能标签 - 自动为文件打标签
     */
    @RiseLog(operationName = "AI智能标签")
    @PostMapping("/autoTag")
    public Y9Result<Map<String, Object>> autoTag(@RequestParam String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("文件ID不能为空");
        }
        Map<String, Object> result = aiService.autoTag(fileId);
        return Y9Result.success(result);
    }

    /**
     * AI 文档问答 - 基于文件内容进行问答
     */
    @RiseLog(operationName = "AI文档问答")
    @PostMapping("/docQuestion")
    public Y9Result<Map<String, Object>> docQuestion(@RequestParam String fileId, @RequestParam String question) {
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("文件ID不能为空");
        }
        if (StringUtils.isBlank(question)) {
            return Y9Result.failure("请输入您的问题");
        }
        Map<String, Object> result = aiService.docQuestion(fileId, question);
        return Y9Result.success(result);
    }

    // ==================== 新增：文件提及 & 加密 ====================

    /**
     * @ 文件提及搜索 — 输入 @ 后自动补全网盘文件
     */
    @RiseLog(operationName = "@文件提及搜索")
    @GetMapping("/files/mention")
    public Y9Result<List<Map<String, Object>>> searchFilesForMention(@RequestParam String keyword,
        @RequestParam(defaultValue = "10") int limit) {
        if (StringUtils.isBlank(keyword)) {
            return Y9Result.failure("请输入搜索关键词");
        }
        return Y9Result.success(aiService.searchFilesForMention(keyword, limit));
    }

    /**
     * AI 设置文件加密密码
     */
    @RiseLog(operationName = "AI设置文件加密")
    @PostMapping("/files/encrypt")
    public Y9Result<Map<String, Object>> setFileEncryption(@RequestParam String fileId,
        @RequestParam(required = false) String password) {
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("文件ID不能为空");
        }
        return Y9Result.success(aiService.setFileEncryption(fileId, password));
    }

    /**
     * 获取可加密/已加密文件列表
     */
    @RiseLog(operationName = "获取可加密文件列表")
    @GetMapping("/files/encryptable")
    public Y9Result<List<Map<String, Object>>> getEncryptableFiles(@RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "20") int limit) {
        return Y9Result.success(aiService.getEncryptableFiles(keyword, limit));
    }

    // ==================== 高级查询（自然语言） ====================

    /**
     * AI 智能查询 — 统一自然语言入口，自动识别查询类型（搜索/统计/分析）
     * <p>
     * 支持示例：<br>
     * - "有哪些文件超过100MB？"<br>
     * - "查找所有包含'方案'的文档"<br>
     * - "帮我分析最近的存储使用情况"<br>
     * - "今天上传了什么文件"
     */
    @RiseLog(operationName = "AI智能查询")
    @GetMapping("/smartQuery")
    public Y9Result<Map<String, Object>> smartQuery(@RequestParam String query,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        if (StringUtils.isBlank(query)) {
            return Y9Result.failure("请输入查询内容");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        }
        Map<String, Object> result = aiService.aiSearch(query, page, size);
        return Y9Result.success(result);
    }

    // ==================== AI 聊天中上传文件到"我的文件" ====================

    /**
     * AI 聊天中上传文件到"我的文件"<br>
     * 用户在输入框输入"@我的文件上传文件"触发的快捷上传，将文件直接存入"我的文件"列表。
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @RiseLog(operationName = "AI聊天上传文件")
    @PostMapping("/uploadFile")
    public Y9Result<Map<String, Object>> uploadFile(@RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Y9Result.failure("请选择要上传的文件");
        }
        try {
            // 直接上传到"我的文件"（parentId="my", listType="my"），内部自动触发 AI 索引
            Map<String, Object> result = fileNodeService.saveUploadFile(file, "my", "my");
            return Y9Result.success(result);
        } catch (Exception e) {
            LOGGER.error("AI聊天上传文件失败", e);
            return Y9Result.failure("文件上传失败：" + e.getMessage());
        }
    }

}
