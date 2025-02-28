package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataSourceService;
import net.risesoft.util.Y9FormDbMetaDataUtil;
import net.risesoft.util.db.DbMetaDataUtil;
import net.risesoft.y9.sqlddl.pojo.DbColumn;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/vue/source", produces = "application/json")
@RequiredArgsConstructor
public class SourceController {

    private final DataSourceService dataSourceService;

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据源分类列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findCategoryAll")
    public Y9Result<List<DataSourceTypeEntity>> findCategoryAll() {
        List<DataSourceTypeEntity> list = dataSourceService.findDataCategory();
        return Y9Result.success(list, "获取成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据类别获取数据源列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findByBaseType")
    public Y9Result<List<DataSourceEntity>> findByBaseType(@RequestParam String category) {
        List<DataSourceEntity> list = dataSourceService.findByBaseType(category);
        list.stream().map((item) -> {
            if (StringUtils.isNotBlank(item.getPassword())) {
                item.setPassword("******");
            }
            return item;
        }).collect(Collectors.toList());
        return Y9Result.success(list, "获取成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据类别获取数据信息列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findTablesByBaseType")
    public Y9Result<List<Map<String, Object>>> findTablesByBaseType(@RequestParam String category) {
        List<Map<String, Object>> list_map = new ArrayList<>();
        try {
            if (StringUtils.isBlank(category)) {
                List<DataSourceTypeEntity> plist = dataSourceService.findDataCategory();
                for (DataSourceTypeEntity item : plist) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getId());
                    map.put("name", item.getName());
                    map.put("url", "");
                    map.put("driver", item.getDriver());
                    map.put("type", item.getType());
                    list_map.add(map);
                }
                return Y9Result.success(list_map, "获取成功");
            }

            List<DataSourceEntity> list = dataSourceService.findByBaseType(category);
            for (DataSourceEntity item : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("name", item.getName());
                map.put("url", item.getUrl());
                map.put("driver", item.getDriver());
                map.put("username", item.getUsername());
                map.put("password", item.getPassword());
                map.put("type", item.getType());
                map.put("baseType", item.getBaseType());
                map.put("createTime", item.getCreateTime());
                map.put("updateTime", item.getUpdateTime());
                map.put("children", new ArrayList<>());
                DataSource dataSource = Y9FormDbMetaDataUtil.createDataSource(item.getUrl(), item.getDriver(),
                    item.getUsername(), item.getPassword());

                try {
                    List<Map<String, Object>> childList_map = new ArrayList<>();
                    List<Map<String, Object>> table_map = Y9FormDbMetaDataUtil.listAllTables(dataSource);
                    int i = 0;
                    for (Map<String, Object> table : table_map) {
                        Map<String, Object> child_map = new HashMap<>();
                        child_map.put("id", item.getId() + i);
                        child_map.put("name", table.get("name").toString());
                        child_map.put("url", item.getId());
                        child_map.put("driver", item.getDriver());
                        child_map.put("username", item.getUsername());
                        child_map.put("password", item.getPassword());
                        child_map.put("type", 3);
                        child_map.put("baseType", item.getBaseType());
                        child_map.put("createTime", item.getCreateTime());
                        child_map.put("updateTime", item.getUpdateTime());
                        childList_map.add(child_map);
                        i++;
                    }
                    map.put("children", childList_map);
                } catch (Exception e) {
                    LOGGER.error("获取数据库表失败", e);
                }
                if (StringUtils.isNotBlank(item.getPassword())) {
                    item.setPassword("******");
                }
                list_map.add(map);
            }
        } catch (Exception e) {
            LOGGER.error("获取数据库表失败", e);
        }
        return Y9Result.success(list_map, "获取数据库表成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取表字段信息", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findTableColumnsByTableName")
    public Y9Result<List<DbColumn>> findTableColumnsByTableName(String dataSourceId, String tableName) {
        List<DbColumn> list = new ArrayList<>();
        DataSourceEntity item = dataSourceService.getDataSourceById(dataSourceId);
        if (null != item) {
            DataSource dataSource = Y9FormDbMetaDataUtil.createDataSource(item.getUrl(), item.getDriver(),
                item.getUsername(), item.getPassword());
            try {
                list = Y9FormDbMetaDataUtil.listAllColumns(Objects.requireNonNull(dataSource), tableName, "%");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Y9Result.success(list, "获取表字段信息成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取表字段数据信息", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findTableDataByTableName")
    public Y9Page<Map<String, Object>> findTableDataByTableName(String dataSourceId, String tableName,
        @RequestParam(required = false) String columnNameAndValues, @RequestParam Integer page,
        @RequestParam Integer rows) {
        try {
            DataSourceEntity item = dataSourceService.getDataSourceById(dataSourceId);
            if (null != item) {
                DataSource dataSource = Y9FormDbMetaDataUtil.createDataSource(item.getUrl(), item.getDriver(),
                    item.getUsername(), item.getPassword());
                return Y9FormDbMetaDataUtil.listTableData(Objects.requireNonNull(dataSource), columnNameAndValues,
                    tableName, page, rows);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Y9Page.failure(page, rows, 0, null, "获取数据失败", 500);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据源列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/findByType")
    public Y9Result<List<DataSourceEntity>> findByType(Integer type) {
        return Y9Result.success(dataSourceService.findByType(type), "获取成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "搜索数据源列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/searchSource")
    public Y9Result<List<Map<String, Object>>> searchSource(String name) {
        return Y9Result.success(dataSourceService.searchSource(name), "获取成功");
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存数据源分类信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveDataCategory")
    public Y9Result<DataSourceTypeEntity> saveDataCategory(MultipartFile iconFile, DataSourceTypeEntity entity) {
        return dataSourceService.saveDataCategory(iconFile, entity);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除数据源分类信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteCategory")
    public Y9Result<String> deleteCategory(@RequestParam String id) {
        return dataSourceService.deleteCategory(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存数据源连接信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveSource")
    public Y9Result<String> saveSource(DataSourceEntity entity) {
        dataSourceService.saveDataSource(entity);
        return Y9Result.successMsg("保存成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据id获取数据源信息", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/getDataSource")
    public Y9Result<DataSourceEntity> getDataSource(String id) {
        DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(id);
        if (dataSourceEntity != null && StringUtils.isNotBlank(dataSourceEntity.getPassword())) {
            dataSourceEntity.setPassword("******");
        }
        return Y9Result.success(dataSourceEntity, "获取成功");
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除数据源", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteSource")
    public Y9Result<String> deleteSource(@RequestParam String id) {
        return dataSourceService.deleteDataSource(id);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据库需要提取的表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/getNotExtractList")
    public Map<String, Object> getNotExtractList(String baseId, String tableName) {
        Map<String, Object> map = dataSourceService.getNotExtractList(baseId, tableName);
        return map;
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "检测数据源状态", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/checkStatus")
    public Y9Result<Boolean> checkStatus(String sourceId) {
        DataSourceEntity source = dataSourceService.getDataSourceById(sourceId);
        if (source != null && source.getType() == 0) {
            return Y9Result.success(DbMetaDataUtil.getConnection(source.getDriver(), source.getUsername(),
                source.getPassword(), source.getUrl()));
        }
        return Y9Result.success(true);
    }

}
