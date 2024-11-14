package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DocumentFile;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DocumentFileRepository
    extends JpaRepository<DocumentFile, Long>, JpaSpecificationExecutor<DocumentFile> {

    DocumentFile findByDetailId(String detailId);
}
