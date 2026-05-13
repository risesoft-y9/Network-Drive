package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.risesoft.entity.SubscribeFileEntity;

public interface SubscribeFileRepository extends JpaRepository<SubscribeFileEntity, String> {

    Page<SubscribeFileEntity> findBySubscribeId(String subscribeId, Pageable pageable);

}
