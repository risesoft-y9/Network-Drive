package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import net.risesoft.consts.PunctuationConsts;
import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.CategoryTableField;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DocumentFile;
import net.risesoft.entity.ImageFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.entity.VideoFile;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Manager;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.CategoryTableRepository;
import net.risesoft.repository.MetadataConfigRepository;
import net.risesoft.service.CategoryTableFieldService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.util.EntityOrTableUtils;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.org.ManagerApiClient;

@Service
public class MetadataConfigServiceImpl implements MetadataConfigService {

    private final JdbcTemplate jdbcTemplate4Tenant;
    private final MetadataConfigRepository metadataConfigRepository;
    private final ManagerApiClient managerApiClient;
    private final CategoryTableRepository categoryTableRepository;
    private final CategoryTableFieldService categoryTableFieldService;
    private final String ingoreFields =
        "id,categoryId,tenantId,tabIndex,detailId,dataAssetsId,ID,CATEGORY_ID,TENANT_ID,TABINDEX,DETAIL_ID,DATAASSETS_ID,FILE_STATUS,FILE_GRADE,IS_DELETED";

    public MetadataConfigServiceImpl(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant,
        MetadataConfigRepository metadataConfigRepository, ManagerApiClient managerApiClient,
        CategoryTableRepository categoryTableRepository, CategoryTableFieldService categoryTableFieldService) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.metadataConfigRepository = metadataConfigRepository;
        this.managerApiClient = managerApiClient;
        this.categoryTableRepository = categoryTableRepository;
        this.categoryTableFieldService = categoryTableFieldService;
    }

    @Override
    public boolean exitMetadataConfig() {
        List<MetadataConfig> list = metadataConfigRepository.findAll();
        return list != null && list.size() > 0;
    }

    @Override
    public MetadataConfig saveMetadataConfig(MetadataConfig metadataConfig) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        if (StringUtils.isBlank(metadataConfig.getId())) {
            metadataConfig.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            metadataConfig.setCreateTime(sdf.format(new Date()));
            metadataConfig.setUpdateTime(sdf.format(new Date()));
            metadataConfig.setUserId(userInfo.getPersonId());
            metadataConfig.setUserName(userInfo.getName());
            Integer tabIndex = metadataConfigRepository.getMaxTabIndex(metadataConfig.getViewType());
            metadataConfig.setTabIndex(tabIndex == null ? 1 : tabIndex + 1);
            return metadataConfigRepository.save(metadataConfig);
        }
        metadataConfig.setUpdateTime(sdf.format(new Date()));
        metadataConfig.setUserId(userInfo.getPersonId());
        metadataConfig.setUserName(userInfo.getName());
        return metadataConfigRepository.save(metadataConfig);
    }

    @Override
    public MetadataConfig save(MetadataConfig metadataConfig) {
        return metadataConfigRepository.save(metadataConfig);
    }

    @Override
    public MetadataConfig findByColumnName(String columnName) {
        return metadataConfigRepository.findByColumnName(columnName);
    }

    @Override
    public MetadataConfig findById(String id) {
        return metadataConfigRepository.findById(id).orElse(null);
    }

    @Override
    public Page<MetadataConfig> listByViewType(String viewType, int page, int rows) {
        Sort sort = Sort.by(Sort.Direction.ASC, "tabIndex");
        Pageable pageable = PageRequest.of(page - 1, rows, sort);
        return metadataConfigRepository.findByViewTypeAndIsHideFalse(viewType, pageable);
    }

    @Override
    public List<MetadataConfig> listByViewType(String viewType) {
        return metadataConfigRepository.findByViewTypeAndIsHideFalseOrderByTabIndex(viewType);
    }

    @Override
    public void saveOrder(String[] idAndTabIndexs) {
        List<String> list = Lists.newArrayList(idAndTabIndexs);
        try {
            for (String s : list) {
                String[] arr = s.split(PunctuationConsts.COLON);
                metadataConfigRepository.updateOrder(Integer.parseInt(arr[1]), arr[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initMetadataConfig() {
        metadataConfigRepository.deleteAll();
        Manager manager = managerApiClient.getByLoginName(Y9LoginUserHolder.getTenantId(), "systemManager").getData();

        List<CategoryEnums> categoryEnumsList =
            EnumSet.allOf(CategoryEnums.class).stream().collect(Collectors.toList());
        for (CategoryEnums categoryEnums : categoryEnumsList) {
            int i = 0;
            List<Map<String, Object>> fieldList = new ArrayList<>();
            // 初始化基础标准字段配置
            initBaseConfig(categoryEnums.getEnName(), manager.getId(), manager.getName(), "archives");
            if (categoryEnums.getEnName().equals("WS")) {
                fieldList.addAll(EntityOrTableUtils.getTableFieldList(DocumentFile.class, ""));
            } else if (categoryEnums.getEnName().equals("ZP")) {
                fieldList.addAll(EntityOrTableUtils.getTableFieldList(ImageFile.class, ""));
            } else if (categoryEnums.getEnName().equals("LX")) {
                fieldList.addAll(EntityOrTableUtils.getTableFieldList(VideoFile.class, ""));
            } else if (categoryEnums.getEnName().equals("LY")) {
                fieldList.addAll(EntityOrTableUtils.getTableFieldList(AudioFile.class, ""));
            }

            for (Map<String, Object> field : fieldList) {
                String fieldName = (String)field.get("fieldName");
                String fieldType = (String)field.get("fieldType");
                String comment = (String)field.get("comment");
                int fieldLength = (int)field.get("fieldLength");
                fieldName = EntityOrTableUtils.toCamelCase(fieldName);
                initData(categoryEnums.getEnName(), fieldName, fieldType, fieldLength, comment, manager.getId(),
                    manager.getName(), categoryEnums.getEnName());
            }
        }
    }

    public void initBaseConfig(String category, String userId, String userName, String fielOrigin) {
        List<Map<String, Object>> fieldList = EntityOrTableUtils.getTableFieldList(DataAssets.class, "");
        for (Map<String, Object> field : fieldList) {
            String fieldName = (String)field.get("fieldName");
            String fieldType = (String)field.get("fieldType");
            String comment = (String)field.get("comment");
            int fieldLength = (int)field.get("fieldLength");

            fieldName = EntityOrTableUtils.toCamelCase(fieldName);
            initData(category, fieldName, fieldType, fieldLength, comment, userId, userName, fielOrigin);
        }
    }

    @Override
    public void initMetadataConfigByViewType(String viewType) {
        List<MetadataConfig> configList =
            metadataConfigRepository.findByViewTypeAndIsHideFalseOrderByTabIndex(viewType);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        if (configList.size() > 0) {
            for (MetadataConfig config : configList) {
                Integer tabIndex = metadataConfigRepository.getMaxTabIndex(viewType);
                config.setTabIndex(null == tabIndex ? 1 : tabIndex + 1);
                config.setUserId(userInfo.getPersonId());
                config.setUserName(userInfo.getName());
                config.setCreateTime(sdf.format(new Date()));
                config.setUpdateTime(sdf.format(new Date()));
                config.setDisPlayWidth("100");
                config.setIsRecord(1);
                config.setIsOrder(1);
                config.setOpenSearch(0);
                config.setIsListShow(1);
                config.setDisPlayAlign("center");
                config.setRe_inputBoxType("input");
                config.setRe_inputBoxWidth("200");
                config.setRe_isOneLine(0);
                this.save(config);
            }
        }
    }

    @Override
    public void initCustomMetadataConfigByViewType(String viewType) {
        CategoryTable categoryTable = categoryTableRepository.findByCategoryMark(viewType);
        if (null != categoryTable) {
            List<CategoryTableField> categoryTableFieldList =
                categoryTableFieldService.listByTableId(categoryTable.getId());
            for (CategoryTableField categoryTableField : categoryTableFieldList) {
                this.save("add", viewType, categoryTableField.getId(), categoryTableField.getFieldName(),
                    categoryTableField.getFieldCnName(), categoryTableField.getFieldType(),
                    categoryTableField.getFieldLength(), viewType);
            }
        }
    }

    @Override
    public MetadataConfig findByViewTypeAndTableField(String viewType, String tableField) {
        return metadataConfigRepository.findByViewTypeAndTableFieldId(viewType, tableField);
    }

    @Override
    public MetadataConfig findByViewTypeAndColumnName(String viewType, String columnName) {
        return metadataConfigRepository.findByViewTypeAndColumnName(viewType, columnName);
    }

    @Override
    public void initBaseMetadataConfig(String viewType) {
        List<Map<String, Object>> fieldList = EntityOrTableUtils.getTableFieldList(DataAssets.class, "");
        String userId = Y9LoginUserHolder.getUserInfo().getPersonId(),
            userName = Y9LoginUserHolder.getUserInfo().getName();
        for (Map<String, Object> field : fieldList) {
            String fieldName = (String)field.get("fieldName");
            String fieldType = (String)field.get("fieldType");
            String comment = (String)field.get("comment");
            int fieldLength = (int)field.get("fieldLength");
            fieldName = EntityOrTableUtils.toCamelCase(fieldName);
            initData(viewType, fieldName, fieldType, fieldLength, comment, userId, userName, "archives");
        }
    }

    @Override
    public List<MetadataConfig> getMetadataFieldList(String viewType, Integer isListShow) {
        return metadataConfigRepository.findByViewTypeAndIsListShowAndIsHideFalseOrderByTabIndex(viewType, isListShow);
    }

    @Override
    public List<MetadataConfig> getMetadataRecordConfigList(String viewType) {
        return metadataConfigRepository.findByViewTypeAndIsRecordRequiredAndIsHideFalseOrderByTabIndex(viewType, 1);
    }

    @Override
    public List<MetadataConfig> getMetadataCheckedRequiredConfigList(String viewType) {
        return metadataConfigRepository.findByViewTypeAndIsCheckedRequiredAndIsHideFalse(viewType, 1);
    }

    @Override
    public void save(String saveType, String viewType, String tableField, String columnName, String displayName,
        String dataType, int fieldLength, String fieldOrigin) {
        // TODO 保存数据
        if (saveType.equals("add")) {
            // 再保存门类新增数据
            this.save(viewType, tableField, columnName, displayName, dataType, fieldLength, fieldOrigin);
        } else if (saveType.equals("edit")) {
            MetadataConfig metadataConfig = this.findByViewTypeAndTableField(viewType, tableField);
            if (null == metadataConfig) {
                this.save(viewType, tableField, columnName, displayName, dataType, fieldLength, fieldOrigin);
            } else {
                metadataConfig.setColumnName(columnName);
                metadataConfig.setDisPlayName(displayName);
                metadataConfig.setDataType(dataType);
                this.save(metadataConfig);
            }
        }
    }

    public void save(String viewType, String tableField, String columnName, String displayName, String dataType,
        int fieldLength, String fieldOrigin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();

        MetadataConfig config = new MetadataConfig();
        config.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        config.setViewType(viewType);
        config.setFieldOrigin(fieldOrigin);
        config.setColumnName(columnName);
        config.setDisPlayName(displayName);
        config.setDataType(dataType);
        config.setFieldLength(fieldLength);
        config.setIsListShow(1);
        config.setDisPlayAlign("center");
        Integer tabIndex = metadataConfigRepository.getMaxTabIndex(viewType);
        config.setTabIndex(null == tabIndex ? 1 : tabIndex + 1);
        config.setUserId(userInfo.getPersonId());
        config.setUserName(userInfo.getName());
        config.setCreateTime(sdf.format(new Date()));
        config.setUpdateTime(sdf.format(new Date()));
        config.setDisPlayWidth("100");
        config.setIsRecord(1);
        config.setRe_inputBoxType("input");
        config.setRe_inputBoxWidth("200");
        config.setRe_isOneLine(0);
        config.setIsOrder(1);
        config.setTableFieldId(tableField);
        config.setIsHide(false);
        if (ingoreFields.indexOf(columnName) != -1) {
            config.setIsHide(true);
        }
        this.save(config);
    }

    private void initData(String category, String fieldName, String fieldType, int fieldLength, String comment,
        String userId, String userName, String fielOrigin) {
        // TODO 初始化数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MetadataConfig config = new MetadataConfig();
        config.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        config.setViewType(category);
        config.setFieldOrigin(fielOrigin);
        config.setColumnName(fieldName);
        config.setDisPlayName(comment);
        config.setDataType(fieldType);
        config.setFieldLength(fieldLength);
        config.setIsListShow(1);
        config.setDisPlayAlign("center");
        Integer tabIndex = metadataConfigRepository.getMaxTabIndex(category);
        config.setTabIndex(null == tabIndex ? 1 : tabIndex + 1);
        config.setUserId(userId);
        config.setUserName(userName);
        config.setCreateTime(sdf.format(new Date()));
        config.setUpdateTime(sdf.format(new Date()));
        config.setDisPlayWidth("100");
        config.setIsRecord(1);
        config.setRe_inputBoxType("input");
        config.setRe_inputBoxWidth("200");
        config.setRe_isOneLine(0);
        config.setIsOrder(1);
        config.setIsHide(false);
        if (ingoreFields.indexOf(fieldName) != -1) {
            config.setIsHide(true);
        }
        this.save(config);
    }
}
