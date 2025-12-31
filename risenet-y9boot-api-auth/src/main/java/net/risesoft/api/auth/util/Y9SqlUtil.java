package net.risesoft.api.auth.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import com.zaxxer.hikari.HikariDataSource;

public class Y9SqlUtil {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Y9SqlUtil.class);

    /**
     * 连接池缓存，使用数据库连接信息的哈希值作为key
     */
    private static final Map<String, HikariDataSource> DATA_SOURCE_CACHE = new ConcurrentHashMap<>();

    /**
     * 静态初始化块，注册JVM关闭钩子
     * 确保在应用程序关闭时，所有数据源连接池都被正确关闭
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutdown hook triggered, closing all DruidDataSources...");
            closeAllDataSources();
            logger.info("All DruidDataSources closed successfully");
        }, "Y9SqlUtil-ShutdownHook"));
    }

    /**
     * 生成连接池缓存的key
     */
    private static String generateDataSourceKey(String url, String username, String password, String driver) {
        // 使用连接信息的组合作为key，确保相同配置的连接池只创建一次
        return url + "_" + username + "_" + password + "_" + driver;
    }

    public static DataSource createDataSource(String url, String username, String password, String driver) {
        String key = generateDataSourceKey(url, username, password, driver);

        // 尝试从缓存中获取连接池
        HikariDataSource dataSource = DATA_SOURCE_CACHE.get(key);

        if (dataSource == null) {
            try {
                // 如果缓存中没有，则创建新的连接池
                dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(url);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName(driver);

                // 设置基本连接池参数
                dataSource.setMinimumIdle(5); // 最小连接数
                dataSource.setMaximumPoolSize(20); // 最大连接数  请求失败之后中断

                // 将新创建的连接池放入缓存
                HikariDataSource existingDataSource = DATA_SOURCE_CACHE.putIfAbsent(key, dataSource);
                if (existingDataSource != null) {
                    // 如果有其他线程已经创建了相同的连接池，则关闭当前创建的并使用已有的
                    try {
                        dataSource.close();
                    } catch (Exception e) {
                        logger.error("Failed to close redundant DruidDataSource", e);
                    }
                    dataSource = existingDataSource;
                    logger.debug("Reusing existing DruidDataSource for URL: {}", url);
                } else {
                    logger.info("Created new DruidDataSource for URL: {}", url);
                }
            } catch (Exception e) {
                logger.error("Failed to create DruidDataSource for URL: {}", url, e);
                throw new RuntimeException("Failed to create data source", e);
            }
        } else {
            logger.debug("Reusing existing DruidDataSource for URL: {}", url);
        }

        return dataSource;
    }

    /**
     * 执行SQL查询（非参数化，存在SQL注入风险）
     * 请尽量使用带参数的executeQuery方法
     *
     * @param sql SQL语句
     * @param url 数据库URL
     * @param username 用户名
     * @param password 密码
     * @param driver 驱动类名
     * @return 查询结果
     * @throws RuntimeException 如果执行查询时发生错误
     */
    public static List<Map<String, Object>> executeQuery(String sql, String url, String username, String password,
        String driver) {
        return executeDatabaseOperation("query", sql, false, () -> {
            DataSource dataSource = createDataSource(url, username, password, driver);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForList(sql);
        });
    }

    /**
     * 执行参数化SQL查询（推荐使用，防止SQL注入）
     *
     * @param sql SQL语句，使用?作为参数占位符
     * @param url 数据库URL
     * @param username 用户名
     * @param password 密码
     * @param driver 驱动类名
     * @param args SQL参数
     * @return 查询结果
     * @throws RuntimeException 如果执行查询时发生错误
     */
    public static List<Map<String, Object>> executeQuery(String sql, String url, String username, String password,
        String driver, @Nullable Object... args) {
        return executeDatabaseOperation("query", sql, true, () -> {
            DataSource dataSource = createDataSource(url, username, password, driver);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForList(sql, args);
        });
    }

    /**
     * 执行SQL更新（非参数化，存在SQL注入风险）
     * 请尽量使用带参数的executeUpdate方法
     *
     * @param sql SQL语句
     * @param url 数据库URL
     * @param username 用户名
     * @param password 密码
     * @param driver 驱动类名
     * @return 受影响的行数
     * @throws RuntimeException 如果执行更新时发生错误
     */
    public static int executeUpdate(String sql, String url, String username, String password, String driver) {
        return executeDatabaseOperation("update", sql, false, () -> {
            DataSource dataSource = createDataSource(url, username, password, driver);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.update(sql);
        });
    }

    /**
     * 执行参数化SQL更新（推荐使用，防止SQL注入）
     *
     * @param sql SQL语句，使用?作为参数占位符
     * @param url 数据库URL
     * @param username 用户名
     * @param password 密码
     * @param driver 驱动类名
     * @param args SQL参数
     * @return 受影响的行数
     * @throws RuntimeException 如果执行更新时发生错误
     */
    public static int executeUpdate(String sql, String url, String username, String password, String driver,
        @Nullable Object... args) {
        return executeDatabaseOperation("update", sql, true, () -> {
            DataSource dataSource = createDataSource(url, username, password, driver);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.update(sql, args);
        });
    }

    /**
     * 通用的数据库操作执行方法，处理重复的逻辑
     *
     * @param operationType 操作类型（query/update）
     * @param sql SQL语句
     * @param isParameterized 是否为参数化语句
     * @param operationSupplier 实际的数据库操作
     * @param <T> 返回值类型
     * @return 数据库操作结果
     * @throws RuntimeException 如果执行操作时发生错误
     */
    private static <T> T executeDatabaseOperation(String operationType, String sql, boolean isParameterized,
        Supplier<T> operationSupplier) {
        long startTime = System.currentTimeMillis();
        String operationDesc = (isParameterized ? "parameterized " : "non-parameterized ") + operationType;

        logger.debug("Executing {}: {}", operationDesc, sql);

        try {
            // 如果是非参数化语句，进行SQL注入检测
            if (!isParameterized) {
                checkSqlInjection(sql);
            }

            // 执行实际的数据库操作
            T result = operationSupplier.get();

            long endTime = System.currentTimeMillis();

            // 根据操作类型记录不同的日志信息
            if ("query".equals(operationType)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> queryResult = (List<Map<String, Object>>) result;
                logger.info("{} executed successfully in {} ms, returned {} rows",
                    operationDesc, (endTime - startTime), queryResult.size());
            } else if ("update".equals(operationType)) {
                int updateResult = (int) result;
                logger.info("{} executed successfully in {} ms, affected {} rows",
                    operationDesc, (endTime - startTime), updateResult);
            }

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Failed to execute {} in {} ms: {}",
                operationDesc, (endTime - startTime), sql, e);

            throw new RuntimeException("Failed to execute " + operationDesc, e);
        }
    }

    /**
     * SQL注入检测
     *
     * @param sql SQL语句
     * @throws SecurityException 如果检测到潜在的SQL注入风险
     */
    private static void checkSqlInjection(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }

        // 转换为小写以便不区分大小写检测
        String lowerSql = sql.toLowerCase().trim();

        // 危险的SQL操作关键词（需要空格分隔，避免误报）
        String[] dangerousOperations = {
            " union ", " insert ", " update ", " delete ", " drop ", " create ",
            " alter ", " truncate ", " exec ", " execute ", " xp_", " sp_"
        };

        // 危险的注释和分隔符（不需要空格）
        String[] dangerousDelimiters = {
            "--", "/*", "*/", ";"
        };

        // 危险的字符串分隔符（需要特殊处理）
        String[] dangerousQuotes = {
            "'", "\""
        };

        // 检查危险操作
        for (String keyword : dangerousOperations) {
            if (lowerSql.contains(keyword)) {
                logger.warn("Potential SQL injection detected with dangerous operation '{}' in SQL: {}", keyword.trim(), sql);
                // 对于非参数化查询，我们应该更严格地阻止危险操作
                if (isNonParameterizedOperation(sql)) {
                    throw new SecurityException("Potential SQL injection detected: dangerous operation '" + keyword.trim() + "' found");
                }
                break;
            }
        }

        // 检查危险分隔符
        for (String delimiter : dangerousDelimiters) {
            if (lowerSql.contains(delimiter)) {
                logger.warn("Potential SQL injection detected with dangerous delimiter '{}' in SQL: {}", delimiter, sql);
                break;
            }
        }

        // 检查危险的字符串分隔符（可能用于转义）
        for (String quote : dangerousQuotes) {
            int quoteCount = lowerSql.length() - lowerSql.replace(quote, "").length();
            // 如果存在奇数个引号，可能存在注入风险
            if (quoteCount % 2 != 0) {
                logger.warn("Potential SQL injection detected with unbalanced quotes '{}' in SQL: {}", quote, sql);
                break;
            }
        }
    }

    /**
     * 判断是否为非参数化操作
     *
     * @param sql SQL语句
     * @return 是否为非参数化操作
     */
    private static boolean isNonParameterizedOperation(String sql) {
        // 简单判断：如果SQL中包含?占位符，则认为是参数化操作
        return !sql.contains("?");
    }

    /**
     * 关闭所有缓存的连接池
     * 在应用程序关闭时调用
     */
    public static void closeAllDataSources() {
        logger.info("Closing all DruidDataSources, total: {}", DATA_SOURCE_CACHE.size());

        int closedCount = 0;
        int failedCount = 0;

        for (HikariDataSource dataSource : DATA_SOURCE_CACHE.values()) {
            if (dataSource != null && !dataSource.isClosed()) {
                try {
                    dataSource.close();
                    closedCount++;
                    logger.debug("Successfully closed DruidDataSource");
                } catch (Exception e) {
                    failedCount++;
                    logger.error("Failed to close DruidDataSource", e);
                }
            } else if (dataSource != null) {
                closedCount++;
                logger.debug("DruidDataSource is already closed");
            }
        }

        DATA_SOURCE_CACHE.clear();
        logger.info("Close operation completed. Closed: {}, Failed: {}, Total: {}",
            closedCount, failedCount, (closedCount + failedCount));
    }

}
