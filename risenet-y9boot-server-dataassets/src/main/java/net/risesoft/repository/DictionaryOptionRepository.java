package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DictionaryOption;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DictionaryOptionRepository
    extends JpaRepository<DictionaryOption, String>, JpaSpecificationExecutor<DictionaryOption> {

    List<DictionaryOption> findByNameContaining(String name);

    DictionaryOption findByType(String type);

}
