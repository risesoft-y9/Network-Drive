package net.risesoft.repository;

import net.risesoft.entity.DataApiOnlineEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface DataApiOnlineRepository extends JpaRepository<DataApiOnlineEntity, String>, JpaSpecificationExecutor<DataApiOnlineEntity> {
	
	List<DataApiOnlineEntity> findByParentIdOrderByCreateTime(String parentId);
	
	List<DataApiOnlineEntity> findByParentIdAndCreatorIdOrderByCreateTime(String parentId, String creatorId);
	
	@Transactional
	@Modifying
	@Query("delete from DataApiOnlineEntity p where p.parentId = ?1")
	void deleteByParentId(String parentId);
	
	@Query("select p.id from DataApiOnlineEntity p where p.parentId = ?1")
	List<String> findByParentId(String parentId);
	
}
