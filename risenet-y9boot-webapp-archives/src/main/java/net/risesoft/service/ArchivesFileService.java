package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.ArchivesFile;

public interface ArchivesFileService {

    ArchivesFile save(ArchivesFile archivesFile);

    boolean isArchivesFileExists(Long archiveId, String fileName);

    Integer getMaxTabIndex(Long archiveId);

    List<ArchivesFile> findByArchivesId(Long archivesId);

    void deleteFile(String id);

    ArchivesFile findById(String id);
}
