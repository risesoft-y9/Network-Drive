package net.risesoft.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DictionaryOption;
import net.risesoft.entity.DictionaryValue;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DictionaryOptionService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/dataDict", produces = MediaType.APPLICATION_JSON_VALUE)
public class DictionaryOptionRestController {

    private final DictionaryOptionService dictionaryOptionService;

    /**
     * 删除数据字典
     *
     * @param type 字典类型
     */
    @PostMapping(value = "/delOptionClass")
    public Y9Result<String> delOptionClass(String type) {
        return dictionaryOptionService.delOptionClass(type);
    }

    /**
     * 删除数据字典值
     *
     * @param id 主键id
     * @return Y9Result<String>
     */
    @PostMapping(value = "/delOptionValue")
    public Y9Result<String> delOptionValue(String id) {
        return dictionaryOptionService.delOptionValue(id);
    }

    /**
     * 获取数据字典
     *
     * @param type 字典类型
     */
    @GetMapping(value = "/getOptionClass")
    public Y9Result<DictionaryOption> getOptionClass(String type) {
        DictionaryOption dictionaryOption = dictionaryOptionService.findByType(type);
        return Y9Result.success(dictionaryOption, "获取成功");
    }

    /**
     * 获取数据字典列表
     *
     * @param name 数据字典名称
     */
    @GetMapping(value = "/getOptionClassList")
    public Y9Result<List<DictionaryOption>> getOptionClassList(@RequestParam(required = false) String name) {
        List<DictionaryOption> list = dictionaryOptionService.listByName(name);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 数据字典值
     *
     * @param id 主键id
     * @return Y9Result<Y9FormOptionValue>
     */
    @GetMapping(value = "/getOptionValue")
    public Y9Result<DictionaryValue> getOptionValue(String id) {
        DictionaryValue dictionaryValue = dictionaryOptionService.findById(id);
        return Y9Result.success(dictionaryValue, "获取成功");
    }

    /**
     * 获取数据字典值列表
     *
     * @param type 字典标识
     * @return Y9Result<List < Y9FormOptionValue>>
     */
    @GetMapping(value = "/getOptionValueList")
    public Y9Result<List<DictionaryValue>> getOptionValueList(String type) {
        List<DictionaryValue> list = dictionaryOptionService.listByTypeOrderByTabIndexAsc(type);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 保存数据字典
     *
     * @param dictionaryOption 字典类型数据
     * @return
     */
    @PostMapping(value = "/saveOptionClass")
    public Y9Result<DictionaryOption> saveOptionClass(DictionaryOption dictionaryOption) {
        return dictionaryOptionService.saveOptionClass(dictionaryOption);
    }

    /**
     * 保存数据字典值
     *
     * @param dictionaryValue 字典值数据
     * @return
     */
    @PostMapping(value = "/saveOptionValue")
    public Y9Result<DictionaryValue> saveOptionValue(DictionaryValue dictionaryValue) {
        return dictionaryOptionService.saveOptionValue(dictionaryValue);
    }

    /**
     * 保存排序
     *
     * @param ids 主键ids
     * @return
     */
    @PostMapping(value = "/saveOrder")
    public Y9Result<String> saveOrder(String ids) {
        return dictionaryOptionService.saveOrder(ids);
    }

    /**
     * 设置默认选中
     *
     * @param id 主键id
     * @return
     */
    @PostMapping(value = "/updateOptionValue")
    public Y9Result<String> updateOptionValue(String id) {
        return dictionaryOptionService.updateOptionValue(id);
    }

}
