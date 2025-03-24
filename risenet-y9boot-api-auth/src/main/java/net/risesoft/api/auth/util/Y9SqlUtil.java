package net.risesoft.api.auth.util;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import com.alibaba.druid.pool.DruidDataSource;

public class Y9SqlUtil {
	
	public static DataSource createDataSource(String url, String username, String password, String driver) {
		DruidDataSource druidDataSource = new DruidDataSource();
		druidDataSource.setUrl(url);
		druidDataSource.setUsername(username);
		druidDataSource.setPassword(password);
		druidDataSource.setDriverClassName(driver);
		druidDataSource.setConnectionErrorRetryAttempts(2); // 失败后重连的次数
		druidDataSource.setBreakAfterAcquireFailure(true); // 请求失败之后中断
		druidDataSource.setMaxWait(5000);//最长等待时间（超时时间）
		return druidDataSource;
	}
	
	public static List<Map<String, Object>> executeQuery(String sql, String url, String username, 
			String password, String driver) {
		// 创建 JdbcTemplate
		JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
		// 执行查询
		return jdbcTemplate.queryForList(sql);
	}
	
	public static List<Map<String, Object>> executeQuery(String sql, String url, String username, 
			String password, String driver, @Nullable Object... args) {
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
	
	public static int executeUpdate(String sql, String url, String username, String password, String driver, @Nullable Object... args) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(url, username, password, driver));
        // 返回受影响的行数
        return jdbcTemplate.update(sql, args);
    }

}
