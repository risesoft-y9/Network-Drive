package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.SubscribeEntity;

public interface SubscribeRepository extends JpaRepository<SubscribeEntity, String>, JpaSpecificationExecutor<SubscribeEntity> {
	
	Page<SubscribeEntity> findByUserIdAndAssetsIdIn(String userId, List<Long> assetsId, Pageable pageable);
	
	Page<SubscribeEntity> findByAssetsIdIn(List<Long> assetsId, Pageable pageable);
	
	SubscribeEntity findByAssetsIdAndProvideTypeAndUserId(Long assetsId, String provideType, String userId);

}
