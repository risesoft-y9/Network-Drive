package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.DownloadLogEntity;

public interface DownloadLogRepository
    extends JpaRepository<DownloadLogEntity, String>, JpaSpecificationExecutor<DownloadLogEntity> {

}
