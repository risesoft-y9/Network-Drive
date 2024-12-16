package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.Archives;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ArchivesRepository extends JpaRepository<Archives, Long>, JpaSpecificationExecutor<Archives> {

    Page<Archives> findByCategoryIdAndFileStatusAndIsDeleted(String categoryId, Integer fileStatus, Boolean isDeleted,
        Pageable pageable);

    List<Archives> findByArchivesIdIn(Long[] ids);
}
