package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.CategoryTableField;

public interface CategoryTableFieldService {

    /**
     * 根据id删除表字段定义
     *
     * @param id
     * @return
     */
    void delete(String id);

    /**
     * 根据id获取表字段定义
     *
     * @param id
     * @return
     */
    CategoryTableField findById(String id);

    /**
     * 根据表id获取表字段定义
     *
     * @param tableId
     * @return
     */
    List<CategoryTableField> listByTableId(String tableId);

    /**
     * 保存表字段信息
     *
     * @param field
     * @return
     */
    CategoryTableField saveOrUpdate(CategoryTableField field);

    /**
     * 修改字段状态
     *
     * @param tableId
     */
    void updateState(String tableId);

}
