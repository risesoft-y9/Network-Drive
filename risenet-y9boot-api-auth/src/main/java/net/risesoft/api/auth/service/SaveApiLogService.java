package net.risesoft.api.auth.service;

import org.springframework.stereotype.Service;

import net.risesoft.id.Y9IdGenerator;
import net.risesoft.y9.util.InetAddressUtil;
import net.risesoft.y9public.entity.ApiServiceLogEntity;
import net.risesoft.y9public.repository.ApiServiceLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

@Service
public class SaveApiLogService {
	
	private static final Logger log = LoggerFactory.getLogger(SaveApiLogService.class);
	
	private String serverIp = "";
	
	public SaveApiLogService() {
		this.serverIp = InetAddressUtil.getLocalAddress().getHostAddress();
		log.debug("SaveApiLogService init......");
	}
	
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
		apiServiceLogEntity.setServerIp(serverIp);
		apiServiceLogRepository.save(apiServiceLogEntity);
	}

}
