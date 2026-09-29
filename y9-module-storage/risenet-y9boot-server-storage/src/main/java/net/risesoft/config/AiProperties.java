package net.risesoft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * AI 大模型配置属性
 * <p>
 * 支持所有 OpenAI 兼容协议的模型，包括但不限于：
 * <ul>
 * <li>OpenAI (GPT-4, GPT-3.5)</li>
 * <li>DeepSeek (deepseek-chat, deepseek-reasoner)</li>
 * <li>通义千问 (qwen-turbo, qwen-plus, qwen-max)</li>
 * <li>文心一言 (ernie-bot)</li>
 * <li>智谱 ChatGLM (glm-4)</li>
 * <li>Moonshot/Kimi (moonshot-v1)</li>
 * <li>Ollama 本地部署 (llama3, qwen2, etc.)</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用 AI 功能（false 时回退到 stub 模式） */
    private boolean enabled = false;

    /** API 地址，例如 https://api.openai.com 或 https://dashscope.aliyuncs.com/compatible-mode */
    private String baseUrl = "https://api.openai.com";

    /** API Key */
    private String apiKey = "";

    /** 模型名称，例如 gpt-4, deepseek-chat, qwen-turbo */
    private String model = "gpt-3.5-turbo";

    /** 最大输出 Token 数 */
    private int maxTokens = 2000;

    /** 温度参数 0-2，越高越随机 */
    private double temperature = 0.7;

    /** 连接超时（毫秒） */
    private int connectTimeout = 30000;

    /** 读取超时（毫秒） */
    private int readTimeout = 120000;

    /** 系统提示词 */
    private String systemPrompt = "你是一个专业的文件管理助手，帮助用户管理网盘文件。"
        + "你可以帮助用户查找文件、分析文件内容、提取文件信息、进行数据对比等。"
        + "回答要简洁、准确，使用中文。";

    /** 文件上传后是否异步提取文本内容并建立索引 */
    private boolean indexOnUpload = true;

    /** 支持索引的文件类型（逗号分隔的扩展名） */
    private String indexableExtensions = "txt,csv,json,xml,md,log,java,py,js,ts,html,css,sql,yml,yaml,properties";

    /** 文件内容最大提取长度（字符数），防止超大文件占用过多 Token */
    private int maxContentLength = 10000;
}
