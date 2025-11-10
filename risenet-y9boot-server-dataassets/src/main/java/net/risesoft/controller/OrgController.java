package net.risesoft.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.dto.platform.CreatePersonDTO;
import net.risesoft.enums.platform.org.OrgTreeTypeEnum;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.platform.org.OrgUnit;
import net.risesoft.model.platform.org.Organization;
import net.risesoft.model.platform.org.Person;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

import y9.client.rest.platform.org.OrgUnitApiClient;
import y9.client.rest.platform.org.OrganizationApiClient;

/**
 * 组织架构、权限接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/vue/org")
public class OrgController {

    private final OrgUnitApiClient orgUnitManager;
    private final OrganizationApiClient organizationManager;
    private final PersonApi personApi;

    /**
     * 获取当前租户的组织架构
     *
     * @return
     */
    @GetMapping(value = "/getOrganization")
    public Y9Result<List<Organization>> getOrganization() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Organization> organizationList = organizationManager.list(tenantId).getData();
        return Y9Result.success(organizationList);
    }

    /**
     * 根据id或name获取组织架构树
     * 
     * @param id
     * @param name
     * @return
     */
    @GetMapping("/getTree")
    public Y9Result<List<OrgUnit>> getOrgTree(@RequestParam(required = false) String id,
        @RequestParam(required = false) String name) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        if (StringUtils.isBlank(id)) {
            List<Organization> organizationList = organizationManager.list(tenantId).getData();
            if (organizationList != null && organizationList.size() > 0) {
                id = organizationList.get(0).getId();
            }
        }
        List<OrgUnit> orgUnitList;
        if (StringUtils.isNotBlank(name)) {
            orgUnitList = orgUnitManager.treeSearch(tenantId, name, OrgTreeTypeEnum.TREE_TYPE_PERSON).getData();
        } else {
            orgUnitList = orgUnitManager.getSubTree(tenantId, id, OrgTreeTypeEnum.TREE_TYPE_PERSON).getData();
        }
        return Y9Result.success(orgUnitList);
    }

    @RiseLog(operationType = OperationTypeEnum.CHECK, logLevel = LogLevelEnum.RSLOG, operationName = "校验密码")
    @GetMapping("/checkPassword")
    public Y9Result<Boolean> checkPassword(@RequestParam String pwd) {
        // 只需要在下方修改的接口中传入旧密码，修改之前会先校验
        return Y9Result.success(true, "校验密码操作成功");
    }

    @RiseLog(logLevel = LogLevelEnum.RSLOG, operationType = OperationTypeEnum.MODIFY, operationName = "重置密码")
    @PostMapping(value = "/modifyPwd")
    public Y9Result<String> modifyPassword(@RequestParam String oldpwd, @RequestParam String newpwd) {
        personApi.modifyPassword(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId(), oldpwd, newpwd)
            .getData();
        return Y9Result.successMsg("重置密码成功！");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, logLevel = LogLevelEnum.RSLOG, operationName = "获取人员信息")
    @GetMapping("/getPersonInfo")
    public Y9Result<Map<String, Object>> getPersonInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        Person person = personApi.get(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId()).getData();
        map.put("cname", person.getDescription());
        map.put("email", person.getEmail());
        return Y9Result.success(map);
    }

    @RiseLog(logLevel = LogLevelEnum.RSLOG, operationType = OperationTypeEnum.MODIFY, operationName = "保存人员信息")
    @PostMapping(value = "/savePerson")
    public Y9Result<String> savePerson(@RequestParam String cname, @RequestParam String email) {
        Person person = personApi.get(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId()).getData();
        if (person != null) {
            CreatePersonDTO personDTO = new CreatePersonDTO();
            Y9BeanUtil.copyProperties(person, personDTO);
            personDTO.setEmail(email);
            personDTO.setDescription(cname);
            personApi.savePersonWithExt(Y9LoginUserHolder.getTenantId(), personDTO);
        } else {
            return Y9Result.failure("保存失败：人员信息不存在");
        }
        return Y9Result.successMsg("保存成功");
    }

}
