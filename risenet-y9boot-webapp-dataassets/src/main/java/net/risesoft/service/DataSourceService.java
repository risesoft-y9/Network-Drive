package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.pojo.Y9Result;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.entity.DataSourceEntity;

public interface DataSourceService {

	/**
	 * 查询数据源分页列表
	 */
	Page<DataSourceEntity> getDataSourcePage(String baseName, int page, int rows);

	/**
	 * 保存数据源实体类
	 * 
	 */
	DataSourceEntity saveDataSource(DataSourceEntity entity);

	/**
	 * 根据ID获取数据源
	 * 
	 * @param id ：数据源主键id
	 * @return
	 */
	DataSourceEntity getDataSourceById(String id);

	/**
	 * 根据id删除数据源
	 * 
	 * @param id ：数据源主键id
	 */
	Y9Result<String> deleteDataSource(String id);

	/**
	 * 根据数据源名称查询
	 * @param baseName
	 * @return
	 */
	DataSourceEntity findByBaseName(String baseName);
	
	/**
	 * 根据类别获取数据源列表
	 * @param baseType
	 * @return
	 */
	List<DataSourceEntity> findByBaseType(String baseType);
	
	/**
	 * 获取数据源列表：1-获取所有，0-获取非表的数据源
	 * @return
	 */
	List<DataSourceEntity> findByType(Integer type);
	
	/**
	 * 搜索数据源
	 * @param baseName
	 * @return
	 */
	List<Map<String, Object>> searchSource(String baseName);
	
	/**
	 * 根据数据源获取需要提取的表
	 * @param baseId
	 * @param tableName
	 * @return
	 */
	Map<String, Object> getNotExtractList(String baseId, String tableName);
	
	/**
	 * 获取数据源分类列表
	 * @return
	 */
	List<DataSourceTypeEntity> findDataCategory();
	
	/**
	 * 保存分类信息
	 * @param entity
	 * @return
	 */
	Y9Result<DataSourceTypeEntity> saveDataCategory(MultipartFile iconFile, DataSourceTypeEntity entity);
	
	/**
	 * 删除数据源分类
	 * @param id
	 */
	Y9Result<String> deleteCategory(String id);

}
