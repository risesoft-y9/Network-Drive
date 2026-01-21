package net.risesoft.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.auth.util.Y9SqlUtil;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.ApiServiceModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataSourceService;
import net.risesoft.util.Y9FormDbMetaDataUtil;
import net.risesoft.y9.sqlddl.pojo.DbColumn;

@Validated
@RestController
@RequestMapping(value = "/vue/source", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SourceController {

    private final DataSourceService dataSourceService;

    @RiseLog(operationName = "获取数据源分类列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping(value = "/findCategoryAll")
    public Y9Result<List<DataSourceTypeEntity>> findCategoryAll() {
        List<DataSourceTypeEntity> list = dataSourceService.findDataCategory();
        return Y9Result.success(list, "获取成功");
    }

    @RiseLog(operationName = "根据类别获取数据源列表", logLevel = LogLevelEnum.RSLOG, enable = false)
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

    @RiseLog(operationName = "获取数据表列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping(value = "/getTablePage")
    public Y9Result<List<Map<String, Object>>> getTablePage(String id, String name) {
        return Y9Result.success(dataSourceService.getTablePage(id, name), "获取数据库表成功");
    }

    @RiseLog(operationName = "获取表字段信息", logLevel = LogLevelEnum.RSLOG, enable = false)
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

    @RiseLog(operationName = "获取表字段数据信息", logLevel = LogLevelEnum.RSLOG, enable = false)
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

    @RiseLog(operationName = "搜索数据源列表", logLevel = LogLevelEnum.RSLOG, enable = false)
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

    @RiseLog(operationName = "根据id获取数据源信息", logLevel = LogLevelEnum.RSLOG, enable = false)
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

    @RiseLog(operationName = "检测数据源状态", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping(value = "/checkStatus")
    public Y9Result<Boolean> checkStatus(String sourceId) {
        DataSourceEntity source = dataSourceService.getDataSourceById(sourceId);
        if (source != null && source.getType() == 0) {
            return Y9Result.success(Y9FormDbMetaDataUtil.getConnection(source.getDriver(), source.getUsername(),
                source.getPassword(), source.getUrl()));
        }
        return Y9Result.success(true);
    }

    @GetMapping(value = "/getTableSelectTree")
    public Y9Result<List<Map<String, Object>>> getTableSelectTree(String type) {
        return Y9Result.success(dataSourceService.getTableSelectTree(type));
    }

    @RiseLog(operationType = OperationTypeEnum.CHECK, operationName = "SQL语句测试", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/testSql")
    public Y9Result<List<Map<String, Object>>> testSql(ApiServiceModel apiServiceModel) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        try {
            DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(apiServiceModel.getDataSourceId());
            if (dataSourceEntity != null) {
                if (apiServiceModel.getParams() == null) {
                    listMap = Y9SqlUtil.executeQuery(apiServiceModel.getSqlData(), dataSourceEntity.getUrl(),
                        dataSourceEntity.getUsername(), dataSourceEntity.getPassword(), dataSourceEntity.getDriver());
                } else {
                    List<Object> object = new ArrayList<Object>();
                    int page = 0, size = 0;
                    for (Map<String, Object> data : apiServiceModel.getParams()) {
                        // 获取参数名称
                        String name = data.get("name").toString();
                        // 用参数名称获取值
                        Object value = data.get("value");

                        if (name.equals("page")) {
                            page = Integer.parseInt(value.toString());
                        } else if (name.equals("size")) {
                            size = Integer.parseInt(value.toString());
                        } else {
                            object.add(value);
                        }
                    }
                    if (size != 0) {
                        object.add(size);
                        object.add((page - 1) * size);
                    }
                    listMap = Y9SqlUtil.executeQuery(apiServiceModel.getSqlData(), dataSourceEntity.getUrl(),
                        dataSourceEntity.getUsername(), dataSourceEntity.getPassword(), dataSourceEntity.getDriver(),
                        object.toArray());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("程序出错：" + e.getMessage());
        }
        return Y9Result.success(listMap);
    }

    @GetMapping(value = "/getDataBase")
    public Y9Result<List<Map<String, Object>>> getDataBase() {
        return Y9Result.success(dataSourceService.getDataBase());
    }

    @GetMapping(value = "/getDataSourceByAssetsId")
    public Y9Result<List<Map<String, Object>>> getDataSourceByAssetsId(Long assetsId) {
        return Y9Result.success(dataSourceService.getDataSourceByAssetsId(assetsId));
    }

    @RiseLog(operationName = "根据资产id获取挂接的数据表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping(value = "/getTableByAssetsId")
    public Y9Result<List<Map<String, Object>>> getTableByAssetsId(Long assetsId, String identifier) {
        List<String> list = dataSourceService.getTableByAssetsId(assetsId, identifier);
        List<Map<String, Object>> listMap = dataSourceService.getTablePage(identifier, "");
        // 过滤掉listMap里name值不在list的元素
        listMap =
            listMap.stream().filter((item) -> list.contains(item.get("name").toString())).collect(Collectors.toList());
        return Y9Result.success(listMap, "获取数据库表成功");
    }

}
