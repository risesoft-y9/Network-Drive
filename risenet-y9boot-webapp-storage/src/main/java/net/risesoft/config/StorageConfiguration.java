package net.risesoft.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class StorageConfiguration {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
        supportedMediaTypes.add(MediaType.parseMediaType("text/html;charset=UTF-8"));
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        return converter;
    }

    /*
     * @Bean public FilterRegistrationBean<Y9SkipSSOFilter> y9Oauth2ResourceFilter() { final FilterRegistrationBean<Y9SkipSSOFilter> filterBean = new FilterRegistrationBean<>(); filterBean.setFilter(new Y9SkipSSOFilter()); filterBean.setAsyncSupported(false);
     * filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE); filterBean.addUrlPatterns("/*"); return filterBean; }
     */
}
