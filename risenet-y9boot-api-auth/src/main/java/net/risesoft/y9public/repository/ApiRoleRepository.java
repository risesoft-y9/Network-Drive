package net.risesoft.y9public.repository;

import net.risesoft.y9public.entity.ApiRoleEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApiRoleRepository extends JpaRepository<ApiRoleEntity, String>, JpaSpecificationExecutor<ApiRoleEntity> {
	
	Page<ApiRoleEntity> findByAppNameContaining(String appName, Pageable pageable);
	
	ApiRoleEntity findByAppKey(String appKey);
	
}
