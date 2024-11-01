package net.risesoft.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.resource.DataCatalogApiClient;

/**
 * 档案目录管理、权限接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/vue/catalog")
public class ArchivesCatalogController {

    private final DataCatalogApiClient dataCatalogApiClient;

    /**
     * 根据id或name获取组织架构树
     * 
     * @param id
     * @return
     */
    @GetMapping("/getCatelogTree")
    public Y9Result<List<DataCatalog>> getCatelogTree(@RequestParam(required = false) String id) {
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = Y9LoginUserHolder.getUserInfo().getPersonId();
        return dataCatalogApiClient.getTree("normal", id, false, tenantId, userId, AuthorityEnum.BROWSE);
    }

}
