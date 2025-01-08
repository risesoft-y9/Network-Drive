package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.DataAssets;
import net.risesoft.model.SearchPage;
import net.risesoft.pojo.Y9Result;

public interface DataAssetsService {

    SearchPage<DataAssets> listArchives(String categoryId, Integer fileStatus, Boolean isDeleted, int page, int rows);

    SearchPage<DataAssets> listArchivesByColumnNameAndValues(String categoryId, Integer fileStatus, Boolean isDeleted,
        String columnNameAndValues, int page, int rows);

    DataAssets save(DataAssets archives);

    DataAssets findById(Long id);

    void delete(String categoryId, Long[] id);

    void signDelete(String categoryId, Long[] id);

    void recordArchiving(Long[] ids);

    Y9Result<String> createAssetsNo(String categoryId, Long[] ids);

    List<DataAssets> findByDataAssetsIdIn(Long[] ids);

}
