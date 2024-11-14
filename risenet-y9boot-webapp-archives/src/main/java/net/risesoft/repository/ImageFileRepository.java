package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ImageFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ImageFileRepository extends JpaRepository<ImageFile, Long>, JpaSpecificationExecutor<ImageFile> {

    ImageFile findByDetailId(String detailId);
}
