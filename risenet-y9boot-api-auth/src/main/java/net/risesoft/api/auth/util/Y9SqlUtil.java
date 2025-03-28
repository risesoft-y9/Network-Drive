package net.risesoft.api.auth.util;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import com.zaxxer.hikari.HikariDataSource;

public class Y9SqlUtil {

    public static HikariDataSource createDataSource(String url, String username, String password, String driver) {
        HikariDataSource druidDataSource = new HikariDataSource();
        druidDataSource.setJdbcUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setDriverClassName(driver);
        druidDataSource.setMaximumPoolSize(20);// 连接池
        druidDataSource.setMinimumIdle(1);
        return druidDataSource;
    }

    public static List<Map<String, Object>> executeQuery(String sql, String url, String username, String password,
        String driver) {
        // 创建 JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
        // 执行查询
        return jdbcTemplate.queryForList(sql);
    }

    public static List<Map<String, Object>> executeQuery(String sql, String url, String username, String password,
        String driver, @Nullable Object... args) {
        // 创建 JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
        // 执行查询
        return jdbcTemplate.queryForList(sql, args);
    }

    public static int executeUpdate(String sql, String url, String username, String password, String driver) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
        // 返回受影响的行数
        return jdbcTemplate.update(sql);
    }

    public static int executeUpdate(String sql, String url, String username, String password, String driver,
        @Nullable Object... args) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
        // 返回受影响的行数
        return jdbcTemplate.update(sql, args);
    }

}
