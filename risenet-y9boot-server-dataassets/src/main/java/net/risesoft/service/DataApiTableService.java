package net.risesoft.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.risesoft.entity.DataApiTableEntity;
import net.risesoft.entity.TableForeignKeyEntity;

/**
 * 接口资源申请表服务接口
 * 提供接口资源申请的CRUD操作
 */
public interface DataApiTableService {
    /**
     * 分页查询接口资源申请表
     * @param pageable 分页参数
     * @return 接口资源申请表分页列表
     */
    Page<DataApiTableEntity> findByTableName(String tableName, Pageable pageable);
    
    /**
     * 保存接口资源申请表
     * @param dataApiTableEntity 接口资源申请表实体
     * @return
     */
    String save(DataApiTableEntity dataApiTableEntity);
    
    /**
     * 根据ID删除接口资源申请表
     * @param id 接口资源申请表ID
     */
    void deleteById(Long id);
    
    /**
     * 根据ID查询接口资源申请表
     * @param id 接口资源申请表ID
     * @return 接口资源申请表实体
     */
    DataApiTableEntity findById(Long id);

    /**
     * 根据表名称获取外键信息
     * @param tableName 表名称
     * @param dataSourceId 数据源id
     * @return 外键信息列表
     */
    List<String> findForeignKeysByTableName(String tableName, String dataSourceId);
}