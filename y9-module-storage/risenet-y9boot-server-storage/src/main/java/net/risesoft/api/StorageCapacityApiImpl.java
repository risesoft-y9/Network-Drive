package net.risesoft.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.storage.StorageCapacityApi;
import net.risesoft.entity.StorageCapacity;
import net.risesoft.model.storage.StorageUsageRatioModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.util.FileUtils;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 存储容量API接口
 *
 * @author yihong
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/services/rest/capacity", produces = MediaType.APPLICATION_JSON_VALUE)
public class StorageCapacityApiImpl implements StorageCapacityApi {

    private final StorageCapacityService storageCapacityService;

    /**
     * 获取当前人的文件存储空间使用比例（用于饼状图显示）
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return Y9Result<StorageUsageRatioModel> 返回已使用容量、未使用容量、比例等数据
     */
    @Override
    public Y9Result<StorageUsageRatioModel> getStorageUsageRatio(@RequestParam String tenantId,
        @RequestParam String userId) {
        StorageUsageRatioModel model = new StorageUsageRatioModel();
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            StorageCapacity sc = storageCapacityService.findByCapacityOwnerId(userId);
            if (sc == null) {
                model.setCapacitySize(0L);
                model.setUsedLength(0L);
                model.setRemainingLength(0L);
                model.setCapacitySizeStr("0 B");
                model.setUsedLengthStr("0 B");
                model.setRemainingLengthStr("0 B");
                model.setUsedPercentage(0.0);
                model.setRemainingPercentage(0.0);
                return Y9Result.success(model, "获取存储空间使用比例成功");
            }

            long capacitySize = sc.getCapacitySize();
            long remainingLength = sc.getRemainingLength();
            long usedLength = capacitySize - remainingLength;

            // 计算百分比
            double usedPercentage = 0.0;
            double remainingPercentage = 100.0;
            if (capacitySize > 0) {
                BigDecimal used = BigDecimal.valueOf(usedLength);
                BigDecimal total = BigDecimal.valueOf(capacitySize);
                usedPercentage = used.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
                remainingPercentage = BigDecimal.valueOf(100)
                    .subtract(BigDecimal.valueOf(usedPercentage))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            }

            model.setCapacitySize(capacitySize);
            model.setUsedLength(usedLength);
            model.setRemainingLength(remainingLength);
            model.setCapacitySizeStr(FileUtils.convertFileSize(capacitySize));
            model.setUsedLengthStr(FileUtils.convertFileSize(usedLength));
            model.setRemainingLengthStr(FileUtils.convertFileSize(remainingLength));
            model.setUsedPercentage(usedPercentage);
            model.setRemainingPercentage(remainingPercentage);

            return Y9Result.success(model, "获取存储空间使用比例成功");
        } catch (Exception e) {
            LOGGER.error("获取存储空间使用比例失败", e);
            return Y9Result.failure("获取存储空间使用比例失败：" + e.getMessage());
        }
    }
}
