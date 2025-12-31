package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataApiTableEntity;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataApiTableRepository extends JpaRepository<DataApiTableEntity, Long>, JpaSpecificationExecutor<DataApiTableEntity> {

    Page<DataApiTableEntity> findByTenantIdAndCreatorId(String tenantId, String creatorId, Pageable pageable);

    Page<DataApiTableEntity> findByTableNameContainingAndTenantIdAndCreatorId(String tableName, String tenantId, String creatorId, Pageable pageable);

    Page<DataApiTableEntity> findByTenantId(String tenantId, Pageable pageable);

    Page<DataApiTableEntity> findByTableNameContainingAndTenantId(String tableName, String tenantId, Pageable pageable);

    DataApiTableEntity findByTableNameAndDataSourceIdAndOwner(String tableName, String dataSourceId, String owner);

    DataApiTableEntity findByTableNameAndDataSourceIdAndCreatorIdAndIsDeleted(String tableName, String dataSourceId, String creatorId, Boolean isDeleted);
}
