package net.risesoft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.spring.boot.Y9Banner;

@SpringBootApplication
@EnableConfigurationProperties(Y9Properties.class)
public class DataAssetsApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(DataAssetsApplication.class);
        springApplication.setBanner(new Y9Banner());
        springApplication.run(args);
    }
}
