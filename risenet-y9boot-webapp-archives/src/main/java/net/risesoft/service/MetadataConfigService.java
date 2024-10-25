package net.risesoft.service;

import java.util.List;
import java.util.Map;

import net.risesoft.entity.MetadataConfig;

public interface MetadataConfigService {

    boolean exitMetadataConfig();

    Map<String, Object> getTableFieldList(Class<?> entityClass);

    List<Map<String, Object>> getEntityFieldList(Class<?> entityClasst);

    MetadataConfig saveMetadataConfig(MetadataConfig metadataConfig);

    MetadataConfig save(MetadataConfig metadataConfig);

    MetadataConfig findByColumnName(String columnName);

    List<MetadataConfig> listAll();

    void saveOrder(String[] idAndTabIndexs);

    void resetConfig();
}
