package net.risesoft;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.service.MetadataConfigService;
import net.risesoft.y9.Y9LoginUserHolder;

@Component
@Slf4j
@RequiredArgsConstructor
public class OnApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

    private final MetadataConfigService metadataConfigService;

    @Value("${y9.app.archives.tenantId}")
    private String tenantId;

    private void createMetadataConfig() {
        // 初始化元数据配置,暂未考虑后续加字段后更新元数据配置的情况
        if (!metadataConfigService.exitMetadataConfig()) {
            metadataConfigService.resetConfig();
            // List<Map<String, Object>> fieldList = metadataConfigService.getEntityFieldList(Archives.class);
            // int i = 0;
            // for (Map<String, Object> field : fieldList) {
            // String fieldName = (String)field.get("fieldName");
            // String fieldType = (String)field.get("fieldType");
            // String comment = (String)field.get("comment");
            // //initData(FileCategory.WENJIAN.getValue(), fieldName, fieldType, comment, i);
            // // initData(FileCategory.ANJIAN.getValue(), fieldName, fieldType, comment, i);
            // i++;
            // }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("archives-service ApplicationReady...");
        Y9LoginUserHolder.setTenantId(tenantId);
        createMetadataConfig();
    }

}
