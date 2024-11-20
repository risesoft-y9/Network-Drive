package net.risesoft.service;

import net.risesoft.entity.ArchivesFile;

public interface ArchivesFileService {

    ArchivesFile save(ArchivesFile archivesFile);

    boolean isArchivesFileExists(Long archiveId, String fileName);

    Integer getMaxTabIndex(Long archiveId);
}
