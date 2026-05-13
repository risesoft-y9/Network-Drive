package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import net.risesoft.entity.LabelCatalogEntity;
import net.risesoft.entity.LabelDataEntity;
import net.risesoft.pojo.Y9Result;

public interface LabelService {

    /**
     * 查询目录列表
     */
    List<Map<String, Object>> getTree(String parentId, String searchName);

    /**
     * 根据ID获取数据
     * 
     * @return
     */
    LabelCatalogEntity getById(String id);

    /**
     * 根据id删除数据
     * 
     */
    Y9Result<String> deleteData(String id);

    /**
     * 保存数据
     * 
     * @param entity
     * @return
     */
    Y9Result<LabelCatalogEntity> saveData(LabelCatalogEntity entity);

    /**
     * 保存标签数据
     * 
     * @param labelDataEntity
     * @return
     */
    Y9Result<String> saveLabelData(LabelDataEntity labelDataEntity);

    /**
     * 删除标签数据
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteLabelData(String id);

    /**
     * 分页获取标签
     * 
     * @param parentId
     * @param page
     * @param size
     * @return
     */
    Page<LabelDataEntity> searchPage(String parentId, int page, int size);

    /**
     * 获取标签列表
     * 
     * @return
     */
    Y9Result<List<Map<String, Object>>> getLabelDataList();

    /**
     * 保存资产标注信息
     * 
     * @param ids
     * @param assetsId
     * @return
     */
    Y9Result<String> saveAssetsLabel(String ids, Long assetsId);

    /**
     * 获取资产标注
     * 
     * @param assetsId
     * @return
     */
    Y9Result<List<String>> getLabels(Long assetsId);

    String getLabelData(Long assetsId);

}
