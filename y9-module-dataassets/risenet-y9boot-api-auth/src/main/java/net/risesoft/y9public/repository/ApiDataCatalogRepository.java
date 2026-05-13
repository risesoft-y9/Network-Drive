package net.risesoft.y9public.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.y9public.entity.ApiDataCatalogEntity;

public interface ApiDataCatalogRepository
    extends JpaRepository<ApiDataCatalogEntity, String>, JpaSpecificationExecutor<ApiDataCatalogEntity> {

    Page<ApiDataCatalogEntity> findByNameContaining(String name, Pageable pageable);

    List<ApiDataCatalogEntity> findByParentIdOrderByTabIndex(String parentId);

    @Query("select max(tabIndex) from ApiDataCatalogEntity p where p.parentId = ?1")
    Integer getMaxTabIndex(String parentId);

    ApiDataCatalogEntity findByNameAndParentId(String name, String parentId);

    List<ApiDataCatalogEntity> findByNameContaining(String name);

    @Transactional
    @Modifying
    @Query("delete from ApiDataCatalogEntity p where p.parentId =?1")
    void deleteByParentId(String parentId);

}
