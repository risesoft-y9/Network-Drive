package net.risesoft;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.Category;
import net.risesoft.enums.DataSourceEnums;
import net.risesoft.service.CategoryService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.y9.Y9LoginUserHolder;

@Component
@Slf4j
@RequiredArgsConstructor
public class OnApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

    private final MetadataConfigService metadataConfigService;

    private final CategoryService categoryService;
    private final Environment environment;
    @Value("${y9.app.dataAssets.tenantId}")
    private String tenantId;

    private void createCategoryConfig() {
        // 初始化分类配置,暂未考虑后续加分类后更新分类配置的情况
        List<Category> categories = categoryService.findByCategorySource(DataSourceEnums.SYSTEM_DEFAULT.getValue());
        if (categories.size() == 0 || categories.isEmpty()) {
            categoryService.initCategoryData();
        }
    }

    private void createMetadataConfig() {
        // 初始化元数据配置,暂未考虑后续加字段后更新元数据配置的情况
        if (!metadataConfigService.exitMetadataConfig()) {
            metadataConfigService.initMetadataConfig();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("DataAssets service ApplicationReady...");
        Y9LoginUserHolder.setTenantId(tenantId);
        createCategoryConfig();
        createMetadataConfig();
    }

    @PostConstruct
    public void init() {
        IdCode.init(environment.getProperty("idCode.api_code"), environment.getProperty("idCode.api_key"),
            environment.getProperty("idCode.idCode_url"), environment.getProperty("idCode.main_code"),
            environment.getProperty("idCode.analyze_url"), environment.getProperty("idCode.goto_url"),
            environment.getProperty("idCode.sample_url"));
    }

}
