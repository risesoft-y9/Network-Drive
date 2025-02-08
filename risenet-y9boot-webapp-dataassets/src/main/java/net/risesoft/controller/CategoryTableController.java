package net.risesoft.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.CategoryTable;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CategoryTableService;
import net.risesoft.util.Y9FormDbMetaDataUtil;
import net.risesoft.y9.json.Y9JsonUtil;

@Slf4j
@RestController
@RequestMapping(value = "/vue/category/table", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryTableController {
    private final JdbcTemplate jdbcTemplate4Tenant;

    private final CategoryTableService categoryTableService;

    public CategoryTableController(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant,
        CategoryTableService categoryTableService) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.categoryTableService = categoryTableService;
    }

    @PostMapping(value = "/categoryIsExistTable")
    public Y9Result<Object> categoryIsExistTable(@RequestParam String categoryMark) {
        CategoryTable categoryTable = categoryTableService.checkCategoryIsExistTable(categoryMark);
        return Y9Result.success(categoryTable, "获取成功");
    }

    /**
     * 添加数据库表
     *
     * @param tableName 表名称
     * @param systemName 系统名称
     * @param systemCnName 系统中文名称
     * @return
     */
    @PostMapping(value = "/addDataBaseTable")
    public Y9Result<Object> addDataBaseTable(@RequestParam String tableName, @RequestParam String systemName,
        @RequestParam String systemCnName) {
        return categoryTableService.addDataBaseTable(tableName, systemName, systemCnName);
    }

    /**
     * 新生成表，创建数据库表
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return
     */
    @PostMapping(value = "/buildTable")
    public Y9Result<Object> buildTable(@RequestParam String tables, @RequestParam String fields) {
        CategoryTable table = Y9JsonUtil.readValue(tables, CategoryTable.class);
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        return categoryTableService.buildTable(table, listMap);
    }

    /**
     * 数据库是否存在该表
     *
     * @param tableName 表名
     * @return
     */
    @GetMapping(value = "/checkTableExist")
    public Y9Result<String> checkTableExist(@RequestParam String tableName) {
        try {
            boolean msg = Y9FormDbMetaDataUtil
                .checkTableExist(Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()), tableName);
            return Y9Result.success(msg ? "exist" : "isNotExist", "获取成功");
        } catch (Exception e) {
            LOGGER.error("获取数据库表失败", e);
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 获取数据库表
     *
     * @param name 表名
     * @return
     */
    @GetMapping(value = "/getAllTables")
    public Y9Result<Map<String, Object>> getAllTables(@RequestParam(required = false) String name) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> list = categoryTableService.getAllTables(name);
        String tableNames = categoryTableService.getAlltableName();
        map.put("rows", list);
        map.put("tableNames", tableNames);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 获取新增或修改表数据
     *
     * @param id 表id
     * @return
     */
    @GetMapping(value = "/newOrModifyTable")
    public Y9Result<Map<String, Object>> newOrModifyTable(@RequestParam(required = false) String id) {
        Map<String, Object> map = new HashMap<>(16);
        try {
            String databaseName = Y9FormDbMetaDataUtil
                .getDatabaseDialectName(Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()));
            map.put("databaseName", databaseName);
            if (StringUtils.isNotBlank(id) && !UtilConsts.NULL.equals(id)) {
                CategoryTable categoryTable = categoryTableService.findById(id);
                map.put("categoryTable", categoryTable);
            }
            String tableNames = categoryTableService.getAlltableName();
            map.put("tableNames", tableNames);
            return Y9Result.success(map, "获取成功");
        } catch (Exception e) {
            LOGGER.error("获取数据库表失败", e);
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 删除业务表
     *
     * @param ids 表ids
     * @return
     */
    @PostMapping(value = "/removeTable")
    public Y9Result<Object> removeTable(@RequestParam String ids) {
        return categoryTableService.delete(ids);
    }

    /**
     * 保存业务表
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return
     */
    @PostMapping(value = "/saveTable")
    public Y9Result<Object> saveTable(@RequestParam(required = false) String tables,
        @RequestParam(required = false) String fields) {
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        CategoryTable table = Y9JsonUtil.readValue(tables, CategoryTable.class);
        return categoryTableService.updateTable(table, listMap, "save");
    }

    /**
     * 修改表结构，增加字段
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return Y9Result<String>
     */
    @PostMapping(value = "/updateTable")
    public Y9Result<Object> updateTable(@RequestParam String tables, @RequestParam String fields) {
        CategoryTable table = Y9JsonUtil.readValue(tables, CategoryTable.class);
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        return categoryTableService.updateTable(table, listMap, "");
    }

}
