package net.risesoft.y9public.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.y9public.entity.ApiServiceLogEntity;

public interface ApiServiceLogRepository
    extends JpaRepository<ApiServiceLogEntity, String>, JpaSpecificationExecutor<ApiServiceLogEntity> {

    Page<ApiServiceLogEntity> findByAppNameContaining(String appName, Pageable pageable);

}
