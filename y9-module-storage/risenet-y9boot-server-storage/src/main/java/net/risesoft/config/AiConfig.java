package net.risesoft.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * AI 模块 Spring 配置
 */
@Configuration
public class AiConfig {

    @Bean
    public RestTemplate aiRestTemplate(AiProperties aiProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofMillis(aiProperties.getConnectTimeout()).toMillis());
        factory.setReadTimeout((int) Duration.ofMillis(aiProperties.getReadTimeout()).toMillis());

        RestTemplate restTemplate = new RestTemplateBuilder()
            .requestFactory(() -> factory)
            .build();

        // 确保 UTF-8 编码
        restTemplate.getMessageConverters().set(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        return restTemplate;
    }
}
