package net.risesoft.api.storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.model.storage.StorageUsageRatioModel;
import net.risesoft.pojo.Y9Result;

/**
 * 消息提醒接口
 *
 * @author yihong
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
    @GetMapping("/getStorageUsageRatio")
    Y9Result<StorageUsageRatioModel> getStorageUsageRatio(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId);

}