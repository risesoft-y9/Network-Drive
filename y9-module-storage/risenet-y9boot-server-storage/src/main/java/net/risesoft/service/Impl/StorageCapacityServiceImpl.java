package net.risesoft.service.Impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.StorageCapacity;
import net.risesoft.enums.StorageAuditLogEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.AuditLogEvent;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.StorageCapacityRepository;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.util.FileUtils;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.util.Y9StringUtil;

@Slf4j
@Service
public class StorageCapacityServiceImpl implements StorageCapacityService {

    private final StorageCapacityRepository storageCapacityRepository;
    private final PlatformTransactionManager transactionManager;
    @Value("${y9.app.storage.defaultStorageCapacity}")
    private String defaultStorageCapacity;

    public StorageCapacityServiceImpl(StorageCapacityRepository storageCapacityRepository,
        PlatformTransactionManager transactionManager) {
        this.storageCapacityRepository = storageCapacityRepository;
        this.transactionManager = transactionManager;
    }

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
    public StorageCapacity findOrCreateCapacity(String userId, String userName) {
        // 先快速查询，大部分情况记录已存在，直接返回
        StorageCapacity capacity = storageCapacityRepository.findByCapacityOwnerId(userId);
        if (capacity != null) {
            return capacity;
        }
        // 不存在则创建，synchronized + 独立事务提交保证线程安全
        synchronized (this) {
            // 双重检查：上一个线程的事务在 synchronized 内已提交，此时一定能读到
            capacity = storageCapacityRepository.findByCapacityOwnerId(userId);
            if (capacity != null) {
                return capacity;
            }
            // 用 TransactionTemplate 替代 @Transactional，确保事务在 synchronized 内提交
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return txTemplate.execute(status -> {
                StorageCapacity sc = new StorageCapacity();
                sc.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                sc.setCapacityOwnerId(userId);
                sc.setCapacityOwnerName(userName);
                sc.setCapacitySize(Long.valueOf(defaultStorageCapacity));
                sc.setRemainingLength(Long.valueOf(defaultStorageCapacity));
                sc.setCreateTime(new Date());
                // saveAndFlush 确保 INSERT 在事务提交前就发送到 DB
                storageCapacityRepository.saveAndFlush(sc);
                return sc;
            });
            // execute() 返回前会提交事务，保证后续线程进入 synchronized 时 DB 中已有数据
        }
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
    @Transactional
    public Y9Result<Object> updateCapacity(StorageCapacity storageCapacity) {
        try {
            StorageCapacity sc = this.findById(storageCapacity.getId());
            if (null != sc) {
                long defaultSize = Long.parseLong(defaultStorageCapacity);
                if (storageCapacity.getCapacitySize() == defaultSize
                    || storageCapacity.getCapacitySize() > defaultSize) {
                    if (storageCapacity.getCapacitySize() > sc.getCapacitySize()) {
                        Long size = storageCapacity.getCapacitySize() - sc.getCapacitySize();
                        sc.setRemainingLength(size + sc.getRemainingLength());
                    }
                    if (storageCapacity.getCapacitySize() < sc.getCapacitySize()) {
                        Long size = sc.getCapacitySize() - storageCapacity.getCapacitySize();
                        sc.setRemainingLength(sc.getRemainingLength() - size);
                    }
                    sc.setCapacitySize(storageCapacity.getCapacitySize());
                    sc.setUpdateTime(new Date());
                    this.save(sc);
                    AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                        .action(StorageAuditLogEnum.STORAGE_CAPACITY_UPDATE.getAction())
                        .description(Y9StringUtil.format(StorageAuditLogEnum.STORAGE_CAPACITY_UPDATE.getDescription(),
                            sc.getOperatorName(), sc.getCapacityOwnerName(), sc.getCapacitySize()))
                        .objectId(sc.getId())
                        .oldObject(sc)
                        .currentObject(null)
                        .build();
                    Y9Context.publishEvent(auditLogEvent);
                } else {
                    return Y9Result.failure("存储空间只能扩容，扩容值不能小于默认存储容量：" + defaultStorageCapacity + "字节("
                        + FileUtils.convertFileSize(sc.getCapacitySize()) + ")");
                }
            }
        } catch (Exception e) {
            LOGGER.error("存储空间扩容失败", e);
        }
        return Y9Result.success(null, "存储空间扩容成功");
    }

    @Override
    public StorageCapacity findById(String id) {
        return storageCapacityRepository.findById(id).orElse(null);
    }

}
