package net.risesoft.y9public.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.y9public.entity.ApiServiceEntity;

public interface ApiServiceRepository
    extends JpaRepository<ApiServiceEntity, String>, JpaSpecificationExecutor<ApiServiceEntity> {

    Page<ApiServiceEntity> findByParentIdAndApiNameContaining(String parentId, String apiName, Pageable pageable);

    long countByParentId(String parentId);

    List<ApiServiceEntity> findByApiType(Integer apiType);

}
