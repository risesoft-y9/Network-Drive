package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.DataSourceEntity;

public interface DataSourceRepository
    extends JpaRepository<DataSourceEntity, String>, JpaSpecificationExecutor<DataSourceEntity> {

    List<DataSourceEntity> findByNameContainingAndBaseTypeAndTenantId(String baseName, String baseType,
        String tenantId);

    List<DataSourceEntity> findByNameContainingAndBaseTypeAndTenantIdAndUserId(String baseName, String baseType,
        String tenantId, String userId);

    List<DataSourceEntity> findByBaseTypeAndTenantIdOrderByCreateTime(String baseType, String tenantId);

    List<DataSourceEntity> findByBaseTypeAndTenantIdAndUserIdOrderByCreateTime(String baseType, String tenantId,
        String userId);

    long countByBaseType(String baseType);

    @Query("select p.id from DataSourceEntity p where p.tenantId = ?1")
    List<String> findIdByTenantId(String tenantId);

    List<DataSourceEntity> findByTenantId(String tenantId);

}
