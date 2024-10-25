package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.MetadataConfig;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface MetadataConfigRepository
    extends JpaRepository<MetadataConfig, String>, JpaSpecificationExecutor<MetadataConfig> {

    @Query("SELECT MAX(tabIndex) FROM MetadataConfig")
    Integer getMaxTabIndex();

    MetadataConfig findByColumnName(String columnName);

    @Modifying
    @Transactional(readOnly = false)
    @Query("update MetadataConfig t set t.tabIndex=?1 where t.id=?2")
    void updateOrder(Integer tabIndex, String id);
}
