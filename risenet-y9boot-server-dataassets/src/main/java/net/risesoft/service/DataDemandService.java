package net.risesoft.service;

import java.util.List;
import java.util.Map;

import net.risesoft.entity.DataDemandEntity;
import net.risesoft.entity.DataDemandEntity2;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

public interface DataDemandService {

    /**
     * 保存数据
     * 
     * @return
     */
    Y9Result<String> saveData(DataDemandEntity dataDemandEntity);

    /**
     * 删除数据
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteData(String id);

    /**
     * 分页查询
     * 
     * @param searchText
     * @param dataType
     * @param industry
     * @param budget
     * @param status
     * @param page
     * @param size
     * @return
     */
    Y9Page<Map<String, Object>> searchPage(String searchText, Integer dataType, Integer industry, Integer budget,
        Integer status, Integer sort, int page, int size);

    /**
     * 根据id获取需求信息
     * 
     * @param id
     * @return
     */
    Y9Result<DataDemandEntity> getById(String id);

    /**
     * 我的需求列表-管理员查所有
     * 
     * @param searchText
     * @param page
     * @param size
     * @return
     */
    Y9Page<Map<String, Object>> searchPage2(String searchText, int page, int size);

    /**
     * 审核需求
     * 
     * @param id
     * @param examine
     * @return
     */
    Y9Result<String> examineData(String id, Integer examine);

    /**
     * 停止发布的需求
     * 
     * @param id
     * @return
     */
    Y9Result<String> stopData(String id);

    /**
     * 保存留言信息
     * 
     * @param text
     * @return
     */
    Y9Result<String> saveText(String demandId, String text);

    /**
     * 获取需求的个人消息信息
     * 
     * @param demandId
     * @return
     */
    Y9Result<List<Map<String, Object>>> getAskList(String demandId);

    /**
     * 我的参与列表
     * 
     * @param searchText
     * @param page
     * @param size
     * @return
     */
    Y9Page<Map<String, Object>> searchPage3(String searchText, int page, int size);


    /**
     * 保存数据
     * 
     * @return
     */
    Y9Result<String> saveData2(DataDemandEntity2 dataDemandEntity);

    /**
     * 删除数据
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteData2(String id);

    /**
     * 分页查询
     * 
     * @param searchText
     * @param page
     * @param size
     * @return
     */
    Y9Page<DataDemandEntity2> searchDemandPage(String searchText, String pageType, int page, int size);

}
