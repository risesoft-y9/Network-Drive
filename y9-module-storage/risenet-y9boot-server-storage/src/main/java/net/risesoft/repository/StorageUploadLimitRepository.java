package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.StorageUploadLimit;

public interface StorageUploadLimitRepository
    extends JpaRepository<StorageUploadLimit, String>, JpaSpecificationExecutor<StorageUploadLimit> {

    /**
     * 获取全局唯一的上传大小限制记录
     *
     * @return
     */
    StorageUploadLimit findFirstByOrderByIdAsc();
}
