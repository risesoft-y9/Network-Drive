package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.DataAssetsFile;

public interface DataAssetsFileService {

    DataAssetsFile save(DataAssetsFile dataAssetsFile);

    boolean isArchivesFileExists(Long detailId, String fileName);

    Integer getMaxTabIndex(Long detailId);

    List<DataAssetsFile> findByDetailId(Long detailId);

    void deleteFile(String id);

    DataAssetsFile findById(String id);
}
