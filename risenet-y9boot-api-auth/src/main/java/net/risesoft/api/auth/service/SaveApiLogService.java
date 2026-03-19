package net.risesoft.api.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.risesoft.id.Y9IdGenerator;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9public.entity.ApiServiceEntity;
import net.risesoft.y9public.entity.ApiServiceLogEntity;
import net.risesoft.y9public.repository.ApiServiceLogRepository;
import net.risesoft.y9public.repository.ApiServiceRepository;

@Service
public class SaveApiLogService {

    @Autowired
    private ApiServiceLogRepository apiServiceLogRepository;

    @Autowired
    private ApiServiceRepository apiServiceRepository;

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
        // 根据请求地址判断接口类型
        String url = Y9Context.getProperty("y9.common.dataAssetsBaseUrl");
        if(requestUrl.contains(url + "/services/rest/get/") || requestUrl.contains(url + "/services/rest/post/")
            || requestUrl.contains(url + "/services/rest/getPage/")) {
            apiServiceLogEntity.setApiType("内部接口");
            // 获取接口名称，获取请求地址“/“分割后的最后一个元素
            String apiServiceId = requestUrl.split("/")[requestUrl.split("/").length - 1];
            ApiServiceEntity apiServiceEntity = apiServiceRepository.findById(apiServiceId).orElse(null);
            if(apiServiceEntity != null){
                apiServiceLogEntity.setRemark(apiServiceEntity.getApiName());
            } else {
                apiServiceLogEntity.setRemark("未找到接口名称");
            }
        }else if(requestUrl.contains(url + "/services/rest/search")){
            apiServiceLogEntity.setApiType("订阅生成接口");
            // 获取接口名称，获取请求地址“/“分割后的最后一个元素
            String name = requestUrl.split("/")[requestUrl.split("/").length - 1];
            apiServiceLogEntity.setRemark("订阅表：" + name);
        }else{
            apiServiceLogEntity.setApiType("其它接口");
            apiServiceLogEntity.setRemark("未找到接口名称");
        }
        apiServiceLogRepository.save(apiServiceLogEntity);
    }

}
