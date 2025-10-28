package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.LabelCatalogEntity;
import net.risesoft.entity.LabelDataEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.LabelService;

@Validated
@RestController
@RequestMapping(value = "/vue/label", produces = "application/json")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取目录树", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/getTree")
    public Y9Result<List<Map<String, Object>>> getTree(String parentId, String name) {
        return Y9Result.success(labelService.getTree(parentId, name), "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取目录信息", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/{id}")
    public Y9Result<LabelCatalogEntity> getData(@PathVariable @NotBlank String id) {
        return Y9Result.success(labelService.getById(id), "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存目录信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveData")
    public Y9Result<LabelCatalogEntity> saveData(LabelCatalogEntity entity) {
        return labelService.saveData(entity);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除目录数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteData")
    public Y9Result<String> deleteData(@RequestParam String id) {
        return labelService.deleteData(id);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "分页获取标签列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/searchPage")
    public Y9Page<LabelDataEntity> searchPage(String parentId, Integer page, Integer size) {
        Page<LabelDataEntity> pageList = labelService.searchPage(parentId, page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存标签", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveLabelData")
    public Y9Result<String> saveLabelData(LabelDataEntity entity) {
        return labelService.saveLabelData(entity);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除标签数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteLabelData")
    public Y9Result<String> deleteLabelData(@RequestParam String id) {
        return labelService.deleteLabelData(id);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取标签列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/getLabelDataList")
    public Y9Result<List<Map<String, Object>>> getLabelDataList() {
        return labelService.getLabelDataList();
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存资产标签数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveAssetsLabel")
    public Y9Result<String> saveAssetsLabel(String ids, Long assetsId) {
        return labelService.saveAssetsLabel(ids, assetsId);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取资产标签列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping(value = "/getLabels")
    public Y9Result<List<String>> getLabels(Long assetsId) {
        return labelService.getLabels(assetsId);
    }

}
