package net.risesoft.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.consts.SqlConstants;
import net.risesoft.pojo.Y9Page;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Y9FormDbMetaDataUtil extends DbMetaDataUtil {

    public static DataSource createDataSource(String url, String driverClassName, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }
    
    /**
     * 判断数据库连接状态
     */
    public static Boolean getConnection(String driverClass, String userName, String passWord, String url) {
        boolean flag = false;
        Connection connection = null;
        try {
            Class.forName(driverClass);
            connection = DriverManager.getConnection(url, userName, passWord);
            flag = true;
        } catch (ClassNotFoundException e) {
        	LOGGER.error(driverClass + "-驱动包不存在");
        } catch (SQLException e) {
        	LOGGER.error(url + "-连接失败：" + e.getMessage());
        } finally {
            ReleaseResource(connection, null, null, null);
        }
        return flag;
    }

    /**
     * 释放资源
     */
    public static void ReleaseResource(Connection connection, PreparedStatement pstm, ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstm != null) {
            try {
                pstm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回所有的业务表
     */
    public static List<Map<String, Object>> listAllTables(DataSource dataSource) throws Exception {
        List<Map<String, Object>> tableNames = new ArrayList<>();

        ResultSet rs = null;
        String sql = "show tables";
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {

            String dialect = getDatabaseDialectNameByConnection(connection);
            switch (dialect) {
                case SqlConstants.DBTYPE_ORACLE:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                case SqlConstants.DBTYPE_DM:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                case SqlConstants.DBTYPE_KINGBASE:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                default:
                    sql = "show tables";
                    break;
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tableName = rs.getString(1);
                // mysql中不处理视图
                Map<String, Object> al = new HashMap<>(16);
                al.put("name", tableName);
                tableNames.add(al);
            }
        } catch (Exception ex) {
            LOGGER.error("查询所有的业务表失败", ex);
            throw ex;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return tableNames;
    }

    /**
     * 返回表数据
     */
    public static Y9Page<Map<String, Object>> listTableData(DataSource dataSource, String columnNameAndValues,
        String tableName, int pageNum, int pageSize) throws Exception {
        List<Map<String, Object>> tableDataList = new ArrayList<>();
        int totalRecords = 0;
        int totalPages = 0;
        ResultSet rs = null;
        StringBuilder sqlStr = new StringBuilder();

        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            int offset = (pageNum - 1) * pageSize;
            String dialect = getDatabaseDialectNameByConnection(connection);
            String conditionSql = "";
            if (StringUtils.isNotBlank(columnNameAndValues)) {
                String[] arr = columnNameAndValues.split(";");

                for (int i = 0; i < arr.length; i++) {
                    String[] arrTemp = arr[i].split(":");
                    if (arrTemp.length == 2) {
                        String value = arrTemp[1];
                        String columnName = arrTemp[0].toUpperCase();
                        if (StringUtils.isBlank(conditionSql)) {
                            conditionSql += "INSTR(T." + columnName + ",'" + value + "') > 0 ";
                        } else {
                            conditionSql += " AND INSTR(T." + columnName + ",'" + value + "') > 0 ";
                        }
                    }
                }
            }

            if (SqlConstants.DBTYPE_ORACLE.equals(dialect) || SqlConstants.DBTYPE_DM.equals(dialect)
                || SqlConstants.DBTYPE_KINGBASE.equals(dialect)) {
                // sqlStr.append("SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM \"").append(tableName)
                // .append("\" ) a ");
                // sqlStr.append("WHERE ROWNUM <= ").append(offset + pageSize).append(") ");
                // sqlStr.append("WHERE rnum > ").append(offset);
                sqlStr = new StringBuilder("SELECT * FROM \"" + tableName + "\"  T WHERE ");
                if (StringUtils.isNotBlank(conditionSql)) {
                    sqlStr.append(conditionSql).append(" AND ");
                }
                sqlStr.append(" ROWNUM BETWEEN " + (offset + 1) + " AND " + (offset + pageSize));
                // 添加Oracle特定的分页逻辑
                // sqlStr.append("WHERE ROWNUM BETWEEN ").append((pageNum - 1) * pageSize + 1).append(" AND ")
                // .append(pageNum * pageSize).append(" AND ").append(conditionSql);
            } else if (SqlConstants.DBTYPE_MYSQL.equals(dialect)) {
                sqlStr = new StringBuilder("SELECT * FROM " + tableName + "  T ");
                if (StringUtils.isNotBlank(conditionSql)) {
                    sqlStr.append("WHERE ").append(conditionSql);
                }
                sqlStr.append(" LIMIT ").append(offset).append(",").append(pageSize);
                // sqlStr.append("SELECT * FROM ").append(tableName).append(" T LIMIT ").append(offset).append(", ")
                // .append(pageSize).append(" AND ").append(conditionSql);
            } else {
                throw new UnsupportedOperationException("不支持的数据库类型");
            }

            rs = stmt.executeQuery(sqlStr.toString());
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    rowMap.put(columnName, columnValue);
                }
                tableDataList.add(rowMap);
            }
            // 获取总记录数
            String countSql = "SELECT COUNT(*) FROM " + tableName + " T "
                + (StringUtils.isNotBlank(conditionSql) ? " WHERE " + conditionSql : "");
            try (Statement countStmt = connection.createStatement();
                ResultSet countRs = countStmt.executeQuery(countSql)) {
                if (countRs.next()) {
                    totalRecords = countRs.getInt(1);
                }
            }
            // 计算总页数
            totalPages = (int)Math.ceil((double)totalRecords / pageSize);
        } catch (Exception ex) {
            LOGGER.error("查询表数据失败", ex);
            throw ex;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return Y9Page.success(pageNum, totalPages, totalRecords, tableDataList, "获取数据成功");
    }

    /**
     * 返回所有的业务表
     */
    public static Map<String, Object> listAllTableNames(DataSource dataSource) throws Exception {
        Map<String, Object> al = new HashMap<>(16);

        ResultSet rs = null;
        String sql = "show tables";
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {

            String dialect = getDatabaseDialectNameByConnection(connection);
            switch (dialect) {
                case SqlConstants.DBTYPE_ORACLE:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                case SqlConstants.DBTYPE_DM:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                case SqlConstants.DBTYPE_KINGBASE:
                    sql = "SELECT table_name FROM all_tables";
                    break;
                default:
                    sql = "show tables";
                    break;
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tableName = rs.getString(1);
                // mysql中不处理视图
                al.put(tableName.toLowerCase(), tableName);
            }
        } catch (Exception ex) {
            LOGGER.error("查询所有的业务表失败", ex);
            throw ex;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return al;
    }

    public static List<Map<String, Object>> listTypes(DataSource dataSource) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String dialect = getDatabaseDialectName(dataSource);
            switch (dialect) {
                case SqlConstants.DBTYPE_MYSQL:
                    list = listTypes4Mysql();
                    break;
                case SqlConstants.DBTYPE_ORACLE:
                    list = listTypes4Oracle();
                    break;
                case SqlConstants.DBTYPE_DM:
                    list = listTypes4Dm();
                    break;
                case SqlConstants.DBTYPE_KINGBASE:
                    list = listTypes4Kingbase();
                    break;
                default:
                    list = listTypes4Mysql();
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return list;
    }

    private static List<Map<String, Object>> listTypes4Dm() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);
        map.put("typeName", "VARCHAR2");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "NUMBER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "LONG");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "TIMESTAMP");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "BLOB");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "DATE");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "INTEGER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "CLOB");
        list.add(map);
        return list;
    }

    private static List<Map<String, Object>> listTypes4Kingbase() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);
        map.put("typeName", "VARCHAR2");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "NUMBER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "LONG");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "TIMESTAMP");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "BLOB");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "DATE");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "INTEGER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "CLOB");
        list.add(map);
        return list;
    }

    private static List<Map<String, Object>> listTypes4Mysql() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);
        map.put("typeName", "varchar");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "int");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "integer");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "longblob");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "text");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "longtext");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "datetime");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "date");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "bit");
        return list;
    }

    private static List<Map<String, Object>> listTypes4Oracle() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);
        map.put("typeName", "VARCHAR2");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "NUMBER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "LONG");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "TIMESTAMP");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "BLOB");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "DATE");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "INTEGER");
        list.add(map);
        map = new HashMap<>(16);
        map.put("typeName", "CLOB");
        list.add(map);
        return list;
    }
}
