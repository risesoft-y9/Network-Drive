package net.risesoft.service;

import java.util.List;

import org.springframework.data.domain.Page;

import net.risesoft.entity.MetadataConfig;

public interface MetadataConfigService {

    boolean exitMetadataConfig();

    MetadataConfig saveMetadataConfig(MetadataConfig metadataConfig);

    MetadataConfig save(MetadataConfig metadataConfig);

    MetadataConfig findByColumnName(String columnName);

    MetadataConfig findById(String id);

    Page<MetadataConfig> listByViewType(String viewType, int page, int rows);

    List<MetadataConfig> listByViewType(String viewType);

    List<MetadataConfig> findByViewType(String viewType);

    List<MetadataConfig> getMetadataFieldList(String viewType, Integer isListShow);

    /**
     * 获取元数据著录必填配置列表
     *
     * @param viewType
     * @return
     */
    List<MetadataConfig> getMetadataRecordConfigList(String viewType);

    void saveOrder(String[] idAndTabIndexs);

    void initMetadataConfig();

    void initMetadataConfigByViewType(String viewType);

    void save(String saveType, String viewType, String tableField, String columnName, String displayName,
        String dataType, int fieldLength, String fieldOrigin);

    MetadataConfig findByViewTypeAndTableField(String viewType, String tableField);

    MetadataConfig findByViewTypeAndColumnName(String viewType, String columnName);

    void initBaseMetadataConfig(String viewType);

}
