package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.FileInfo;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface FileInfoRepository extends JpaRepository<FileInfo, Long>, JpaSpecificationExecutor<FileInfo> {

    Page<FileInfo> findByAssetsId(Long assetsId, Pageable pageable);

    Page<FileInfo> findByAssetsIdAndNameContains(Long assetsId, String name, Pageable pageable);

    long countByAssetsId(Long assetsId);

    List<FileInfo> findByAssetsIdAndFileType(Long assetsId, String fileType);

    List<FileInfo> findByAssetsIdAndIdentifierAndFileType(Long assetsId, String identifier, String fileType);

    long countByFileType(String fileType);
}
