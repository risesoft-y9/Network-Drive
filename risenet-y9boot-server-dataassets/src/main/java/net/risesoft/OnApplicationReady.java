package net.risesoft;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OnApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    @Value("${y9.app.dataAssets.tenantId}")
    private String tenantId;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("DataAssets service ApplicationReady...");
        //Y9LoginUserHolder.setTenantId(tenantId);
    }

    @PostConstruct
    public void init() {
        IdCodeConfig.init(environment.getProperty("idCode.api_code"), environment.getProperty("idCode.api_key"),
            environment.getProperty("idCode.idCode_url"), environment.getProperty("idCode.main_code"),
            environment.getProperty("idCode.analyze_url"), environment.getProperty("idCode.goto_url"),
            environment.getProperty("idCode.sample_url"));
    }

}
