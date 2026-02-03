package net.risesoft.repository;

import net.risesoft.entity.DownloadLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DownloadLogRepository extends JpaRepository<DownloadLogEntity, String>, JpaSpecificationExecutor<DownloadLogEntity> {

}
