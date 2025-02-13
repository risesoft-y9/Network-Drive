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
import net.risesoft.entity.CategoryTableField;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CategoryTableFieldService;
import net.risesoft.util.Y9FormDbMetaDataUtil;

@Slf4j
@RestController
@RequestMapping(value = "/vue/category/tableField", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryTableFieldController {

    private final JdbcTemplate jdbcTemplate4Tenant;

    private final CategoryTableFieldService categoryTableFieldService;

    public CategoryTableFieldController(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant,
        CategoryTableFieldService categoryTableFieldService) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.categoryTableFieldService = categoryTableFieldService;
    }

    /**
     * 删除表字段定义
     *
     * @param id 字段id
     * @return Y9Result<String>
     */
    @PostMapping(value = "/deleteField")
    public Y9Result<String> deleteField(@RequestParam String id) {
        categoryTableFieldService.delete(id);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 获取表字段定义列表
     *
     * @param tableId 表id
     * @return Y9Result<Map<String, Object>>
     */
    @GetMapping(value = "/getTableFieldList")
    public Y9Result<List<CategoryTableField>> getTableFieldList(@RequestParam String tableId) {
        List<CategoryTableField> map = categoryTableFieldService.listByTableId(tableId);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 获取新增或修改表字段信息
     *
     * @param id 字段id
     * @param tableId 表id
     * @return Y9Result<Map<String, Object>>
     */
    @GetMapping(value = "/newOrModifyField")
    public Y9Result<Map<String, Object>> newOrModifyField(@RequestParam(required = false) String id,
        @RequestParam String tableId) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("tableId", tableId);
        CategoryTableField field;
        if (StringUtils.isNotBlank(id) && !UtilConsts.NULL.equals(id)) {
            field = categoryTableFieldService.findById(id);
            // if (field != null) {
            // String type = field.getFieldType();
            // field.setFieldType(type);
            // }
            map.put("id", id);
            map.put("field", field);
        }
        try {
            List<Map<String, Object>> list =
                Y9FormDbMetaDataUtil.listTypes(Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()));
            map.put("typeList", list);
            String databaseName = Y9FormDbMetaDataUtil
                .getDatabaseDialectName(Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()));
            map.put("databaseName", databaseName);
        } catch (Exception e) {
            LOGGER.error("获取数据库类型失败", e);
            return Y9Result.failure("获取失败");
        }
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 保存或更新表字段定义
     *
     * @param field 字段信息
     * @return Y9Result<String>
     */
    @PostMapping(value = "/saveField")
    public Y9Result<String> saveField(CategoryTableField field) {
        try {
            categoryTableFieldService.saveOrUpdate(field);
            return Y9Result.successMsg("保存成功");
        } catch (Exception e) {
            LOGGER.error("保存失败", e);
            return Y9Result.failure("保存失败");
        }
    }

}
