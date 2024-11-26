package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ArchivesFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ArchivesFileRepository
    extends JpaRepository<ArchivesFile, String>, JpaSpecificationExecutor<ArchivesFile> {

    ArchivesFile findByArchivesIdAndFileName(Long archiveId, String fileName);

    @Query("SELECT MAX(a.tabIndex) FROM ArchivesFile a WHERE a.archivesId = ?1")
    Integer getMaxTabIndex(Long archiveId);

    List<ArchivesFile> findByArchivesId(Long archivesId);
}
