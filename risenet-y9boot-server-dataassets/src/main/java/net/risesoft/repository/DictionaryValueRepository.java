package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DictionaryValue;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DictionaryValueRepository extends JpaRepository<DictionaryValue, String>, JpaSpecificationExecutor<DictionaryValue> {

    @Modifying
    @Transactional(readOnly = false)
    void deleteByType(String type);

    List<DictionaryValue> findByTypeOrderByTabIndexAsc(String type);

    @Query("select max(tabIndex) from DictionaryValue where type = ?1")
    Integer getMaxTabIndex(String type);
    
    DictionaryValue findByCodeAndType(String code, String type);

}
