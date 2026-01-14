package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.DataRecentEntity;

public interface DataRecentRepository extends JpaRepository<DataRecentEntity, String>, JpaSpecificationExecutor<DataRecentEntity> {

}
