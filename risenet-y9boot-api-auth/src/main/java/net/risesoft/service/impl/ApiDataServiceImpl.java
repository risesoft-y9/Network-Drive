package net.risesoft.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.auth.util.Y9CipherUtil;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.ApiServiceModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ApiDataService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9public.entity.ApiDataCatalogEntity;
import net.risesoft.y9public.entity.ApiRoleEntity;
import net.risesoft.y9public.entity.ApiServiceEntity;
import net.risesoft.y9public.entity.ApiServiceLogEntity;
import net.risesoft.y9public.repository.ApiDataCatalogRepository;
import net.risesoft.y9public.repository.ApiRoleRepository;
import net.risesoft.y9public.repository.ApiServiceLogRepository;
import net.risesoft.y9public.repository.ApiServiceRepository;

@Service(value = "apiDataService")
@RequiredArgsConstructor
public class ApiDataServiceImpl implements ApiDataService {
	
	private final ApiDataCatalogRepository apiDataCatalogRepository;
	private final ApiServiceRepository apiServiceRepository;
	private final ApiRoleRepository apiRoleRepository;
	private final ApiServiceLogRepository apiServiceLogRepository;
	
	@Override
	public ApiDataCatalogEntity getById(String id) {
		return apiDataCatalogRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteData(String id) {
		// 检验目录和子目录下是否存在文件
		String msg = "";
		ApiDataCatalogEntity catalogEntity = getById(id);
		if(catalogEntity != null) {
			if(apiServiceRepository.countByParentId(id) > 0) {
				msg = "目录[" + catalogEntity.getName() + "]下存在API，无法删除";
			}else {
				msg = checkData(id, msg);
			}
		}
		if(StringUtils.isNotBlank(msg)) {
			return Y9Result.failure(msg);
		}
		apiDataCatalogRepository.deleteById(id);
		deleteChild(id);
		return Y9Result.successMsg("删除成功");
	}
	
	private void deleteChild(String parentId) {
		List<ApiDataCatalogEntity> list = apiDataCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(ApiDataCatalogEntity catalog : list) {
			apiDataCatalogRepository.delete(catalog);
			deleteChild(catalog.getId());
		}
	}
	
	private String checkData(String parentId, String msg) {
		// 查找子节点
		List<ApiDataCatalogEntity> list = apiDataCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(ApiDataCatalogEntity catalog : list) {
			if(apiServiceRepository.countByParentId(catalog.getId()) > 0) {
				return "目录[" + catalog.getName() + "]下存在API，无法删除";
			}else {
				checkData(catalog.getId(), msg);
			}
		}
		return "";
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<ApiDataCatalogEntity> saveData(ApiDataCatalogEntity entity) {
		try {
			ApiDataCatalogEntity info = apiDataCatalogRepository.findByNameAndParentId(entity.getName(), entity.getParentId());
			if(info != null && !info.getId().equals(entity.getId())) {
				return Y9Result.failure("该节点下已存在[" + entity.getName() + "]节点，不能重复添加");
			}
			if (StringUtils.isBlank(entity.getId())) {
				entity.setId(Y9IdGenerator.genId());
			}
			if(entity.getTabIndex() == null) {
				Integer tabIndex = apiDataCatalogRepository.getMaxTabIndex(entity.getParentId());
				if(tabIndex == null) {
					tabIndex = 1;
				}else {
					tabIndex = tabIndex + 1;
				}
				entity.setTabIndex(tabIndex);
			}
			if(entity.getLevel() == null) {
				ApiDataCatalogEntity catalogEntity = getById(entity.getParentId());
				if(catalogEntity == null) {
					entity.setLevel(1);
				}else {
					entity.setLevel(catalogEntity.getLevel() + 1);
				}
			}
			return Y9Result.success(apiDataCatalogRepository.save(entity), "保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> getTree(String parentId, String searchName) {
		if(StringUtils.isBlank(parentId)) {
			parentId = "0";
		}
		List<String> ids = null;
		if(StringUtils.isNotBlank(searchName)) {
			ids = searchTree(searchName);
		}
		return getListMap(parentId, ids);
	}
	
	private List<Map<String, Object>> getListMap(String parentId, List<String> ids) {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		List<ApiDataCatalogEntity> catalogEntities = apiDataCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(ApiDataCatalogEntity catalogEntity : catalogEntities) {
			if(ids != null && !ids.contains(catalogEntity.getId())) {
				continue;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", catalogEntity.getId());
			map.put("name", catalogEntity.getName());
			map.put("parentId", catalogEntity.getParentId());
			map.put("description", catalogEntity.getDescription());
			map.put("tabIndex", catalogEntity.getTabIndex());
			map.put("level", catalogEntity.getLevel());
			map.put("isChild", checkChild(catalogEntity.getId()));
			// 获取子节点
			if(ids != null && ids.size() > 0) {
				map.put("children", getListMap(catalogEntity.getId(), ids));
			}
			listMap.add(map);
		}
		return listMap;
	}
	
	private boolean checkChild(String parentId) {
		List<ApiDataCatalogEntity> catalogEntities = apiDataCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		if(catalogEntities != null && catalogEntities.size() > 0) {
			return true;
		}
		return false;
	}
	
	private List<String> searchTree(String searchName) {
		List<String> ids = new ArrayList<String>();
		List<ApiDataCatalogEntity> catalogEntities = apiDataCatalogRepository.findByNameContaining(searchName);
		for(ApiDataCatalogEntity catalogEntity : catalogEntities) {
			if(!ids.contains(catalogEntity.getId())) {
				ids.add(catalogEntity.getId());
			}
			// 递归获取所有上级节点
			getParentId(ids, catalogEntity.getParentId());
		}
		return ids;
	}
	
	private void getParentId(List<String> ids, String id) {
		ApiDataCatalogEntity catalogEntity = getById(id);
		if(catalogEntity != null) {
			if(!ids.contains(id)) {
				ids.add(id);
			}
			getParentId(ids, catalogEntity.getParentId());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveApiData(ApiServiceModel apiServiceModel) {
		try {
			// 检验sql合格性
			boolean result = checkSqlData(apiServiceModel.getMethod(), apiServiceModel.getSqlData());
			if(result) {
				return Y9Result.failure("保存失败：SQL语句不合规");
			}
			
			ApiServiceEntity apiServiceEntity = null;
			if(StringUtils.isBlank(apiServiceModel.getId())) {
				apiServiceEntity = new ApiServiceEntity();
				apiServiceEntity.setId(Y9IdGenerator.genId());
			}else {
				apiServiceEntity = apiServiceRepository.findById(apiServiceModel.getId()).orElse(null);
			}
			apiServiceEntity.setApiName(apiServiceModel.getApiName());
			apiServiceEntity.setApiType(apiServiceModel.getApiType());
			apiServiceEntity.setApiUrl(apiServiceModel.getApiUrl());
			apiServiceEntity.setDataSourceId(apiServiceModel.getDataSourceId().split("-")[1]);
			apiServiceEntity.setMethod(apiServiceModel.getMethod());
			apiServiceEntity.setParams(Y9JsonUtil.writeValueAsString(apiServiceModel.getParams()));
			apiServiceEntity.setParentId(apiServiceModel.getParentId());
			apiServiceEntity.setRemark(apiServiceModel.getRemark());
			apiServiceEntity.setSqlData(apiServiceModel.getSqlData());
			
			apiServiceRepository.save(apiServiceEntity);
			return Y9Result.successMsg("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}
	
	private boolean checkSqlData(String method, String sqlData) {
		boolean result = false;
		if(method.equals("query")) {
			result = StringUtils.containsAnyIgnoreCase(sqlData, "delete", "insert", "update");
		}else if(method.equals("delete")) {
			result = StringUtils.containsAnyIgnoreCase(sqlData, "insert", "update");
		}else if(method.equals("insert")) {
			result = StringUtils.containsAnyIgnoreCase(sqlData, "delete", "update");
		}else if(method.equals("update")) {
			result = StringUtils.containsAnyIgnoreCase(sqlData, "insert", "delete");
		}else {
			result = StringUtils.containsAnyIgnoreCase(sqlData, "delete", "insert", "update");
		}
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteApiData(String id) {
		apiServiceRepository.deleteById(id);
		return Y9Result.successMsg("删除成功");
	}

	@Override
	public Page<ApiServiceEntity> searchPage(String parentId, String apiName, int page, int size) {
		if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createTime"));
		return apiServiceRepository.findByParentIdAndApiNameContaining(parentId, apiName, pageable);
	}

	@Override
	public ApiServiceEntity findById(String id) {
		return apiServiceRepository.findById(id).orElse(null);
	}

	@Override
	public Y9Result<Map<String, Object>> getApiInfo(String id) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ApiServiceEntity apiServiceEntity = findById(id);
			if(apiServiceEntity != null) {
				map.put("name", apiServiceEntity.getApiName());
				map.put("type", apiServiceEntity.getApiType() == 0 ? "公开" : "私有");
				map.put("remark", apiServiceEntity.getRemark());
				String url = Y9Context.getProperty("y9.common.dataAssetsBaseUrl") + "/services/rest/";
				if(apiServiceEntity.getMethod().equals("query")) {
					map.put("method", "GET");
					url = url + "get/" + apiServiceEntity.getApiUrl() + "/" + apiServiceEntity.getId();
				} else if(apiServiceEntity.getMethod().equals("page")) {
					map.put("method", "GET");
					url = url + "getPage/" + apiServiceEntity.getApiUrl() + "/" + apiServiceEntity.getId();
				} else {
					map.put("method", "POST");
					url = url + "post/" + apiServiceEntity.getApiUrl() + "/" + apiServiceEntity.getId();
				}
				map.put("url", url);
				// 获取参数信息
				String params = apiServiceEntity.getParams();
				if(StringUtils.isNotBlank(params)) {
					map.put("params", Y9JsonUtil.readListOfMap(params));
				}else {
					map.put("params", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("程序出错：" + e.getMessage());
		}
		return Y9Result.success(map);
	}

	@Override
	public Page<ApiRoleEntity> searchRolePage(String appName, int page, int size) {
		if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createTime"));
		return apiRoleRepository.findByAppNameContaining(appName, pageable);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveApiRoleData(ApiRoleEntity apiRoleEntity) {
		ApiRoleEntity entity = apiRoleRepository.findById(apiRoleEntity.getAppName()).orElse(null);
		if(entity == null) {
			apiRoleEntity.setAppKey(Y9CipherUtil.generateAppKey());
		}else {
			apiRoleEntity.setAppKey(entity.getAppKey());
		}
		apiRoleRepository.save(apiRoleEntity);
		return Y9Result.successMsg("保存成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteApiRoleData(String id) {
		apiRoleRepository.deleteById(id);
		return Y9Result.successMsg("删除成功");
	}

	@Override
	public Page<ApiServiceLogEntity> searchLogPage(String appName, int page, int size) {
		if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
		return apiServiceLogRepository.findByAppNameContaining(appName, pageable);
	}

	@Override
	public Y9Result<List<Map<String, Object>>> getApiTreeList() {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		List<ApiServiceEntity> dataEntities = apiServiceRepository.findByApiType(1);
		for(ApiServiceEntity entity : dataEntities) {
			Map<String, Object> map = new HashMap<String, Object>();
			ApiDataCatalogEntity catalogEntity = getById(entity.getParentId());
			map.put("label", catalogEntity.getName() + "-" + entity.getApiName());
			map.put("key", entity.getId());
			listMap.add(map);
		}
		return Y9Result.success(listMap);
	}

	@Override
	public Y9Result<List<String>> getApiRole(String appName) {
		List<String> ids = new ArrayList<String>();
		ApiRoleEntity entity = apiRoleRepository.findById(appName).orElse(null);
		if(entity != null && StringUtils.isNotBlank(entity.getApiIds())) {
			ids = Arrays.asList(entity.getApiIds().split(","));
		}
		return Y9Result.success(ids);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveApiRole(String ids, String appName) {
		try {
			ApiRoleEntity entity = apiRoleRepository.findById(appName).orElse(null);
			if(entity != null) {
				entity.setApiIds(ids);
				apiRoleRepository.save(entity);
			}else {
				return Y9Result.failure("保存失败：主体信息不存在，请刷新下页面");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
		return Y9Result.successMsg("保存成功");
	}

}