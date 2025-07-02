package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.CategoryTableField;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface CategoryTableFieldRepository
    extends JpaRepository<CategoryTableField, String>, JpaSpecificationExecutor<CategoryTableField> {

    @Modifying
    @Transactional(readOnly = false)
    @Query("delete from CategoryTableField t where t.tableId =?1 ")
    void deleteByTableId(String id);

    List<CategoryTableField> findByTableIdAndIsSystemFieldOrderByDisplayOrderAsc(String tableId, Integer isSystemField);

    List<CategoryTableField> findByTableIdOrderByDisplayOrderAsc(String tableId);

    @Query("select max(t.displayOrder) from CategoryTableField t where t.tableId = ?1")
    Integer getMaxDisplayOrder(String tableId);

    @Modifying
    @Transactional(readOnly = false)
    @Query("update CategoryTableField t set t.oldFieldName=?2 where t.tableName =?1 and t.fieldName =?2")
    void updateOldFieldName(String tableName, String columnName);

    @Modifying
    @Transactional(readOnly = false)
    @Query("update CategoryTableField t set t.state=1 where t.tableId =?1")
    void updateState(String tableId);

}
