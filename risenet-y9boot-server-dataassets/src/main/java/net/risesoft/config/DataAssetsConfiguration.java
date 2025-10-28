package net.risesoft.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.risesoft.api.auth.handler.ApiAuthInterceptor;
import net.risesoft.converter.EncryptConverter;

@Configuration
public class DataAssetsConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApiAuthInterceptor apiAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiAuthInterceptor).addPathPatterns("/services/rest/**");
    }

    @Bean
    @DependsOn("y9Context")
    public EncryptConverter encryptConverter() {
        return new EncryptConverter();
    }
}
