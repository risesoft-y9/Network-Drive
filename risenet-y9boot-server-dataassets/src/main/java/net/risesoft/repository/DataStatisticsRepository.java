package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.DataStatisticsEntity;

public interface DataStatisticsRepository extends JpaRepository<DataStatisticsEntity, String>, JpaSpecificationExecutor<DataStatisticsEntity> {

    /**
     * 根据数据库ID和日期查询数据统计
     * 
     * @param sourceId 数据库ID
     * @param dataTime 日期
     * @return 数据统计实体
     */
    DataStatisticsEntity findBySourceIdAndDataTime(String sourceId, String dataTime);

}
