package net.risesoft.repository;

import net.risesoft.entity.DataApiOnlineInfoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataApiOnlineInfoRepository extends JpaRepository<DataApiOnlineInfoEntity, String>, JpaSpecificationExecutor<DataApiOnlineInfoEntity> {
	
}
