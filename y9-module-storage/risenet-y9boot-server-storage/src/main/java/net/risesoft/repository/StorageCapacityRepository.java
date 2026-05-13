package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.StorageCapacity;

public interface StorageCapacityRepository
    extends JpaRepository<StorageCapacity, String>, JpaSpecificationExecutor<StorageCapacity> {

    Page<StorageCapacity> findByCapacityOwnerNameLike(String userName, Pageable pageable);

    StorageCapacity findByCapacityOwnerId(String userId);
}
