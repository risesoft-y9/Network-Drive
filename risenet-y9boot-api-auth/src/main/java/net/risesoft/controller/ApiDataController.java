package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.pojo.ApiServiceModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ApiDataService;
import net.risesoft.y9public.entity.ApiDataCatalogEntity;
import net.risesoft.y9public.entity.ApiRoleEntity;
import net.risesoft.y9public.entity.ApiServiceEntity;
import net.risesoft.y9public.entity.ApiServiceLogEntity;

@Validated
@RestController
@RequestMapping(value = "/vue/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiDataController {

    private final ApiDataService apiDataService;

    @GetMapping("/getTree")
    public Y9Result<List<Map<String, Object>>> getTree(String parentId, String name) {
        return Y9Result.success(apiDataService.getTree(parentId, name), "获取数据成功");
    }

    @GetMapping(value = "/{id}")
    public Y9Result<ApiDataCatalogEntity> getData(@PathVariable @NotBlank String id) {
        return Y9Result.success(apiDataService.getById(id), "获取数据成功");
    }

    @PostMapping(value = "/saveData")
    public Y9Result<ApiDataCatalogEntity> saveData(ApiDataCatalogEntity entity) {
        return apiDataService.saveData(entity);
    }

    @PostMapping(value = "/deleteData")
    public Y9Result<String> deleteData(@RequestParam String id) {
        return apiDataService.deleteData(id);
    }

    @GetMapping("/searchPage")
    public Y9Page<ApiServiceEntity> searchPage(String parentId, String apiName, Integer page, Integer size) {
        Page<ApiServiceEntity> pageList = apiDataService.searchPage(parentId, apiName, page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @PostMapping(value = "/saveApiData")
    public Y9Result<String> saveApiData(ApiServiceModel apiServiceModel) {
        return apiDataService.saveApiData(apiServiceModel);
    }

    @PostMapping(value = "/deleteApiData")
    public Y9Result<String> deleteApiData(@RequestParam String id) {
        return apiDataService.deleteApiData(id);
    }

    @GetMapping("/getApiInfo")
    public Y9Result<Map<String, Object>> getApiInfo(String id) {
        return apiDataService.getApiInfo(id);
    }

    @GetMapping("/searchRolePage")
    public Y9Page<ApiRoleEntity> searchRolePage(String appName, Integer page, Integer size) {
        Page<ApiRoleEntity> pageList = apiDataService.searchRolePage(appName, page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @PostMapping(value = "/saveApiRoleData")
    public Y9Result<String> saveApiRoleData(ApiRoleEntity apiRoleEntity) {
        return apiDataService.saveApiRoleData(apiRoleEntity);
    }

    @PostMapping(value = "/deleteApiRoleData")
    public Y9Result<String> deleteApiRoleData(@RequestParam String id) {
        return apiDataService.deleteApiRoleData(id);
    }

    @GetMapping("/searchLogPage")
    public Y9Page<ApiServiceLogEntity> searchLogPage(String appName, Integer page, Integer size) {
        Page<ApiServiceLogEntity> pageList = apiDataService.searchLogPage(appName, page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @GetMapping(value = "/getApiTreeList")
    public Y9Result<List<Map<String, Object>>> getApiTreeList() {
        return apiDataService.getApiTreeList();
    }

    @PostMapping(value = "/saveApiRole")
    public Y9Result<String> saveApiRole(String ids, String appName) {
        return apiDataService.saveApiRole(ids, appName);
    }

    @GetMapping(value = "/getApiRole")
    public Y9Result<List<String>> getApiRole(String appName) {
        return apiDataService.getApiRole(appName);
    }

    @GetMapping(value = "/getAllUsers")
    public Y9Result<List<String>> getAllUsers() {
        return Y9Result.success(apiDataService.getAllUsers());
    }

    @GetMapping(value = "/getApiRoleById")
    public Y9Result<ApiRoleEntity> getApiRoleById(String id) {
        return Y9Result.success(apiDataService.findApiRoleById(id));
    }
}
