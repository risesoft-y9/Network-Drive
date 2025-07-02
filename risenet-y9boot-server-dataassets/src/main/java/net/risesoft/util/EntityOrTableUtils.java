package net.risesoft.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.risesoft.consts.SqlConstants;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;

@Component
public class EntityOrTableUtils {

    private static JdbcTemplate jdbcTemplate4Tenant;

    public EntityOrTableUtils(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant) {
        EntityOrTableUtils.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
    }

    public static Map<String, Object> convertToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<String, Object> getFieldLength(Class<?> entityClass) {
        Map<String, Object> map = new HashMap<>(16);
        // 获取 User 类的所有字段
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            // 检查字段是否有 @Column 注解
            if (field.isAnnotationPresent(Column.class)) {
                // 获取 @Column 注解
                Column columnAnnotation = field.getAnnotation(Column.class);
                // 获取字段类型
                String fieldType = field.getType().getSimpleName();
                // 获取 length 属性
                int length = columnAnnotation.length();
                // 打印字段名、类型和长度信息
                System.out.println("字段名: " + field.getName());
                System.out.println("字段类型: " + fieldType);
                if (length > 0) {
                    System.out.println("长度限制: " + length);
                    map.put(field.getName(), length);
                } else {
                    System.out.println("没有设置长度限制");
                }
                System.out.println("-------------------");
            }
        }
        return map;
    }

    public static Map<String, Object> getTableComment(Class<?> entityClass) {
        Map<String, Object> map = new HashMap<>(16);
        String tableName = getTableName(entityClass);
        Connection connection = null;
        try {
            // 获取数据库连接
            connection = jdbcTemplate4Tenant.getDataSource().getConnection();
            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            // 获取用户表的列信息
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");

                // 输出列信息
                System.out.println("列名: " + columnName);
                System.out.println("数据类型: " + columnType);
                System.out.println("字段大小: " + columnSize);
                System.out.println("-------------------");
            }
            columns.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static List<Map<String, Object>> getTableFieldList(Class<?> entityClass, String tableNameStr) {
        List<Map<String, Object>> list_map = new ArrayList<Map<String, Object>>();
        String tableName = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (StringUtils.isNotBlank(tableNameStr)) {
                tableName = tableNameStr;
            } else {
                tableName = getTableName(entityClass);
            }
            conn = jdbcTemplate4Tenant.getDataSource().getConnection();

            String sql = "show tables like '" + tableName + "'";
            String dbType = DbMetaDataUtil.getDatabaseDialectName(jdbcTemplate4Tenant.getDataSource());
            if (SqlConstants.DBTYPE_ORACLE.equalsIgnoreCase(dbType) || SqlConstants.DBTYPE_DM.equalsIgnoreCase(dbType)
                || SqlConstants.DBTYPE_KINGBASE.equalsIgnoreCase(dbType)) {
                sql = "SELECT table_name FROM all_tables where table_name = '" + tableName + "'";
            }

            List<Map<String, Object>> list = jdbcTemplate4Tenant.queryForList(sql);
            if (list == null || list.size() == 0) {
                return list_map;
            }
            sql = "Select * from " + tableName + " limit 0,1";

            if (SqlConstants.DBTYPE_ORACLE.equalsIgnoreCase(dbType) || SqlConstants.DBTYPE_DM.equalsIgnoreCase(dbType)
                || SqlConstants.DBTYPE_KINGBASE.equalsIgnoreCase(dbType)) {
                sql = "Select * from \"" + tableName + "\"  where rownum = 0";
            }

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            System.out.println("字段名\t\t类型\\t\t长度\t\t注释");
            while (columns.next()) {
                Map<String, Object> map = new HashMap<>(16);
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String columnComment = columns.getString("REMARKS"); // 字段注释
                System.out.printf("%s\t\t%s\t\t%s\t\t%s%n", columnName, columnType, columnSize, columnComment);
                map.put("fieldName", columnName);
                map.put("fieldType", columnType.toLowerCase());
                map.put("fieldLength", columnSize);
                map.put("comment", columnComment);
                list_map.add(map);
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
        return list_map;
    }

    public static List<Map<String, Object>> getEntityFieldList(Class<?> entityClass) {
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

    public static String getTableName(Class<?> entityClass) {
        Annotation annotation = entityClass.getAnnotation(Table.class);
        if (annotation instanceof Table) {
            Table tableAnnotation = (Table)annotation;
            return tableAnnotation.name();
        }
        return null;
    }

    /**
     * 将驼峰命名的 JSON 字符串转换为下划线命名的 JSON 字符串
     * 
     * @param jsonString 驼峰命名的 JSON 字符串
     * @return 下划线命名的 JSON 字符串
     */
    public static String convertSnakeToCamel(String jsonString) {
        try {
            ObjectMapper snakeCaseMapper = new ObjectMapper();
            snakeCaseMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            ObjectMapper camelCaseMapper = new ObjectMapper();
            camelCaseMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

            ObjectNode jsonNode = (ObjectNode)snakeCaseMapper.readTree(jsonString);
            return camelCaseMapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将下划线命名的字符串转换为驼峰命名的字符串
     * 
     * @param input 下划线命名的字符串
     * @return
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder camelCaseString = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    camelCaseString.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    camelCaseString.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return camelCaseString.toString();
    }

}
