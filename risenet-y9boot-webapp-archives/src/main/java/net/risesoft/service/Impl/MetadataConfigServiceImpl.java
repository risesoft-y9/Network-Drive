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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import net.risesoft.consts.PunctuationConsts;
import net.risesoft.consts.SqlConstants;
import net.risesoft.entity.Archives;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.enums.FileCategory;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.MetadataConfigRepository;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;

@Service
public class MetadataConfigServiceImpl implements MetadataConfigService {

    private final JdbcTemplate jdbcTemplate4Tenant;
    private final MetadataConfigRepository metadataConfigRepository;

    public MetadataConfigServiceImpl(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant,
        MetadataConfigRepository metadataConfigRepository) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.metadataConfigRepository = metadataConfigRepository;
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
            Integer tabIndex = metadataConfigRepository.getMaxTabIndex();
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
    public List<MetadataConfig> listAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "tabIndex");
        return metadataConfigRepository.findAll(sort);
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
    public void resetConfig() {
        metadataConfigRepository.deleteAll();
        List<Map<String, Object>> fieldList = this.getEntityFieldList(Archives.class);
        int i = 0;
        for (Map<String, Object> field : fieldList) {
            String fieldName = (String)field.get("fieldName");
            String fieldType = (String)field.get("fieldType");
            String comment = (String)field.get("comment");
            initData(FileCategory.WENJIAN.getValue(), fieldName, fieldType, comment, i);
            // initData(FileCategory.ANJIAN.getValue(), fieldName, fieldType, comment, i);
            i++;
        }
    }

    private void initData(String category, String fieldName, String fieldType, String comment, int i) {
        // TODO 初始化数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!fieldName.equals("id")) {
            MetadataConfig config = new MetadataConfig();
            config.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            config.setViewType(category);
            config.setFieldOrigin("系统内置");
            config.setColumnName(fieldName);
            config.setDisPlayName(comment);
            config.setDataType(fieldType);
            config.setIsListShow(1);
            config.setDisPlayAlign("center");
            config.setTabIndex(i);
            config.setUserName("系统管理员");
            config.setCreateTime(sdf.format(new Date()));
            config.setUpdateTime(sdf.format(new Date()));
            config.setDisPlayWidth("100");
            config.setIsRecord(1);
            config.setIsOrder(1);
            this.save(config);
        }
    }
}
