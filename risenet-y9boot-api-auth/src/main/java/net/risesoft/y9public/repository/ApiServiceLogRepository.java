package net.risesoft.y9public.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.y9public.entity.ApiServiceLogEntity;

public interface ApiServiceLogRepository
    extends JpaRepository<ApiServiceLogEntity, String>, JpaSpecificationExecutor<ApiServiceLogEntity> {

    Page<ApiServiceLogEntity> findByAppNameContaining(String appName, Pageable pageable);

    /**
     * 根据createTime模糊查询当天的数据量
     * 
     * @param createTime
     * @return
     */
    @Query("select count(a) from ApiServiceLogEntity a where a.createTime between ?1 and ?2")
    Long countByCreateTimeLike(Date start, Date end);

}
