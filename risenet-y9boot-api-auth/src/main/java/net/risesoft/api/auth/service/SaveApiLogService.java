package net.risesoft.api.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.risesoft.id.Y9IdGenerator;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9public.entity.ApiServiceLogEntity;
import net.risesoft.y9public.repository.ApiServiceLogRepository;

@Service
public class SaveApiLogService {

    @Autowired
    private ApiServiceLogRepository apiServiceLogRepository;

    @Async("taskExecutor")
    public void asyncSave(String appName, String requestUrl, String hostIp, String requestParams, String result) {
        ApiServiceLogEntity apiServiceLogEntity = new ApiServiceLogEntity();
        apiServiceLogEntity.setId(Y9IdGenerator.genId());
        apiServiceLogEntity.setAppName(appName);
        apiServiceLogEntity.setHostIp(hostIp);
        apiServiceLogEntity.setRequestParams(requestParams);
        apiServiceLogEntity.setRequestUrl(requestUrl);
        apiServiceLogEntity.setResult(result);
        apiServiceLogEntity.setServerIp(Y9Context.getHostIp());
        apiServiceLogRepository.save(apiServiceLogEntity);
    }

}
