package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.DataAssetsNumberRules;

public interface DataAssetsNumberRulesService {

    DataAssetsNumberRules save(DataAssetsNumberRules dataAssetsNumberRules);

    List<DataAssetsNumberRules> findByCategoryMark(String categoryMark);

    void deleteByCategoryMark(String categoryMark);
}
