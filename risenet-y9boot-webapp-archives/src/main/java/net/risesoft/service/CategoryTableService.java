package net.risesoft.service;

import java.util.List;
import java.util.Map;

import net.risesoft.entity.CategoryTable;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface CategoryTableService {

    /**
     * Description: 查询当前门类是否存在表，存在就查询表信息
     * 
     * @param categoryMark
     * @return
     */
    CategoryTable checkCategoryIsExistTable(String categoryMark);

    /**
     * Description: 添加数据库表
     *
     * @param tableName
     * @param systemName
     * @param systemCnName
     * @return
     */
    Y9Result<Object> addDataBaseTable(String tableName, String systemName, String systemCnName);

    /**
     * 根据表PK创建相应的表
     *
     * @param table
     * @param listMap
     * @return
     */
    Y9Result<Object> buildTable(CategoryTable table, List<Map<String, Object>> listMap);

    /**
     * 根据id删除业务表
     *
     * @param id
     * @return
     */
    Y9Result<Object> delete(String id);

    /**
     * 根据id获取业务表信息
     *
     * @param id
     * @return
     */
    CategoryTable findById(String id);

    /**
     * 获取所有表名称
     *
     * @return
     */
    String getAlltableName();

    /**
     * Description: 获取数据库表
     *
     * @param name
     * @return
     */
    List<Map<String, String>> getAllTables(String name);

    /**
     * Description:
     *
     * @param table
     * @return
     * @throws Exception
     */
    CategoryTable saveOrUpdate(CategoryTable table) throws Exception;

    /**
     * Description: 修改表结构，增加字段
     *
     * @param table
     * @param listMap
     * @param type
     * @return
     */
    Y9Result<Object> updateTable(CategoryTable table, List<Map<String, Object>> listMap, String type);

}
