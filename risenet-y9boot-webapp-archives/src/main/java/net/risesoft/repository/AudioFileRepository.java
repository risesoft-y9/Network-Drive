package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.AudioFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface AudioFileRepository extends JpaRepository<AudioFile, Long>, JpaSpecificationExecutor<AudioFile> {

    AudioFile findByDetailId(Long detailId);

    void deleteByDetailId(Long detailId);
}
