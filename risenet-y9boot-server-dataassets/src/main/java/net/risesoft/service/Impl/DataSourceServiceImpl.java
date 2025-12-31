package net.risesoft.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataSourceRepository;
import net.risesoft.repository.DataSourceTypeRepository;
import net.risesoft.service.DataSourceService;
import net.risesoft.util.Y9FormDbMetaDataUtil;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.permission.cache.PersonRoleApiClient;

import jodd.util.Base64;

@Service(value = "dataSourceService")
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository datasourceRepository;
    private final DataSourceTypeRepository dataSourceTypeRepository;
    private final PersonRoleApiClient personRoleApiClient;

    @Override
    @Transactional(readOnly = false)
    public DataSourceEntity saveDataSource(DataSourceEntity entity) {
        DataSourceEntity df = null;
        if (entity != null) {
            if (StringUtils.isBlank(entity.getId())) {
                entity.setId(Y9IdGenerator.genId());
            } else {
                df = datasourceRepository.findById(entity.getId()).orElse(null);
                if (df != null && entity.getPassword().equals("******")) {
                    entity.setPassword(df.getPassword());
                }
            }
            if (entity.getIsLook() == null) {
                entity.setIsLook(0);
            }
            DataSourceTypeEntity category = dataSourceTypeRepository.findByName(entity.getBaseType());
            if (StringUtils.isBlank(entity.getDriver())) {
                entity.setDriver(category.getDriver());
            }
            entity.setType(category.getType());
            if (entity.getType() == 0) {
                if (entity.getInitialSize() == null) {
                    entity.setInitialSize(1);
                }
                if (entity.getMaxActive() == null) {
                    entity.setMaxActive(20);
                }
                if (entity.getMinIdle() == null) {
                    entity.setMinIdle(1);
                }
            }
            entity.setTenantId(Y9LoginUserHolder.getTenantId());
            entity.setUserId(Y9LoginUserHolder.getPersonId());
            df = datasourceRepository.save(entity);
        }
        return df;
    }

    @Override
    public DataSourceEntity getDataSourceById(String id) {
        return datasourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> deleteDataSource(String id) {
        datasourceRepository.deleteById(id);
        return Y9Result.successMsg("删除成功");
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<DataSourceTypeEntity> saveDataCategory(MultipartFile iconFile, DataSourceTypeEntity entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getName())) {
            DataSourceTypeEntity category = dataSourceTypeRepository.findByName(entity.getName());
            if (category != null && !entity.getId().equals(category.getId())) {
                return Y9Result.failure("该分类已存在！");
            }
            if (StringUtils.isBlank(entity.getId())) {
                entity.setId(Y9IdGenerator.genId());
            } else {
                DataSourceTypeEntity dataSourceTypeEntity =
                    dataSourceTypeRepository.findById(entity.getId()).orElse(null);
                if (dataSourceTypeEntity != null && !dataSourceTypeEntity.getName().equals(entity.getName())) {
                    long count = datasourceRepository.countByBaseType(dataSourceTypeEntity.getName());
                    if (count > 0) {
                        return Y9Result.failure("该分类存在关联数据，名称无法修改！");
                    }
                }
            }
            if (iconFile != null && iconFile.getSize() != 0) {
                byte[] b = null;
                try {
                    b = iconFile.getBytes();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                entity.setImgData(Base64.encodeToString(b));
            }
            return Y9Result.success(dataSourceTypeRepository.save(entity), "保存成功");
        }
        return Y9Result.failure("数据不能为空");
    }

    @Override
    public List<DataSourceTypeEntity> findDataCategory() {
        return dataSourceTypeRepository.findAll();
    }

    @Override
    public List<DataSourceEntity> findByBaseType(String baseType) {
        // 判断是否系统管理员
        boolean isAdmin = personRoleApiClient
            .hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId())
            .getData();
        if (isAdmin) {
            return datasourceRepository.findByBaseTypeAndTenantIdOrderByCreateTime(baseType,
                Y9LoginUserHolder.getTenantId());
        } else {
            return datasourceRepository.findByBaseTypeAndTenantIdAndUserIdOrderByCreateTime(baseType,
                Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId());
        }
    }

    @Override
    public List<Map<String, Object>> searchSource(String baseName) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 判断是否系统管理员
        boolean isAdmin = personRoleApiClient
            .hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId())
            .getData();
        List<DataSourceTypeEntity> list = dataSourceTypeRepository.findAll();
        for (DataSourceTypeEntity category : list) {
            List<DataSourceEntity> sourceList = null;
            if (isAdmin) {
                sourceList = datasourceRepository.findByNameContainingAndBaseTypeAndTenantId(baseName,
                    category.getName(), Y9LoginUserHolder.getTenantId());
            } else {
                sourceList = datasourceRepository.findByNameContainingAndBaseTypeAndTenantIdAndUserId(baseName,
                    category.getName(), Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId());
            }
            if (sourceList.size() == 0) {
                continue;
            }
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for (DataSourceEntity source : sourceList) {
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("id", source.getId());
                rmap.put("baseName", source.getName());
                children.add(rmap);
            }
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("category", category);
            row.put("children", children);
            listMap.add(row);
        }
        return listMap;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> deleteCategory(String id) {
        DataSourceTypeEntity dataSourceTypeEntity = dataSourceTypeRepository.findById(id).orElse(null);
        if (dataSourceTypeEntity != null) {
            long count = datasourceRepository.countByBaseType(dataSourceTypeEntity.getName());
            if (count > 0) {
                return Y9Result.failure("该分类存在关联数据，无法删除！");
            }
            dataSourceTypeRepository.deleteById(id);
            return Y9Result.successMsg("删除成功");
        }
        return Y9Result.failure("数据不存在，请刷新数据");
    }

    @Override
    public List<Map<String, Object>> getTableSelectTree(String type) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        try {
            List<DataSourceTypeEntity> dataSourceTypeEntities = findDataCategory();
            for (DataSourceTypeEntity entity : dataSourceTypeEntities) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("value", entity.getId());
                map.put("label", entity.getName());
                map.put("disabled", true);

                List<Map<String, Object>> child1 = new ArrayList<>();
                List<DataSourceEntity> list = findByBaseType(entity.getName());
                for (DataSourceEntity info : list) {
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("value", "s-" + info.getId());
                    map2.put("label", info.getName());

                    if (type.equals("table")) {
                        // 获取数据源表
                        DataSource dataSource = Y9FormDbMetaDataUtil.createDataSource(info.getUrl(), info.getDriver(),
                            info.getUsername(), info.getPassword());
                        List<Map<String, Object>> child2 = new ArrayList<>();
                        List<Map<String, Object>> table_map = Y9FormDbMetaDataUtil.listAllTables(dataSource);
                        for (Map<String, Object> table : table_map) {
                            Map<String, Object> map3 = new HashMap<>();
                            map3.put("value", "t-" + info.getId() + "-" + table.get("name").toString());
                            map3.put("label", table.get("name").toString());
                            child2.add(map3);
                        }
                        map2.put("children", child2);
                    }

                    child1.add(map2);
                }
                map.put("children", child1);
                listMap.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMap;
    }

    @Override
    public List<Map<String, Object>> getTablePage(String id, String name) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        try {
            DataSourceEntity dataSourceEntity = getDataSourceById(id);
            if (dataSourceEntity != null) {
                // 获取数据源
                DataSource dataSource = Y9FormDbMetaDataUtil.createDataSource(dataSourceEntity.getUrl(),
                    dataSourceEntity.getDriver(), dataSourceEntity.getUsername(), dataSourceEntity.getPassword());
                // 获取数据表
                List<Map<String, Object>> table_map = Y9FormDbMetaDataUtil.listAllTables(dataSource);
                for (Map<String, Object> table : table_map) {
                    String tableName = table.get("name").toString();
                    if (StringUtils.isNotBlank(name) && !tableName.contains(name)) {
                        continue;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", tableName);
                    map.put("cname", table.get("cname").toString());
                    map.put("sourceId", id);
                    listMap.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMap;
    }

    @Override
    public List<Map<String, Object>> getDataBase() {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        try {
            List<DataSourceTypeEntity> dataSourceTypeEntities = findDataCategory();
            for (DataSourceTypeEntity entity : dataSourceTypeEntities) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("value", entity.getId());
                map.put("label", entity.getName());
                map.put("disabled", true);

                List<Map<String, Object>> child1 = new ArrayList<>();
                List<DataSourceEntity> list = datasourceRepository.findByBaseTypeAndTenantIdOrderByCreateTime(entity.getName(),
                Y9LoginUserHolder.getTenantId());
                for (DataSourceEntity info : list) {
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("value", info.getId());
                    map2.put("label", info.getName());

                    child1.add(map2);
                }
                map.put("children", child1);
                listMap.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMap;
    }

}