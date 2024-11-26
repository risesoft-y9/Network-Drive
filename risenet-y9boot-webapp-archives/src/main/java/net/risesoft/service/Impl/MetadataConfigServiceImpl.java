package net.risesoft.service.Impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import net.risesoft.consts.PunctuationConsts;
import net.risesoft.consts.SqlConstants;
import net.risesoft.entity.Archives;
import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.CategoryTableField;
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
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;

import y9.client.rest.platform.org.ManagerApiClient;

@Service
public class MetadataConfigServiceImpl implements MetadataConfigService {

    private final JdbcTemplate jdbcTemplate4Tenant;
    private final MetadataConfigRepository metadataConfigRepository;
    private final ManagerApiClient managerApiClient;
    private final CategoryTableRepository categoryTableRepository;
    private final CategoryTableFieldService categoryTableFieldService;
    private final String ingoreFields = "id,categoryId,tenantId,tabIndex,detailId,archivesId";

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
    public Map<String, Object> getTableFieldList(Class<?> entityClass) {
        Map<String, Object> al = new HashMap<>(16);
        String tableName = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            tableName = getTableName(Archives.class);
            conn = jdbcTemplate4Tenant.getDataSource().getConnection();

            String sql = "show tables like '" + tableName + "'";
            String dbType = DbMetaDataUtil.getDatabaseDialectName(jdbcTemplate4Tenant.getDataSource());
            if (SqlConstants.DBTYPE_ORACLE.equalsIgnoreCase(dbType) || SqlConstants.DBTYPE_DM.equalsIgnoreCase(dbType)
                || SqlConstants.DBTYPE_KINGBASE.equalsIgnoreCase(dbType)) {
                sql = "SELECT table_name FROM all_tables where table_name = '" + tableName + "'";
            }

            List<Map<String, Object>> list = jdbcTemplate4Tenant.queryForList(sql);
            if (list == null || list.size() == 0) {
                return al;
            }
            sql = "Select * from " + tableName + " limit 0,0";

            if (SqlConstants.DBTYPE_ORACLE.equalsIgnoreCase(dbType) || SqlConstants.DBTYPE_DM.equalsIgnoreCase(dbType)
                || SqlConstants.DBTYPE_KINGBASE.equalsIgnoreCase(dbType)) {
                sql = "Select * from \"" + tableName + "\"  where rownum = 0";
            }

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            System.out.println("字段名\t\t类型\t\t注释");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                String columnComment = columns.getString("REMARKS"); // 字段注释
                System.out.printf("%s\t\t%s\t\t%s%n", columnName, columnType, columnComment);
                al.put(columnName, columnComment);
            }
            // ResultSetMetaData dt = rs.getMetaData();
            // for (int i = 0; i < dt.getColumnCount(); i++) {
            // String fieldName = dt.getColumnName(i + 1).toLowerCase();
            // al.put(fieldName, fieldName);
            // }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return al;
    }

    public String getTableName(Class<?> entityClass) {
        Annotation annotation = entityClass.getAnnotation(Table.class);
        if (annotation instanceof Table) {
            Table tableAnnotation = (Table)annotation;
            return tableAnnotation.name();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getEntityFieldList(Class<?> entityClass) {
        List<Map<String, Object>> list_map = new ArrayList<>();

        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            Map<String, Object> map = new HashMap<>(16);
            // 打印字段名称
            System.out.println("字段名称: " + field.getName());
            // 获取字段上的 @Comment 注解
            Comment comment = field.getAnnotation(Comment.class);
            // 获取字段类型
            String fieldType = field.getType().getSimpleName();
            if (comment != null) {
                // 打印注解值
                System.out.println("注解值: " + comment.value());
                map.put("fieldName", field.getName());
                map.put("fieldType", fieldType);
                map.put("comment", comment.value());
                list_map.add(map);
            } else {
                System.out.println("没有注解");
            }
            // 获取字段值
            field.setAccessible(true); // 允许访问 private 字段
        }
        return list_map;
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
        return metadataConfigRepository.findByViewType(viewType, pageable);
    }

    @Override
    public List<MetadataConfig> listByViewType(String viewType) {
        return metadataConfigRepository.findByViewTypeOrderByTabIndex(viewType);
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
            if (categoryEnums.getEnName().equals("document")) {
                fieldList.addAll(this.getEntityFieldList(DocumentFile.class));
            } else if (categoryEnums.getEnName().equals("image")) {
                fieldList.addAll(this.getEntityFieldList(ImageFile.class));
            } else if (categoryEnums.getEnName().equals("video")) {
                fieldList.addAll(this.getEntityFieldList(VideoFile.class));
            } else if (categoryEnums.getEnName().equals("audio")) {
                fieldList.addAll(this.getEntityFieldList(AudioFile.class));
            }

            for (Map<String, Object> field : fieldList) {
                String fieldName = (String)field.get("fieldName");
                String fieldType = (String)field.get("fieldType");
                String comment = (String)field.get("comment");
                if (!fieldName.contains(ingoreFields)) {
                    initData(categoryEnums.getEnName(), fieldName, fieldType, comment, manager.getId(),
                        manager.getName(), categoryEnums.getEnName());
                }
            }
        }
    }

    public void initBaseConfig(String category, String userId, String userName, String fielOrigin) {
        List<Map<String, Object>> fieldList = this.getEntityFieldList(Archives.class);
        for (Map<String, Object> field : fieldList) {
            String fieldName = (String)field.get("fieldName");
            String fieldType = (String)field.get("fieldType");
            String comment = (String)field.get("comment");
            if (!fieldName.contains(ingoreFields)) {
                initData(category, fieldName, fieldType, comment, userId, userName, fielOrigin);
            }

        }
    }

    @Override
    public void initMetadataConfigByViewType(String viewType) {
        List<MetadataConfig> configList = metadataConfigRepository.findByViewTypeOrderByTabIndex(viewType);
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
                    categoryTableField.getFieldCnName(), categoryTableField.getFieldType(), viewType);
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
        List<Map<String, Object>> fieldList = this.getEntityFieldList(Archives.class);
        String userId = Y9LoginUserHolder.getUserInfo().getPersonId(),
            userName = Y9LoginUserHolder.getUserInfo().getName();
        for (Map<String, Object> field : fieldList) {
            String fieldName = (String)field.get("fieldName");
            String fieldType = (String)field.get("fieldType");
            String comment = (String)field.get("comment");
            initData(viewType, fieldName, fieldType, comment, userId, userName, "archives");
        }
    }

    @Override
    public List<MetadataConfig> getMetadataFieldList(String viewType, Integer isListShow) {
        return metadataConfigRepository.findByViewTypeAndIsListShowOrderByTabIndex(viewType, isListShow);
    }

    @Override
    public void saveListFiledShow(String[] idAndIsShow) {

    }

    @Override
    public void save(String saveType, String viewType, String tableField, String columnName, String displayName,
        String dataType, String fieldOrigin) {
        // TODO 保存数据
        if (saveType.equals("add")) {
            // 再保存门类新增数据
            this.save(viewType, tableField, columnName, displayName, dataType, fieldOrigin);
        } else if (saveType.equals("edit")) {
            MetadataConfig metadataConfig = this.findByViewTypeAndTableField(viewType, tableField);
            if (null == metadataConfig) {
                this.save(viewType, tableField, columnName, displayName, dataType, fieldOrigin);
            } else {
                metadataConfig.setColumnName(columnName);
                metadataConfig.setDisPlayName(displayName);
                metadataConfig.setDataType(dataType);
                this.save(metadataConfig);
            }
        }
    }

    public void save(String viewType, String tableField, String columnName, String displayName, String dataType,
        String fieldOrigin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        if (!columnName.contains(ingoreFields)) {
            MetadataConfig config = new MetadataConfig();
            config.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            config.setViewType(viewType);
            config.setFieldOrigin(fieldOrigin);
            config.setColumnName(columnName);
            config.setDisPlayName(displayName);
            config.setDataType(dataType);
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
            this.save(config);
        }
    }

    private void initData(String category, String fieldName, String fieldType, String comment, String userId,
        String userName, String fielOrigin) {
        // TODO 初始化数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // if (!fieldName.equals("id")) {
        MetadataConfig config = new MetadataConfig();
        config.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        config.setViewType(category);
        config.setFieldOrigin(fielOrigin);
        config.setColumnName(fieldName);
        config.setDisPlayName(comment);
        config.setDataType(fieldType);
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
        this.save(config);
        // }
    }
}
