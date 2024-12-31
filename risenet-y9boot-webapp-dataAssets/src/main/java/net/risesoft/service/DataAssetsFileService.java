package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.DataAssetsFile;

public interface DataAssetsFileService {

    DataAssetsFile save(DataAssetsFile archivesFile);

    boolean isArchivesFileExists(Long archiveId, String fileName);

    Integer getMaxTabIndex(Long archiveId);

    List<DataAssetsFile> findByArchivesId(Long archivesId);

    void deleteFile(String id);

    DataAssetsFile findById(String id);
}
