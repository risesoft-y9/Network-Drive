package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.DictionaryOption;
import net.risesoft.entity.DictionaryValue;
import net.risesoft.pojo.Y9Result;

public interface DictionaryOptionService {

    /**
     * 删除数据字典
     *
     * @param type
     * @return
     */
    Y9Result<String> delOptionClass(String type);

    /**
     * 删除数据字典值
     *
     * @param id
     * @return
     */
    Y9Result<String> delOptionValue(String id);

    /**
     * 获取指定的数据字典值
     *
     * @param id
     * @return
     */
    DictionaryValue findById(String id);

    /**
     * 获取指定类型数据
     *
     * @param type
     * @return
     */
    DictionaryOption findByType(String type);

    /**
     * 获取所有数据字典类型
     *
     * @return
     */
    List<DictionaryOption> listAllOptionClass();

    /**
     * 获取所有数据字典值
     *
     * @return
     */
    List<DictionaryValue> listAllOptionValue();

    /**
     * 获取数据字典列表
     *
     * @param name
     * @return
     */
    List<DictionaryOption> listByName(String name);

    /**
     * 获取数据字典值
     *
     * @param type
     * @return
     */
    List<DictionaryValue> listByTypeOrderByTabIndexAsc(String type);

    /**
     * 保存数据字典
     *
     * @param optionClass
     * @return
     */
    Y9Result<DictionaryOption> saveOptionClass(DictionaryOption optionClass);

    /**
     * 保存数据字典值
     *
     * @param optionValue
     * @return
     */
    Y9Result<DictionaryValue> saveOptionValue(DictionaryValue optionValue);

    /**
     * 保存排序
     *
     * @param ids
     * @return
     */
    Y9Result<String> saveOrder(String ids);

    /**
     * 设置默认选中
     *
     * @param id
     * @return
     */
    Y9Result<String> updateOptionValue(String id);

}
