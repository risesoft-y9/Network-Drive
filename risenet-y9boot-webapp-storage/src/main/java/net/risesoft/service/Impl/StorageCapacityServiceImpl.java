package net.risesoft.service.Impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.StorageCapacity;
import net.risesoft.repository.StorageCapacityRepository;
import net.risesoft.service.StorageCapacityService;

@Service
public class StorageCapacityServiceImpl implements StorageCapacityService {

    @Autowired
    private StorageCapacityRepository storageCapacityRepository;

    @Override
    @Transactional(readOnly = false)
    public void save(StorageCapacity storageCapacity) {
        storageCapacityRepository.save(storageCapacity);
    }

    @Override
    public StorageCapacity findByCapacityOwnerId(String userId) {
        return storageCapacityRepository.findByCapacityOwnerId(userId);
    }

    @Override
    public Page<StorageCapacity> findByUserName(String userName, int page, int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Direction.DESC, "createTime");
        if (StringUtils.isNotBlank(userName)) {
            return storageCapacityRepository.findByCapacityOwnerNameLike("%" + userName + "%", pageable);
        }
        return storageCapacityRepository.findAll(pageable);
    }

    @Override
    public StorageCapacity findById(String id) {
        return storageCapacityRepository.findById(id).orElse(null);
    }

}
