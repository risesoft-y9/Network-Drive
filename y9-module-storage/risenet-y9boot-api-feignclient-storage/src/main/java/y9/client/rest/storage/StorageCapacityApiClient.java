package y9.client.rest.storage;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.api.storage.StorageCapacityApi;
import net.risesoft.model.storage.StorageUsageRatioModel;
import net.risesoft.pojo.Y9Result;

/**
 * 网络硬盘存储容量接口
 *
 * @author yihong
 *
 */
@FeignClient(contextId = "StorageCapacityApiClient", name = "${y9.service.storage.name:storage}",
    url = "${y9.service.storage.directUrl:}",
    path = "/${y9.service.storage.name:server-storage}/services/rest/capacity")
public interface StorageCapacityApiClient extends StorageCapacityApi {

    /**
     * 获取当前人的文件存储空间使用比例（用于饼状图显示）
     *
     * @param tenantId
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/getStorageUsageRatio")
    Y9Result<StorageUsageRatioModel> getStorageUsageRatio(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId);

}