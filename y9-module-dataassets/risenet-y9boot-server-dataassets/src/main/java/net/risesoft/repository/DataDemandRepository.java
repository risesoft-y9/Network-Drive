package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.DataDemandEntity;

public interface DataDemandRepository
    extends JpaRepository<DataDemandEntity, String>, JpaSpecificationExecutor<DataDemandEntity> {

    Page<DataDemandEntity> findByIdInAndTitleContainingAndExamine(List<String> id, String title, Integer examine,
        Pageable pageable);

    Page<DataDemandEntity> findByIdInAndExamine(List<String> id, Integer examine, Pageable pageable);

}
