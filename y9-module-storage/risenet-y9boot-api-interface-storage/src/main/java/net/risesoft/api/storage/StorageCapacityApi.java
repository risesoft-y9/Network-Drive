package net.risesoft.api.storage;

import net.risesoft.model.storage.StorageUsageRatioModel;
import net.risesoft.pojo.Y9Result;

/**
 * 消息提醒接口
 *
 * @author 10858
 *
 */
public interface StorageCapacityApi {

    /**
     * 获取设置了该人员某个提醒类型的所有人id
     *
     * @param tenantId 租户唯一标识
     * @param userId 用户唯一标识
     * @return String
     */
    Y9Result<StorageUsageRatioModel> getStorageUsageRatio(String tenantId, String userId);

}