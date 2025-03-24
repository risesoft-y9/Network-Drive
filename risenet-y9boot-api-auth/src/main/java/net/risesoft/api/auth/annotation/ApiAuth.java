package net.risesoft.api.auth.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Api接口权限注解
 * @author pzx
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
public @interface ApiAuth {
	
	String[] roles() default {};
	
	double permitsPerSecond() default 1.0;
	
	String value() default "";
	
    String path() default "";
    
    String method() default "";  

}
