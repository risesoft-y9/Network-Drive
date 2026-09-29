package net.risesoft.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.risesoft.service.Impl.TencentOcrService;
import net.risesoft.util.FileContentExtractor;

/**
 * OCR 服务桥接器：在 Spring 容器启动完成后，把 TencentOcrService 注入到 FileContentExtractor 静态方法。
 *
 * @author yihong
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OcrServiceBridge {

    @Autowired(required = false)
    private TencentOcrService tencentOcrService;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        FileContentExtractor.setTencentOcrService(tencentOcrService);
        if (tencentOcrService != null) {
            LOGGER.info("OCR 服务桥接完成：腾讯云 OCR 已就绪");
        } else {
            LOGGER.info("OCR 服务桥接完成：腾讯云 OCR 未配置，将使用本地 Tesseract");
        }
    }
}