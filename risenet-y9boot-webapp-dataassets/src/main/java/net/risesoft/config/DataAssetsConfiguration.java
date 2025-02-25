package net.risesoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import net.risesoft.converter.EncryptConverter;

@Configuration
public class DataAssetsConfiguration {

    /*
     * @Bean public FilterRegistrationBean<Y9SkipSSOFilter> y9Oauth2ResourceFilter() { final FilterRegistrationBean<Y9SkipSSOFilter> filterBean = new FilterRegistrationBean<>(); filterBean.setFilter(new Y9SkipSSOFilter()); filterBean.setAsyncSupported(false);
     * filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE); filterBean.addUrlPatterns("/*"); return filterBean; }
     */
	
	@Bean
	@DependsOn("y9Context")
	public EncryptConverter encryptConverter() {
		return new EncryptConverter();
	}
}
