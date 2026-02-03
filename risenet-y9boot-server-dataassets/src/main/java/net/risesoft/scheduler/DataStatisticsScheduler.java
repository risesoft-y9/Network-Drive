package net.risesoft.scheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.risesoft.consts.SqlConstants;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataStatisticsEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.tenant.Tenant;
import net.risesoft.repository.DataSourceRepository;
import net.risesoft.repository.DataStatisticsRepository;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.sqlddl.DbMetaDataUtil;
import y9.client.rest.platform.tenant.TenantSystemApiClient;

/**
 * 数据统计定时任务
 * 每天晚上11点50分执行，统计每个数据库的所有业务表的数据总量
 */
@Component
public class DataStatisticsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataStatisticsScheduler.class);

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataStatisticsRepository dataStatisticsRepository;

    @Autowired
    private TenantSystemApiClient tenantSystemApiClient;

    /**
     * 每天晚上11点50分执行
     * cron表达式：秒 分 时 日 月 周 年
     * 50 23 * * * ? 表示每天23点50分执行
     */
    @Scheduled(cron = "0 50 23 * * ?")
    public void statisticsDataVolume() {
        logger.info("开始执行数据量统计定时任务...");
        
        try {
            // 获取所有租户
            List<Tenant> tenantList = tenantSystemApiClient.listTenantBySystemName(Y9Context.getSystemName()).getData();
            if (tenantList != null && !tenantList.isEmpty()) {
                for (Tenant tenant : tenantList) {
                    Y9LoginUserHolder.setTenantId(tenant.getId());
                    logger.info("开始处理租户 {} 的数据量统计", tenant.getName());
                    // 获取所有数据库类型的数据源
                    List<DataSourceEntity> dataSources = dataSourceRepository.findByTenantIdAndType(tenant.getId(), 0);
                    logger.info("共找到 {} 个数据库类型的数据源", dataSources.size());
                    if (dataSources != null && !dataSources.isEmpty()) {
                        for (DataSourceEntity dataSource : dataSources) {
                            logger.info("开始统计数据源：{} 的数据量", dataSource.getName());
                            statisticsDataSource(dataSource, true);
                        }
                    }
                }
            }
            logger.info("数据量统计定时任务执行完成");
        } catch (Exception e) {
            logger.error("数据量统计定时任务执行失败", e);
        }
    }

    /**
     * 统计单个数据源的数据量
     * @param dataSource 数据源
     * @return 数据量
     */
    public long statisticsData(DataSourceEntity dataSource) {
        return statisticsDataSource(dataSource, false);
    }

    /**
     * 统计单个数据源的数据量
     * @param dataSource 数据源
     */
    private long statisticsDataSource(DataSourceEntity dataSource, Boolean isSave) {
        Connection connection = null;
        Statement statement = null;
        ResultSet tables = null;
        long totalCount = 0;
        try {
            // 加载驱动
            Class.forName(dataSource.getDriver());
            
            // 建立连接
            connection = DriverManager.getConnection(
                dataSource.getUrl(),
                dataSource.getUsername(),
                dataSource.getPassword()
            );

            statement = connection.createStatement();
            
            // 获取所有表
            String sql = "";
            String dialect = DbMetaDataUtil.getDatabaseDialectNameByConnection(connection);
            switch (dialect) {
                case SqlConstants.DBTYPE_ORACLE:
                    sql = "SELECT table_name FROM all_tab_comments";
                    break;
                case SqlConstants.DBTYPE_DM:
                    sql = "SELECT table_name FROM all_tab_comments";
                    break;
                case SqlConstants.DBTYPE_KINGBASE:
                    sql = "SELECT table_name FROM all_tab_comments";
                    break;
                case SqlConstants.DBTYPE_MYSQL:
                    sql = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = DATABASE()";
                    break;
                default:
                    sql = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = DATABASE()";
                    break;
            }
            tables = statement.executeQuery(sql);
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                // 跳过系统表
                if (isSystemTable(tableName)) {
                    continue;
                }
                
                // 统计表数据量
                long count = countTableRows(connection, tableName, dataSource.getBaseSchema());
                totalCount += count;
            }
            
            // 保存统计结果
            if (isSave) {
                saveStatistics(dataSource.getId(), totalCount);
            }
            
            logger.info("数据源 {} 的总数据量：{}", dataSource.getName(), totalCount);
        } catch (Exception e) {
            logger.error("统计数据源 {} 失败", dataSource.getName(), e);
        } finally {
            // 关闭资源
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                if (tables != null) {
                    tables.close();
                }
            } catch (Exception e) {
                logger.error("关闭数据库连接失败", e);
            }
        }
        return totalCount;
    }

    /**
     * 统计表的行数
     * @param connection 数据库连接
     * @param tableName 表名
     * @param schema 模式
     * @return 行数
     * @throws Exception 异常
     */
    private long countTableRows(Connection connection, String tableName, String schema) throws Exception {
        Statement statement = connection.createStatement();
        String sql = "SELECT COUNT(*) FROM ";
        
        if (schema != null && !schema.isEmpty()) {
            sql += schema + ".";
        }
        sql += tableName;
        
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.next();
        long count = resultSet.getLong(1);
        
        resultSet.close();
        statement.close();
        
        return count;
    }

    /**
     * 保存统计结果
     * @param dataSource 数据源
     * @param count 数据量
     */
    private void saveStatistics(String dataSourceId, long count) {
        DataStatisticsEntity statistics = new DataStatisticsEntity();
        statistics.setId(Y9IdGenerator.genId());
        // 设置数据时间为当前时间
        statistics.setDataTime(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // 设置数据库ID
        statistics.setSourceId(dataSourceId);
        // 设置数据量
        statistics.setDataCount((int)count);
        dataStatisticsRepository.save(statistics);
    }

    /**
     * 判断是否为系统表
     * @param tableName 表名
     * @return 是否为系统表
     */
    private boolean isSystemTable(String tableName) {
        // 常见系统表前缀
        String[] systemPrefixes = {"SYS_", "V$_", "DBA_", "ALL_", "USER_", "INFORMATION_SCHEMA.", "mysql.", "performance_schema."};
        
        for (String prefix : systemPrefixes) {
            if (tableName.toUpperCase().startsWith(prefix)) {
                return true;
            }
        }
        
        return false;
    }
}
