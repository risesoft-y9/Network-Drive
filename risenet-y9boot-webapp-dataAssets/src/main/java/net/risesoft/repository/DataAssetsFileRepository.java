package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataAssetsFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataAssetsFileRepository
    extends JpaRepository<DataAssetsFile, String>, JpaSpecificationExecutor<DataAssetsFile> {

    DataAssetsFile findByArchivesIdAndFileName(Long archiveId, String fileName);

    @Query("SELECT MAX(a.tabIndex) FROM DataAssetsFile a WHERE a.archivesId = ?1")
    Integer getMaxTabIndex(Long archiveId);

    List<DataAssetsFile> findByArchivesId(Long archivesId);
}
