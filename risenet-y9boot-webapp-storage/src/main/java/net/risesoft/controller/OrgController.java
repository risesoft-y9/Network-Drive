package net.risesoft.controller;

import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.api.platform.permission.PersonRoleApi;
import net.risesoft.enums.platform.OrgTreeTypeEnum;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Organization;
import net.risesoft.model.platform.Position;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import y9.client.rest.platform.org.OrgUnitApiClient;
import y9.client.rest.platform.org.OrganizationApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/vue/org")
public class OrgController {

    @Autowired
    private OrgUnitApiClient orgUnitManager;

    @Autowired
    private PositionApi positionApi;

    @Autowired
    private OrganizationApiClient organizationManager;

    @Autowired
    private PersonRoleApi personRoleApi;

    /**
     * 验证当前人的管理权限
     *
     * @return
     */
    @RequestMapping(value = "/checkManager", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> checkManager() {
        Map<String, Object> res_map = new HashMap<String, Object>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String publicManagerRoleName = Y9Context.getProperty("y9.app.storage.publicManagerRoleName");
        String capacityManagerRoleName = Y9Context.getProperty("y9.app.storage.capacityManagerRoleName");
        String reportManagerRoleName = Y9Context.getProperty("y9.app.storage.reportManagerRoleName");
        boolean publicManager = personRoleApi
            .hasRole(tenantId, Y9Context.getSystemName(), "", publicManagerRoleName, userInfo.getPersonId()).getData();
        boolean capacityManager = personRoleApi
            .hasRole(tenantId, Y9Context.getSystemName(), "", capacityManagerRoleName, userInfo.getPersonId())
            .getData();
        boolean reportManager = personRoleApi
            .hasRole(tenantId, Y9Context.getSystemName(), "", reportManagerRoleName, userInfo.getPersonId()).getData();
        res_map.put("publicManager", publicManager);
        res_map.put("capacityManager", capacityManager);
        res_map.put("reportManager", reportManager);
        return Y9Result.success(res_map);
    }

    /**
     * 获取租户的组织架构
     *
     * @return
     */
    @GetMapping(value = "/getOrganization")
    public Y9Result<List<Organization>> getOrganization() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Organization> organizationList = organizationManager.listAllOrganizations(tenantId).getData();
        return Y9Result.success(organizationList);
    }

    @GetMapping("/getTree")
    public Y9Result<List<OrgUnit>> getOrgTree(@RequestParam(required = false) String id,
        @RequestParam(required = false) String name) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        if (StringUtils.isBlank(id)) {
            List<Organization> organizationList = organizationManager.listAllOrganizations(tenantId).getData();
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

    /**
     * 获取个人所有岗位
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/getPositionList", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Y9Result<Map<String, Object>> getPositionList() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> res_map = new HashMap<String, Object>();
        List<Map<String, Object>> res_list = new ArrayList<Map<String, Object>>();
        List<Position> list = positionApi.listByPersonId(tenantId, Y9LoginUserHolder.getPersonId()).getData();
        for (Position p : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            res_list.add(map);
        }
        res_map.put("positionList", res_list);
        res_map.put("tenantId", tenantId);
        return Y9Result.success(res_map, "获取成功");
    }

}
