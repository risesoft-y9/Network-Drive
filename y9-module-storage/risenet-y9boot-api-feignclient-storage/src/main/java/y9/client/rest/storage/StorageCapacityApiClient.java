package y9.client.rest.storage;

import org.springframework.cloud.openfeign.FeignClient;

import net.risesoft.api.storage.StorageCapacityApi;

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

}