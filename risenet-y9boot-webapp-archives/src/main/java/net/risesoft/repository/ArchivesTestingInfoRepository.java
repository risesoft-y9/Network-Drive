package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ArchivesTestingInfo;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ArchivesTestingInfoRepository
    extends JpaRepository<ArchivesTestingInfo, String>, JpaSpecificationExecutor<ArchivesTestingInfo> {

}
