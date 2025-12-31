package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataAssets;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataAssetsRepository extends JpaRepository<DataAssets, Long>, JpaSpecificationExecutor<DataAssets> {

    Page<DataAssets> findByCategoryIdAndIsDeleted(String categoryId, Boolean isDeleted, Pageable pageable);

    List<DataAssets> findByIdIn(Long[] ids);

    @Query("select max(orderNum) from DataAssets p where p.categoryId = ?1")
    Integer getMaxOrderNum(String categoryId);

    @Query("select max(code) from DataAssets p where p.categoryId = ?1")
    String getMaxCode(String categoryId);

    DataAssets findByCodeGlobalAndIsDeleted(String codeGlobal, Boolean isDeleted);
    
    @Query("select p.id from DataAssets p where p.name like ?1")
    List<Long> findIdByNameLike(String name);
}
