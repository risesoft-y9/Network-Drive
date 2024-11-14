package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import net.risesoft.entity.MetadataConfig;

public interface MetadataConfigService {

    boolean exitMetadataConfig();

    Map<String, Object> getTableFieldList(Class<?> entityClass);

    List<Map<String, Object>> getEntityFieldList(Class<?> entityClasst);

    MetadataConfig saveMetadataConfig(MetadataConfig metadataConfig);

    MetadataConfig save(MetadataConfig metadataConfig);

    MetadataConfig findByColumnName(String columnName);

    MetadataConfig findById(String id);

    Page<MetadataConfig> listByViewType(String viewType, int page, int rows);

    List<MetadataConfig> listByViewType(String viewType);

    void saveOrder(String[] idAndTabIndexs);

    void initMetadataConfig();

    void initMetadataConfigByViewType(String viewType);

    void initCustomMetadataConfigByViewType(String viewType);

    void save(String saveType, String viewType, String tableField, String columnName, String displayName,
        String dataType, String fieldOrigin);

    MetadataConfig findByViewTypeAndTableField(String viewType, String tableField);

    MetadataConfig findByViewTypeAndColumnName(String viewType, String columnName);

    void initBaseMetadataConfig(String viewType);

    List<MetadataConfig> getMetadataFieldList(String viewType, Integer isListShow);

    void saveListFiledShow(String[] idAndIsShow);
}
