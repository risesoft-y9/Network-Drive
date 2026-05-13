package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.LabelCatalogEntity;

public interface LabelCatalogRepository
    extends JpaRepository<LabelCatalogEntity, String>, JpaSpecificationExecutor<LabelCatalogEntity> {

    Page<LabelCatalogEntity> findByNameContaining(String name, Pageable pageable);

    List<LabelCatalogEntity> findByParentIdOrderByTabIndex(String parentId);

    @Query("select max(tabIndex) from LabelCatalogEntity p where p.parentId = ?1")
    Integer getMaxTabIndex(String parentId);

    LabelCatalogEntity findByNameAndParentId(String name, String parentId);

    List<LabelCatalogEntity> findByNameContaining(String name);

    @Transactional
    @Modifying
    @Query("delete from LabelCatalogEntity p where p.parentId =?1")
    void deleteByParentId(String parentId);

}
