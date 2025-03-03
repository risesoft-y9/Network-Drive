package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.LabelDataEntity;

public interface LabelDataRepository extends JpaRepository<LabelDataEntity, String>, JpaSpecificationExecutor<LabelDataEntity> {
	
	Page<LabelDataEntity> findByParentId(String parentId, Pageable pageable);
	
	@Query("select max(tabIndex) from LabelDataEntity p where p.parentId = ?1")
	Integer getMaxTabIndex(String parentId);
	
	@Transactional
	@Modifying
	@Query("delete from LabelDataEntity p where p.parentId =?1")
	void deleteByParentId(String parentId);
	
	long countByParentId(String parentId);
	
}
