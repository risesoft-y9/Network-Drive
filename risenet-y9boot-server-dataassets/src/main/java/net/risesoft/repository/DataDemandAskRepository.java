package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DataDemandAskEntity;

public interface DataDemandAskRepository extends JpaRepository<DataDemandAskEntity, String>, JpaSpecificationExecutor<DataDemandAskEntity> {
	
	@Transactional
	@Modifying
	@Query("delete from DataDemandAskEntity p where p.demandId =?1")
	void deleteByDemandId(String demandId);
	
	List<DataDemandAskEntity> findByDemandIdAndPublisherIdOrderByCreateTime(String demandId, String publisherId);
	
	List<DataDemandAskEntity> findByDemandIdOrderByCreateTime(String demandId);
	
	@Query("select distinct(p.demandId) from DataDemandAskEntity p where p.publisherId = ?1")
	List<String> findByPublisherId(String publisherId);

}
