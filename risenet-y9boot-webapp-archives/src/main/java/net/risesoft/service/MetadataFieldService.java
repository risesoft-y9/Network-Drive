package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.MetadataField;

public interface MetadataFieldService {

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
    MetadataField findById(String id);

    /**
     * 根据表id获取表字段定义
     *
     * @param tableId
     * @return
     */
    List<MetadataField> listByTableId(String tableId);

    /**
     * 取出当前表定义字段
     *
     * @param tableId
     * @param state 字段状态 ：-1未生成表字段，1为已经生成表字段
     * @return
     */
    List<MetadataField> listByTableIdAndState(String tableId, Integer state);

    /**
     * 取出当前表定义字段
     *
     * @param tableId
     * @return
     */
    List<MetadataField> listByTableIdOrderByDisplay(String tableId);

    /**
     * 获取字段系统主键
     *
     * @param tableId
     * @return
     */
    List<MetadataField> listSystemFieldByTableId(String tableId);

    /**
     * 保存表字段信息
     *
     * @param field
     * @return
     */
    MetadataField saveOrUpdate(MetadataField field);

    /**
     * 修改字段状态
     *
     * @param tableId
     */
    void updateState(String tableId);

}
