package net.risesoft.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataApiOnlineEntity;
import net.risesoft.entity.DataApiOnlineInfoEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataApiOnlineInfoRepository;
import net.risesoft.repository.DataApiOnlineRepository;
import net.risesoft.service.DataApiOnlineService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

import y9.client.rest.platform.permission.cache.PersonRoleApiClient;

@Service(value = "dataApiOnlineService")
@RequiredArgsConstructor
public class DataApiOnlineServiceImpl implements DataApiOnlineService {

    private final DataApiOnlineRepository dataApiOnlineRepository;
    private final DataApiOnlineInfoRepository dataApiOnlineInfoRepository;
    private final PersonRoleApiClient personRoleApiClient;

    @Override
    @Transactional(readOnly = false)
    public Y9Result<List<String>> deleteData(String id) {
        List<String> removeIds = new ArrayList<String>();
        try {
            DataApiOnlineEntity dataApiOnlineEntity = dataApiOnlineRepository.findById(id).orElse(null);
            if (dataApiOnlineEntity == null) {
                return Y9Result.failure("删除失败，主体信息不存在");
            } else {
                if (dataApiOnlineEntity.getType().equals("folder")) {
                    delete(id, removeIds);
                } else {
                    dataApiOnlineInfoRepository.deleteById(id);
                }
                dataApiOnlineRepository.deleteById(id);
                removeIds.add(id);
            }
        } catch (Exception e) {
            return Y9Result.failure("删除失败，" + e.getMessage());
        }
        return Y9Result.success(removeIds);
    }

    private void delete(String parentId, List<String> removeIds) {
        List<String> ids = dataApiOnlineRepository.findByParentId(parentId);
        for (String id : ids) {
            DataApiOnlineEntity dataApiOnlineEntity = dataApiOnlineRepository.findById(id).orElse(null);
            if (dataApiOnlineEntity.getType().equals("folder")) {
                delete(dataApiOnlineEntity.getId(), removeIds);
            } else {
                dataApiOnlineInfoRepository.deleteById(id);
            }
            dataApiOnlineRepository.deleteById(id);
            removeIds.add(id);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<DataApiOnlineEntity> saveData(DataApiOnlineEntity entity, DataApiOnlineInfoEntity infoEntity) {
        if (StringUtils.isBlank(entity.getId())) {
            entity.setId(Y9IdGenerator.genId());
            entity.setCreator(Y9LoginUserHolder.getUserInfo().getName());
            entity.setCreatorId(Y9LoginUserHolder.getPersonId());
        } else {
            DataApiOnlineEntity dataApiOnlineEntity = dataApiOnlineRepository.findById(entity.getId()).orElse(null);
            if (dataApiOnlineEntity != null) {
                entity.setCreator(dataApiOnlineEntity.getCreator());
                entity.setCreatorId(dataApiOnlineEntity.getCreatorId());
            } else {
                return Y9Result.failure("数据不存在，修改失败");
            }
        }
        if (infoEntity != null) {
            infoEntity.setId(entity.getId());
            dataApiOnlineInfoRepository.save(infoEntity);
        }
        return Y9Result.success(dataApiOnlineRepository.save(entity), "保存成功");
    }

    @Override
    public List<Map<String, Object>> getTree(String id) {
        if (StringUtils.isBlank(id)) {
            id = "0";
        }
        // 判断是否系统管理员
        boolean isAdmin = personRoleApiClient
            .hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId())
            .getData();
        return getListMap(id, isAdmin);
    }

    private List<Map<String, Object>> getListMap(String parentId, boolean isAdmin) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        List<DataApiOnlineEntity> apiList = null;
        if (parentId.equals("0")) {
            if (isAdmin) {
                apiList = dataApiOnlineRepository.findByParentIdOrderByCreateTime(parentId);
            } else {
                apiList = dataApiOnlineRepository.findByParentIdAndCreatorIdOrderByCreateTime(parentId,
                    Y9LoginUserHolder.getPersonId());
            }
        } else {
            apiList = dataApiOnlineRepository.findByParentIdOrderByCreateTime(parentId);
        }
        for (DataApiOnlineEntity apiOnlineEntity : apiList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", apiOnlineEntity.getId());
            map.put("name", apiOnlineEntity.getName());
            map.put("parentId", apiOnlineEntity.getParentId());
            map.put("type", apiOnlineEntity.getType());

            if (apiOnlineEntity.getType().equals("folder")) {
                map.put("children", getListMap(apiOnlineEntity.getId(), isAdmin));
            } else {
                String json = dataApiOnlineInfoRepository.findById(apiOnlineEntity.getId()).orElse(null).getFormData();
                map.put("ApiForm", Y9JsonUtil.readHashMap(json));
            }
            listMap.add(map);
        }
        return listMap;
    }

    @Override
    public List<Map<String, Object>> getSelectTree() {
        // 判断是否系统管理员
        boolean isAdmin = personRoleApiClient
            .hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId())
            .getData();
        return getSelectListMap("0", isAdmin);
    }

    private List<Map<String, Object>> getSelectListMap(String parentId, boolean isAdmin) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        List<DataApiOnlineEntity> apiList = null;
        if (parentId.equals("0")) {
            if (isAdmin) {
                apiList = dataApiOnlineRepository.findByParentIdOrderByCreateTime(parentId);
            } else {
                apiList = dataApiOnlineRepository.findByParentIdAndCreatorIdOrderByCreateTime(parentId,
                    Y9LoginUserHolder.getPersonId());
            }
        } else {
            apiList = dataApiOnlineRepository.findByParentIdOrderByCreateTime(parentId);
        }
        for (DataApiOnlineEntity apiOnlineEntity : apiList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("value", apiOnlineEntity.getId());
            map.put("label", apiOnlineEntity.getName());

            map.put("children", getSelectListMap(apiOnlineEntity.getId(), isAdmin));
            listMap.add(map);
        }
        return listMap;
    }

    @Override
    public Y9Result<Map<String, Object>> getApiInfo(String id) {
        DataApiOnlineInfoEntity apiOnlineInfoEntity = dataApiOnlineInfoRepository.findById(id).orElse(null);
        return Y9Result.success(Y9JsonUtil.readHashMap(apiOnlineInfoEntity.getFormData()));
    }

}