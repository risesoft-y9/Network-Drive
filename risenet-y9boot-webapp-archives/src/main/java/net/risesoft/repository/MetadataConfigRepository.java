package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.MetadataConfig;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface MetadataConfigRepository
    extends JpaRepository<MetadataConfig, String>, JpaSpecificationExecutor<MetadataConfig> {

    @Modifying
    @Transactional(readOnly = false)
    void deleteByViewType(String viewType);

    @Query("SELECT MAX(tabIndex) FROM MetadataConfig WHERE viewType =?1")
    Integer getMaxTabIndex(String viewType);

    MetadataConfig findByColumnName(String columnName);

    MetadataConfig findByViewTypeAndTableFieldId(String viewType, String tableField);

    MetadataConfig findByViewTypeAndColumnName(String viewType, String columnName);

    Page<MetadataConfig> findByViewType(String viewType, Pageable pageable);

    List<MetadataConfig> findByViewType(String viewType);

    @Modifying
    @Transactional(readOnly = false)
    @Query("update MetadataConfig t set t.tabIndex=?1 where t.id=?2")
    void updateOrder(Integer tabIndex, String id);
}
