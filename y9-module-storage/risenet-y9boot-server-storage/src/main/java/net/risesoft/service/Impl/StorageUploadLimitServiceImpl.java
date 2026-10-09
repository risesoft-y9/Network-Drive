package net.risesoft.service.Impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.StorageUploadLimit;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.StorageUploadLimitRepository;
import net.risesoft.service.StorageUploadLimitService;

@Service
@RequiredArgsConstructor
public class StorageUploadLimitServiceImpl implements StorageUploadLimitService {

    private final StorageUploadLimitRepository storageUploadLimitRepository;

    @Override
    public StorageUploadLimit getUploadLimit() {
        return storageUploadLimitRepository.findFirstByOrderByIdAsc();
    }

    @Override
    @Transactional(readOnly = false)
    public StorageUploadLimit saveOrUpdate(Long uploadSize, String operatorId, String operatorName) {
        StorageUploadLimit sul = storageUploadLimitRepository.findFirstByOrderByIdAsc();
        Date now = new Date();
        if (null == sul) {
            sul = new StorageUploadLimit();
            sul.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            sul.setCreateTime(now);
        }
        sul.setUploadSize(uploadSize);
        sul.setOperatorId(operatorId);
        sul.setOperatorName(operatorName);
        sul.setUpdateTime(now);
        return storageUploadLimitRepository.save(sul);
    }

}
