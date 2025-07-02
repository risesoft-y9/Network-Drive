package net.risesoft.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.org.dto.CreatePersonDTO;
import net.risesoft.pojo.Y9Result;
import y9.client.rest.platform.org.PersonApiClient;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/service/register")
public class LoginController {

    private final PersonApiClient personApiClient;
    
    @Value("${y9.app.dataAssets.orgId}")
    private String orgId;
    
    /**
     * 注册账号
     * @return
     */
    @PostMapping(value = "/savePerson")
	public Y9Result<List<String>> savePerson(@RequestHeader("auth-tenantId") String tenantId, @RequestParam String name, 
			@RequestParam String idCard, @RequestParam String phone, @RequestParam String password) {
    	CreatePersonDTO personDTO = new CreatePersonDTO();
    	personDTO.setIdNum(idCard);
    	personDTO.setIdType("10");
    	personDTO.setLoginName(name);
    	personDTO.setMobile(phone);
    	personDTO.setName(name);
    	personDTO.setPassword(password);
    	personDTO.setParentId(orgId);
		personApiClient.savePersonWithExt(tenantId, personDTO);
		return Y9Result.successMsg("注册成功");
	}

}
