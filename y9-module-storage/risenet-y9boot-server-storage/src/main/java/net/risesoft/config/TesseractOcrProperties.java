package net.risesoft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Tesseract OCR 引擎配置属性
 * <p>
 * 用于图片文字识别（OCR）功能。Tesseract 原生库需要单独安装，
 * 配置安装路径后，tess4j 通过 JNA 加载原生 DLL 进行图片文字提取。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tesseract")
public class TesseractOcrProperties {

    /** Tesseract OCR 安装目录路径，例如 D:/Program Files/Tesseract-OCR */
    private String installPath = "";

    /** 缓存配置值供静态工具类使用 */
    private static volatile TesseractOcrProperties INSTANCE;

    public TesseractOcrProperties() {
        INSTANCE = this;
    }

    /** 供静态工具类获取 Tesseract 安装路径 */
    public static String getInstallPath() {
        TesseractOcrProperties props = INSTANCE;
        if (props != null && props.installPath != null && !props.installPath.isEmpty()) {
            return props.installPath;
        }
        return "";
    }
}
