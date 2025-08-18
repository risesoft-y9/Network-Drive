package net.risesoft.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.enums.platform.permission.AuthorityEnum;
import net.risesoft.model.platform.resource.DataCatalog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.permission.cache.PersonRoleApiClient;
import y9.client.rest.platform.resource.DataCatalogApiClient;

/**
 * 资产目录管理接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/vue/catalog")
public class DataAssetsCatalogController {

    private final DataCatalogApiClient dataCatalogApiClient;
    private final PersonRoleApiClient personRoleApiClient;

    /**
     * 获取数据底座-数据资产的数据目录
     * 
     * @param parentId
     * @return
     */
    @GetMapping("/getCatelogTree")
    public Y9Result<List<DataCatalog>> getCatelogTree(@RequestParam(required = false) String parentId) {
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = Y9LoginUserHolder.getUserInfo().getPersonId();
        return dataCatalogApiClient.getTree("asset", parentId, false, tenantId, userId, AuthorityEnum.BROWSE);
    }

    /**
     * 搜索树
     * 
     * @param name
     * @return
     */
    @GetMapping("/searchCatelogTree")
    public Y9Result<List<DataCatalog>> searchCatelogTree(@RequestParam(required = false) String name) {
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = Y9LoginUserHolder.getUserInfo().getPersonId();
        return dataCatalogApiClient.treeSearch("asset", name, tenantId, userId, AuthorityEnum.BROWSE);
    }

    /**
     * 判断是否系统管理员
     * 
     * @param parentId
     * @return
     */
    @GetMapping("/hasRole")
    public Y9Result<Boolean> hasRole() {
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = Y9LoginUserHolder.getUserInfo().getPersonId();
        return personRoleApiClient.hasRole(tenantId, "dataAssets", null, "系统管理员", userId);
    }

}