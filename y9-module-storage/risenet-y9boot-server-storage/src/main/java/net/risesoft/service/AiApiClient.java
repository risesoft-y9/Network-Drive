package net.risesoft.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.config.AiProperties;

/**
 * AI API 客户端（OpenAI 兼容协议）
 * <p>
 * 对接所有兼容 OpenAI /v1/chat/completions 接口的大模型服务。
 * <p>
 * 支持的模型示例：
 * <ul>
 * <li>DeepSeek: baseUrl=https://api.deepseek.com, model=deepseek-chat</li>
 * <li>通义千问: baseUrl=https://dashscope.aliyuncs.com/compatible-mode/v1, model=qwen-turbo</li>
 * <li>智谱GLM: baseUrl=https://open.bigmodel.cn/api/paas/v4, model=glm-4</li>
 * <li>Kimi: baseUrl=https://api.moonshot.cn/v1, model=moonshot-v1-8k</li>
 * <li>Ollama本地: baseUrl=http://localhost:11434/v1, model=qwen2:7b</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiApiClient {

    private final RestTemplate aiRestTemplate;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 非流式对话 ====================

    /**
     * 发送对话请求（非流式）
     *
     * @param messages 消息列表 [{"role":"user"/"system"/"assistant","content":"..."}]
     * @return AI 回复文本；若 AI 未启用则返回 null
     */
    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> messages) {
        if (!aiProperties.isEnabled()) {
            return null;
        }

        Map<String, Object> body = buildRequestBody(messages, false);
        String url = aiProperties.getBaseUrl() + "/chat/completions";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey());

            String jsonBody = objectMapper.writeValueAsString(body);
            LOGGER.debug("AI 请求 URL={}, model={}", url, aiProperties.getModel());

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = aiRestTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                LOGGER.error("AI API 返回错误, status={}, body={}", response.getStatusCode(), response.getBody());
                return null;
            }

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>)result.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>)choices.get(0).get("message");
                return (String)message.get("content");
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("AI API 调用失败", e);
            return null;
        }
    }

    /**
     * 发送对话请求（带系统提示词）
     */
    public String chatWithSystem(String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", aiProperties.getSystemPrompt()));
        messages.add(buildMessage("user", userMessage));
        return chat(messages);
    }

    /**
     * 发送对话请求（带系统提示词 + 文件上下文）
     */
    public String chatWithFileContext(String userMessage, String fileContent, String fileName) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", aiProperties.getSystemPrompt()));

        String contextMsg = "用户正在查看文件：「" + fileName + "」，以下是文件的部分内容：\n\n```\n"
            + (fileContent != null ? fileContent : "(无法提取文件内容)") + "\n```\n\n请基于以上文件内容回答用户的问题：\n" + userMessage;
        messages.add(buildMessage("user", contextMsg));
        return chat(messages);
    }

    // ==================== 流式对话 (SSE) ====================

    /**
     * 发送流式对话请求（SSE），逐 Token 回调
     *
     * @param messages 消息列表
     * @param onToken 每个 Token 的回调（用于收集完整回复、写入 SSE 等）
     * @return 完整回复文本
     */
    @SuppressWarnings("unchecked")
    public String chatStream(List<Map<String, String>> messages, Consumer<String> onToken) {
        if (!aiProperties.isEnabled()) {
            return null;
        }

        Map<String, Object> body = buildRequestBody(messages, true);
        String url = aiProperties.getBaseUrl() + "/chat/completions";

        // 流式请求需要更长的超时
        SimpleClientHttpRequestFactory streamFactory = new SimpleClientHttpRequestFactory();
        streamFactory.setConnectTimeout(aiProperties.getConnectTimeout());
        streamFactory.setReadTimeout(aiProperties.getReadTimeout());
        streamFactory.setBufferRequestBody(false);

        StringBuilder fullReply = new StringBuilder();

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            LOGGER.debug("AI 流式请求 URL={}, model={}", url, aiProperties.getModel());

            org.springframework.http.client.ClientHttpRequest request =
                streamFactory.createRequest(java.net.URI.create(url), org.springframework.http.HttpMethod.POST);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            request.getHeaders().setBearerAuth(aiProperties.getApiKey());
            request.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));

            try (org.springframework.http.client.ClientHttpResponse httpResponse = request.execute();
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(httpResponse.getBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty())
                        continue;
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data))
                            break;

                        try {
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>)chunk.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>)choices.get(0).get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String token = (String)delta.get("content");
                                    if (token != null) {
                                        fullReply.append(token);
                                        if (onToken != null) {
                                            onToken.accept(token);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.debug("解析流式 chunk 失败: {}", data);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("AI 流式调用失败", e);
        }

        return fullReply.toString();
    }

    // ==================== 文件相关 ====================

    /**
     * AI 智能分析文件内容
     */
    public String analyzeFileContent(String fileName, String fileContent, String analysisType) {
        if (!aiProperties.isEnabled()) {
            return null;
        }

        String typeDesc;
        switch (analysisType) {
            case "summary":
                typeDesc = "请生成以下文件的内容摘要（300字以内）";
                break;
            case "keywords":
                typeDesc = "请提取文件的关键词（5-10个，逗号分隔）";
                break;
            case "entity":
                typeDesc = "请识别文件中的实体信息（人物、组织、日期、地点等）";
                break;
            default:
                typeDesc = "请分析以下文件内容";
                break;
        }

        String prompt = typeDesc + "：\n\n文件名称：「" + fileName + "」\n\n文件内容：\n```\n"
            + (fileContent != null ? fileContent : "(文件内容为二进制格式，无法直接提取文本)") + "\n```";

        return chatWithSystem(prompt);
    }

    /**
     * AI 智能打标签
     */
    @SuppressWarnings("unchecked")
    public List<String> generateTags(String fileName, String fileContent, String fileSuffix) {
        if (!aiProperties.isEnabled()) {
            return null;
        }

        String prompt = "请为以下文件生成3-5个中文标签（每个标签2-7个字），以 JSON 数组格式返回，只返回数组不要其他内容。\n\n" + "文件名：「" + fileName + "」\n"
            + "文件类型：" + fileSuffix + "\n"
            + (fileContent != null ? "文件内容片段：\n```\n" + fileContent + "\n```" : "（二进制文件，请根据文件名和类型推断）");

        String reply = chatWithSystem(prompt);
        if (reply != null) {
            try {
                // 尝试从回复中提取 JSON 数组
                reply = reply.trim();
                if (reply.startsWith("```")) {
                    reply = reply.replaceAll("```json|```", "").trim();
                }
                return objectMapper.readValue(reply, List.class);
            } catch (Exception e) {
                LOGGER.warn("AI 标签解析失败, reply={}", reply);
            }
        }
        return null;
    }

    /**
     * AI 文档问答
     */
    public String documentQa(String fileName, String fileContent, String question) {
        return chatWithFileContext(question, fileContent, fileName);
    }

    // ==================== 内部方法 ====================

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private Map<String, Object> buildRequestBody(List<Map<String, String>> messages, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
        body.put("max_tokens", aiProperties.getMaxTokens());
        body.put("temperature", aiProperties.getTemperature());
        body.put("stream", stream);
        return body;
    }
}
