package net.risesoft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 腾讯云 OCR 配置
 *
 * @author yihong
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tencent-ocr")
public class TencentOcrProperties {

    /** 是否启用腾讯云 OCR */
    private boolean enabled = false;

    /** 腾讯云 API 密钥 ID */
    private String secretId;

    /** 腾讯云 API 密钥 Key */
    private String secretKey;

    /** OCR 接口区域（ap-guangzhou / ap-shanghai / ap-beijing） */
    private String region = "ap-guangzhou";

    /** 调用超时（秒） */
    private int timeout = 30;
}