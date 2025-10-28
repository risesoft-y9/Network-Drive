package y9.autoconfiguration.api;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.ttl.threadpool.TtlExecutors;

import net.risesoft.y9.Y9Context;

@Configuration
@EnableAsync
public class ApiAdminConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApiAdminConfiguration.class);

    @Bean(name = {"taskExecutor"})
    @ConditionalOnMissingBean(name = "taskExecutor")
    public Executor taskExecutor(TaskExecutorBuilder builder) {
        log.debug("Api threadPoolExecutor init ......");
        ThreadPoolTaskExecutor taskExecutor = builder.build();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);// 核心线程数
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setMaxPoolSize(5);// 最大线程数
        taskExecutor.setQueueCapacity(50);// 配置队列大小
        taskExecutor.initialize();
        return TtlExecutors.getTtlExecutor(taskExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public Y9Context y9Context() {
        return new Y9Context();
    }

}
