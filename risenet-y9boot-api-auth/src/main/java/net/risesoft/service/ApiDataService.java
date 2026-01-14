package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import net.risesoft.pojo.ApiServiceModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9public.entity.ApiDataCatalogEntity;
import net.risesoft.y9public.entity.ApiRoleEntity;
import net.risesoft.y9public.entity.ApiServiceEntity;
import net.risesoft.y9public.entity.ApiServiceLogEntity;

public interface ApiDataService {

    /**
     * 查询目录列表
     */
    List<Map<String, Object>> getTree(String parentId, String searchName);

    /**
     * 根据ID获取目录
     * 
     * @return
     */
    ApiDataCatalogEntity getById(String id);

    /**
     * 根据id删除目录
     * 
     */
    Y9Result<String> deleteData(String id);

    /**
     * 保存目录
     * 
     * @param entity
     * @return
     */
    Y9Result<ApiDataCatalogEntity> saveData(ApiDataCatalogEntity entity);

    /**
     * 保存API信息
     * 
     * @param apiServiceEntity
     * @return
     */
    Y9Result<String> saveApiData(ApiServiceModel apiServiceModel);

    /**
     * 删除API
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteApiData(String id);

    /**
     * 分页获取api列表
     * 
     * @param parentId
     * @param apiName
     * @param page
     * @param size
     * @return
     */
    Page<ApiServiceEntity> searchPage(String parentId, String apiName, int page, int size);

    /**
     * 根据ID获取API信息
     * 
     * @param id
     * @return
     */
    ApiServiceEntity findById(String id);

    /**
     * 获取API详情
     * 
     * @param id
     * @return
     */
    Y9Result<Map<String, Object>> getApiInfo(String id);

    /**
     * 获取权限列表
     * 
     * @param appName
     * @param page
     * @param size
     * @return
     */
    Page<ApiRoleEntity> searchRolePage(String appName, int page, int size);

    /**
     * 保存API权限信息
     * 
     * @param ApiRoleEntity
     * @return
     */
    Y9Result<String> saveApiRoleData(ApiRoleEntity apiRoleEntity);

    /**
     * 删除API权限
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteApiRoleData(String id);

    /**
     * 获取接口日志列表
     * 
     * @param appName
     * @param page
     * @param size
     * @return
     */
    Page<ApiServiceLogEntity> searchLogPage(String appName, int page, int size);

    /**
     * 获取API树
     * 
     * @return
     */
    Y9Result<List<Map<String, Object>>> getApiTreeList();

    /**
     * 获取已绑定权限
     * 
     * @param id
     * @return
     */
    Y9Result<List<String>> getApiRole(String appName);

    /**
     * 保存权限
     * 
     * @param ids
     * @param appName
     * @return
     */
    Y9Result<String> saveApiRole(String ids, String appName);

    /**
     * 获取所有用户
     * 
     * @return
     */
    List<String> getAllUsers();

    /**
     * 根据ID获取权限信息
     * 
     * @param id
     * @return
     */
    ApiRoleEntity findApiRoleById(String id);

    /**
     * 根据日志获取每天的接口调用次数
     *
     * @return
     */
    List<Map<String, Object>> getDailyApiCallCount();

}
