package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataAssetsNumberRules;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataAssetsNumberRulesRepository
    extends JpaRepository<DataAssetsNumberRules, String>, JpaSpecificationExecutor<DataAssetsNumberRules> {

    List<DataAssetsNumberRules> findByCategoryMark(String categoryMark);
}
