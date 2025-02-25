package net.risesoft.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataSourceService;
import net.risesoft.util.db.DbMetaDataUtil;

@Validated
@RestController
@RequestMapping(value = "/vue/source", produces = "application/json")
@RequiredArgsConstructor
public class SourceController {

	private final DataSourceService dataSourceService;
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据源分类列表", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/findCategoryAll")
    public Y9Result<List<DataSourceTypeEntity>> findCategoryAll() {
        List<DataSourceTypeEntity> list = dataSourceService.findDataCategory();
        return Y9Result.success(list, "获取成功");
    }
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据类别获取数据源列表", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/findByBaseType")
    public Y9Result<List<DataSourceEntity>> findByBaseType(@RequestParam String category) {
        List<DataSourceEntity> list = dataSourceService.findByBaseType(category);
        list.stream().map((item) -> {
        	if(StringUtils.isNotBlank(item.getPassword())) {
        		item.setPassword("******");
        	}
            return item;
        }).collect(Collectors.toList());
        return Y9Result.success(list, "获取成功");
    }
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据源列表", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/findByType")
    public Y9Result<List<DataSourceEntity>> findByType(Integer type) {
        return Y9Result.success(dataSourceService.findByType(type), "获取成功");
    }
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "搜索数据源列表", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/searchSource")
    public Y9Result<List<Map<String, Object>>> searchSource(String baseName) {
        return Y9Result.success(dataSourceService.searchSource(baseName), "获取成功");
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
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据id获取数据源信息", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/getDataSource")
	public Y9Result<DataSourceEntity> getDataSource(String id) {
		DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(id);
		if(dataSourceEntity != null && StringUtils.isNotBlank(dataSourceEntity.getPassword())) {
			dataSourceEntity.setPassword("******");
		}
		return Y9Result.success(dataSourceEntity, "获取成功");
	}
	
	@RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除数据源", logLevel = LogLevelEnum.RSLOG)
	@PostMapping(value = "/deleteSource")
	public Y9Result<String> deleteSource(@RequestParam String id) {
		return dataSourceService.deleteDataSource(id);
	}
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取数据库需要提取的表", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping("/getNotExtractList")
    public Map<String, Object> getNotExtractList(String baseId, String tableName) {
		Map<String, Object> map = dataSourceService.getNotExtractList(baseId, tableName);
		return map;
	}
	
	@RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "检测数据源状态", logLevel = LogLevelEnum.RSLOG, enable = false)
	@GetMapping(value = "/checkStatus")
	public Y9Result<Boolean> checkStatus(String sourceId) {
		DataSourceEntity source = dataSourceService.getDataSourceById(sourceId);
		if(source != null && source.getType() == 0) {
			return Y9Result.success(DbMetaDataUtil.getConnection(source.getDriver(), source.getUsername(), source.getPassword(), source.getUrl()));
		}
		return Y9Result.success(true);
	}
	
}
