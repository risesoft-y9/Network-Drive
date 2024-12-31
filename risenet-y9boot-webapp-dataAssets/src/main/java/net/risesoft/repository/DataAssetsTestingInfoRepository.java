package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataAssetsTestingInfo;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataAssetsTestingInfoRepository
    extends JpaRepository<DataAssetsTestingInfo, String>, JpaSpecificationExecutor<DataAssetsTestingInfo> {

}
