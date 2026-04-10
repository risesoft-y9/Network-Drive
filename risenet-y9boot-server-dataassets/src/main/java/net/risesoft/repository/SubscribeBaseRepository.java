package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.SubscribeBaseEntity;

public interface SubscribeBaseRepository
    extends JpaRepository<SubscribeBaseEntity, String>, JpaSpecificationExecutor<SubscribeBaseEntity> {

    SubscribeBaseEntity findBySubscribeId(String subscribeId);

    @Query("select s from SubscribeBaseEntity s where s.userId = :userId and (s.rawData = '' or s.rawData is null)")
    List<SubscribeBaseEntity> findByUserIdAndRawDataIsEmpty(String userId);

}
