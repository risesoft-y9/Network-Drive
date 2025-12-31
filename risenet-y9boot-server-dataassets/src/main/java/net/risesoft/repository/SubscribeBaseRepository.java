package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.SubscribeBaseEntity;

public interface SubscribeBaseRepository
    extends JpaRepository<SubscribeBaseEntity, String>, JpaSpecificationExecutor<SubscribeBaseEntity> {

    SubscribeBaseEntity findBySubscribeId(String subscribeId);

}
