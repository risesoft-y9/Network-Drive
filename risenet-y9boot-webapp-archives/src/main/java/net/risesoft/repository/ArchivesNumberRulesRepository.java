package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ArchivesNumberRules;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ArchivesNumberRulesRepository
    extends JpaRepository<ArchivesNumberRules, String>, JpaSpecificationExecutor<ArchivesNumberRules> {

    List<ArchivesNumberRules> findByCategoryMark(String categoryMark);
}
