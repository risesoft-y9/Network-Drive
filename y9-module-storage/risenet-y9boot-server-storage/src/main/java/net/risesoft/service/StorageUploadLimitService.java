package net.risesoft.service;

import net.risesoft.entity.StorageUploadLimit;

public interface StorageUploadLimitService {

    /**
     * 获取全局唯一的上传大小限制记录，不存在时返回 null
     *
     * @return
     */
    public StorageUploadLimit getUploadLimit();

    /**
     * 保存或更新上传大小限制（全局一条记录）
     *
     * @param uploadSize 上传大小限制(单位:字节)
     * @param operatorId 操作人id
     * @param operatorName 操作人
     * @return
     */
    public StorageUploadLimit saveOrUpdate(Long uploadSize, String operatorId, String operatorName);
}
