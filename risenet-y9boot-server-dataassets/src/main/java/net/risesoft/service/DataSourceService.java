package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataSourceTypeEntity;
import net.risesoft.pojo.Y9Result;

public interface DataSourceService {

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
     * 根据类别获取数据源列表
     * 
     * @param baseType
     * @return
     */
    List<DataSourceEntity> findByBaseType(String baseType);

    /**
     * 搜索数据源
     * 
     * @param baseName
     * @return
     */
    List<Map<String, Object>> searchSource(String baseName);

    /**
     * 获取数据源分类列表
     * 
     * @return
     */
    List<DataSourceTypeEntity> findDataCategory();

    /**
     * 保存分类信息
     * 
     * @param entity
     * @return
     */
    Y9Result<DataSourceTypeEntity> saveDataCategory(MultipartFile iconFile, DataSourceTypeEntity entity);

    /**
     * 删除数据源分类
     * 
     * @param id
     */
    Y9Result<String> deleteCategory(String id);

    /**
     * 获取数据库下拉选择框数据
     * 
     * @param type
     * @return
     */
    List<Map<String, Object>> getTableSelectTree(String type);

    /**
     * 获取数据库表列表
     * 
     * @param id
     * @param name
     * @return
     */
    List<Map<String, Object>> getTablePage(String id, String name);

    /**
     * 获取数据库列表-不分权限
     * 
     * @return
     */
    List<Map<String, Object>> getDataBase();

    /**
     * 根据资产id获取挂接的数据库
     * 
     * @param assetsId
     * @return
     */
    List<Map<String, Object>> getDataSourceByAssetsId(Long assetsId);

    /**
     * 根据资产id获取挂接的数据表
     * 
     * @param assetsId ：资产id
     * @param identifier ：数据源id
     * @return
     */
    List<String> getTableByAssetsId(Long assetsId, String identifier);

}
