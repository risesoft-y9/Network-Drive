package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.CategoryTable;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface CategoryTableRepository
    extends JpaRepository<CategoryTable, String>, JpaSpecificationExecutor<CategoryTable> {

    List<CategoryTable> findByCategoryMark(String categoryMark);
}
