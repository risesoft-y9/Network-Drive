package net.risesoft.util;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import net.risesoft.consts.SqlConstants;
import net.risesoft.entity.Archives;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;

public class TableUtil {

    private static JdbcTemplate jdbcTemplate4Tenant = null;

    public TableUtil(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant) {
        TableUtil.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
    }

    public static String getTableName(Class<?> entityClass) {
        Annotation annotation = entityClass.getAnnotation(Table.class);
        if (annotation instanceof Table) {
            Table tableAnnotation = (Table)annotation;
            return tableAnnotation.name();
        }
        return null;
    }

    public static Map<String, Object> getTableFieldList(Class<?> entityClass) {
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
            ResultSetMetaData dt = rs.getMetaData();
            for (int i = 0; i < dt.getColumnCount(); i++) {
                String fieldName = dt.getColumnName(i + 1).toLowerCase();
                al.put(fieldName, fieldName);
            }
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
}
