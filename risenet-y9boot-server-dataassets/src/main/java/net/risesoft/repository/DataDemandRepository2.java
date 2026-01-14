package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.DataDemandEntity2;

public interface DataDemandRepository2
    extends JpaRepository<DataDemandEntity2, String>, JpaSpecificationExecutor<DataDemandEntity2> {

    Page<DataDemandEntity2> findByTitleContainingAndPublisherId(String title, String publisherId, Pageable pageable);

    Page<DataDemandEntity2> findByTitleContaining(String title, Pageable pageable);

}
