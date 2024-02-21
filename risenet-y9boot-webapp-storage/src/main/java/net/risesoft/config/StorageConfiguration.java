package net.risesoft.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StorageConfiguration.class);

    // starter-log工程用到了RequestContextHolder
    // https://github.com/spring-projects/spring-boot/issues/2637
    // https://github.com/spring-projects/spring-boot/issues/4331
    @Bean
    public static RequestContextFilter requestContextFilter() {
        return new OrderedRequestContextFilter();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {}

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
        supportedMediaTypes.add(MediaType.parseMediaType("text/html;charset=UTF-8"));
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        return converter;
    }

    // @Override
    // public void addCorsMappings(CorsRegistry registry){
    // registry.addMapping("/**")
    // .allowedOrigins("*")
    // .allowCredentials(true)
    // .allowedMethods("GET","POST","DELETE","PUT","OPTIONS")
    // .maxAge(3600);
    // }

    /*
     * @Bean public FilterRegistrationBean<Y9SkipSSOFilter> y9Oauth2ResourceFilter() { final FilterRegistrationBean<Y9SkipSSOFilter> filterBean = new FilterRegistrationBean<>(); filterBean.setFilter(new Y9SkipSSOFilter()); filterBean.setAsyncSupported(false);
     * filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE); filterBean.addUrlPatterns("/*"); return filterBean; }
     */
}
