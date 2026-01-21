package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataDemandEntity;
import net.risesoft.entity.DataDemandEntity2;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataDemandService;

@Validated
@RestController
@RequestMapping(value = "/vue/demand", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DataDemandController {

    private final DataDemandService dataDemandService;

    @RiseLog(operationName = "获取需求信息", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping(value = "/getDemandById")
    public Y9Result<DataDemandEntity> getDemandById(@RequestParam String id) {
        return dataDemandService.getById(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存需求信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveData")
    public Y9Result<String> saveData(DataDemandEntity dataDemandEntity) {
        return dataDemandService.saveData(dataDemandEntity);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除需求数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteData")
    public Y9Result<String> deleteData(@RequestParam String id) {
        return dataDemandService.deleteData(id);
    }

    @RiseLog(operationName = "分页获取需求列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping("/searchPage")
    public Y9Page<Map<String, Object>> searchPage(String searchText, Integer dataType, Integer industry, Integer budget,
        Integer status, Integer sort, Integer page, Integer size) {
        return dataDemandService.searchPage(searchText, dataType, industry, budget, status, sort, page, size);
    }

    @RiseLog(operationName = "分页获取需求列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping("/searchPage2")
    public Y9Page<Map<String, Object>> searchPage2(String searchText, Integer page, Integer size) {
        return dataDemandService.searchPage2(searchText, page, size);
    }

    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "审核需求信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/examineData")
    public Y9Result<String> examineData(String id, Integer examine) {
        return dataDemandService.examineData(id, examine);
    }

    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "停止需求信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/stopData")
    public Y9Result<String> stopData(String id) {
        return dataDemandService.stopData(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存消息信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveText")
    public Y9Result<String> saveText(String demandId, String text) {
        return dataDemandService.saveText(demandId, text);
    }

    @RiseLog(operationName = "获取需求回复列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping("/getAskList")
    public Y9Result<List<Map<String, Object>>> getAskList(String demandId) {
        return dataDemandService.getAskList(demandId);
    }

    @RiseLog(operationName = "分页获取参与列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping("/searchPage3")
    public Y9Page<Map<String, Object>> searchPage3(String searchText, Integer page, Integer size) {
        return dataDemandService.searchPage3(searchText, page, size);
    }

    @RiseLog(operationName = "获取个人需求列表", logLevel = LogLevelEnum.RSLOG, enable = false)
    @GetMapping("/searchDemandPage")
    public Y9Page<DataDemandEntity2> searchDemandPage(String searchText, String pageType, Integer page, Integer size) {
        return dataDemandService.searchDemandPage(searchText, pageType, page, size);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存需求信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveData2")
    public Y9Result<String> saveData2(DataDemandEntity2 dataDemandEntity) {
        return dataDemandService.saveData2(dataDemandEntity);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除需求数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteData2")
    public Y9Result<String> deleteData2(@RequestParam String id) {
        return dataDemandService.deleteData2(id);
    }
}
