package net.risesoft.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataApiTableEntity;
import net.risesoft.entity.TableForeignKeyEntity;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataApiTableService;

@Validated
@RestController
@RequestMapping(value = "/vue/dataApiTable", produces = "application/json")
@RequiredArgsConstructor
public class DataApiTableController {

    private final DataApiTableService dataApiTableService;

    @GetMapping(value = "/page")
    public Y9Page<DataApiTableEntity> page(@RequestParam(required = false) String tableName,
                                                   @RequestParam(defaultValue = "1") int page, 
                                                   @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<DataApiTableEntity> pageList = dataApiTableService.findByTableName(tableName, pageable);
            return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Page.failure(page, 0, 0, null, "查询失败：" + e.getMessage(), 500);
        }
    }

    @PostMapping(value = "/save")
    public Y9Result<String> save(@RequestBody DataApiTableEntity dataApiTableEntity) {
        try {
            String saved = dataApiTableService.save(dataApiTableEntity);
            return Y9Result.successMsg(saved);
        } catch (Exception e) {
            return Y9Result.failure("保存失败：" + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    public Y9Result<String> delete(@PathVariable Long id) {
        try {
            dataApiTableService.deleteById(id);
            return Y9Result.successMsg("操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("操作失败：" + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}")
    public Y9Result<DataApiTableEntity> findById(@PathVariable Long id) {
        try {
            DataApiTableEntity dataApiTableEntity = dataApiTableService.findById(id);
            return Y9Result.success(dataApiTableEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("查询失败：" + e.getMessage());
        }
    }

    @GetMapping(value = "/foreignKeys/{tableName}")
    public Y9Result<List<String>> findForeignKeysByTableName(@PathVariable String tableName,
                                                                             @RequestParam String dataSourceId) {
        try {
            List<String> foreignKeys = dataApiTableService.findForeignKeysByTableName(tableName, dataSourceId);
            return Y9Result.success(foreignKeys);
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("查询失败：" + e.getMessage());
        }
    }
}