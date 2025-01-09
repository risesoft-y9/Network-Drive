package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataAssets;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataAssetsRepository extends JpaRepository<DataAssets, Long>, JpaSpecificationExecutor<DataAssets> {

    Page<DataAssets> findByCategoryIdAndAssetsStatusAndIsDeleted(String categoryId, Integer status, Boolean isDeleted,
        Pageable pageable);

    List<DataAssets> findByDataassetsIdIn(Long[] ids);
}
