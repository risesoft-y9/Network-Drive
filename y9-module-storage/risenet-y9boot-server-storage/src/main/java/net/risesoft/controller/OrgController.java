package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.org.OrganizationApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.api.platform.permission.cache.PersonRoleApi;
import net.risesoft.enums.platform.org.OrgTreeTypeEnum;
import net.risesoft.model.platform.org.OrgUnit;
import net.risesoft.model.platform.org.Organization;
import net.risesoft.model.platform.org.Position;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 组织架构、权限接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/vue/org")
public class OrgController {

    private final OrgUnitApi orgUnitApi;
    private final PositionApi positionApi;
    private final OrganizationApi organizationApi;
    private final PersonRoleApi personRoleApi;

    /**
     * 验证当前人的管理权限
     *
     * @return
     */
    @GetMapping(value = "/checkManager", produces = "application/json")
    public Y9Result<Map<String, Object>> checkManager() {
        Map<String, Object> resMap = new HashMap<>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId();
        String systemName = Y9Context.getSystemName();
        String personId = userInfo.getPersonId();
        String publicManagerRoleName = Y9Context.getProperty("y9.app.storage.publicManagerRoleName");
        String capacityManagerRoleName = Y9Context.getProperty("y9.app.storage.capacityManagerRoleName");
        String reportManagerRoleName = Y9Context.getProperty("y9.app.storage.reportManagerRoleName");
        String tagManagerRoleName = Y9Context.getProperty("y9.app.storage.tagManagerRoleName");
        boolean publicManager =
            personRoleApi.hasRole(tenantId, systemName, "", publicManagerRoleName, personId).getData();
        boolean capacityManager =
            personRoleApi.hasRole(tenantId, systemName, "", capacityManagerRoleName, personId).getData();
        boolean reportManager =
            personRoleApi.hasRole(tenantId, systemName, "", reportManagerRoleName, personId).getData();
        boolean tagManager = personRoleApi.hasRole(tenantId, systemName, "", tagManagerRoleName, personId).getData();
        resMap.put("publicManager", publicManager);
        resMap.put("capacityManager", capacityManager);
        resMap.put("reportManager", reportManager);
        resMap.put("tagManager", tagManager);
        return Y9Result.success(resMap);
    }

    /**
     * 获取当前租户的组织架构
     *
     * @return
     */
    @GetMapping(value = "/getOrganization")
    public Y9Result<List<Organization>> getOrganization() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Organization> organizationList = organizationApi.list(tenantId).getData();
        return Y9Result.success(organizationList != null ? organizationList : new ArrayList<>());
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
            List<Organization> organizationList = organizationApi.list(tenantId).getData();
            if (organizationList != null && !organizationList.isEmpty()) {
                id = organizationList.get(0).getId();
            }
        }
        if (StringUtils.isBlank(id)) {
            return Y9Result.success(new ArrayList<>());
        }
        List<OrgUnit> orgUnitList;
        if (StringUtils.isNotBlank(name)) {
            orgUnitList = orgUnitApi.treeSearch(tenantId, id, name, OrgTreeTypeEnum.TREE_TYPE_PERSON).getData();
        } else {
            orgUnitList = orgUnitApi.getSubTree(tenantId, id, OrgTreeTypeEnum.TREE_TYPE_PERSON).getData();
        }
        return Y9Result.success(orgUnitList != null ? orgUnitList : new ArrayList<>());
    }

    /**
     * 获取个人所有岗位
     *
     * @return
     */
    @GetMapping(value = "/getPositionList", produces = "application/json")
    public Y9Result<Map<String, Object>> getPositionList() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> resList = new ArrayList<>();
        List<Position> list = positionApi.listByPersonId(tenantId, Y9LoginUserHolder.getPersonId()).getData();
        if (list != null) {
            for (Position p : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                resList.add(map);
            }
        }
        resMap.put("positionList", resList);
        resMap.put("tenantId", tenantId);
        return Y9Result.success(resMap, "获取成功");
    }

}
