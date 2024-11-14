package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.VideoFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface VideoFileRepository extends JpaRepository<VideoFile, Long>, JpaSpecificationExecutor<VideoFile> {

    VideoFile findByDetailId(String detailId);
}
