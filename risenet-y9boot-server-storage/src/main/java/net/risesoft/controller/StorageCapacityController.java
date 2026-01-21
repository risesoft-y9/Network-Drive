package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.StorageCapacity;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.util.FileUtils;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 文件存储空间接口
 *
 * @author yihong
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/vue/capacity")
public class StorageCapacityController {

    private final StorageCapacityService storageCapacityService;
    @Value("${y9.app.storage.defaultStorageCapacity}")
    private String defaultStorageCapacity;

    /**
     * 获取存储信息
     *
     * @param id
     * @return
     */
    @RiseLog(operationName = "获取存储信息")
    @RequestMapping(value = "/getCapacityInfo")
    public Y9Result<StorageCapacity> getCapacityInfo(String id) {
        StorageCapacity sc = storageCapacityService.findById(id);
        return Y9Result.success(sc, "获取存储信息成功");
    }

    /**
     * 获取存储空间列表
     *
     * @param userName
     * @param page
     * @param rows
     * @return
     */
    @RiseLog(operationName = "获取存储空间列表")
    @GetMapping(value = "/getCapacityList")
    public Y9Page<Map<String, Object>> getCapacityList(String userName, int page, int rows) {
        List<Map<String, Object>> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (page < 1) {
            page = 1;
        }
        Page<StorageCapacity> scList = storageCapacityService.findByUserName(userName, page, rows);
        for (StorageCapacity sc : scList.getContent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sc.getId());
            map.put("capacitySize", FileUtils.convertFileSize(sc.getCapacitySize()));
            map.put("remainingLength", FileUtils.convertFileSize(sc.getRemainingLength()));
            map.put("capacityOwnerId", sc.getCapacityOwnerId());
            map.put("capacityOwnerName", sc.getCapacityOwnerName());
            map.put("operatorId", sc.getOperatorId());
            map.put("operatorName", sc.getOperatorName());
            map.put("updateTime", sc.getUpdateTime() != null ? sdf.format(sc.getUpdateTime()) : "");
            map.put("createTime", sdf.format(sc.getCreateTime()));
            items.add(map);
        }
        return Y9Page.success(page, scList.getTotalPages(), scList.getTotalElements(), items);
    }

    /**
     * 获取存储长度
     *
     * @return
     */
    @RiseLog(operationName = "获取存储长度")
    @RequestMapping(value = "/getCapacitySize")
    public Y9Result<Map<String, Object>> getCapacitySize() {
        Map<String, Object> map = new HashMap<>();
        UserInfo user = Y9LoginUserHolder.getUserInfo();
        String capacitySizeStr = "";
        String remainingLengthStr = "";
        map.put("capacitySize", capacitySizeStr);
        map.put("remainingLength", remainingLengthStr);
        try {
            StorageCapacity sc = storageCapacityService.findByCapacityOwnerId(user.getPersonId());
            if (null != sc) {
                map.put("capacitySize", FileUtils.convertFileSize(sc.getCapacitySize()));
                map.put("remainingLength", FileUtils.convertFileSize(sc.getRemainingLength()));
            }
        } catch (Exception e) {
            LOGGER.error("获取存储长度失败", e);
        }
        return Y9Result.success(map, "获取存储长度成功");
    }

    /**
     * 更新存储空间值
     *
     * @param storageCapacity
     * @return
     */
    @RiseLog(operationName = "更新存储空间值")
    @RequestMapping(value = "/updateCapacity")
    public Y9Result<Object> updateCapacity(StorageCapacity storageCapacity) {
        try {
            StorageCapacity sc = storageCapacityService.findById(storageCapacity.getId());
            if (null != sc) {
                long defaultSize = Long.parseLong(defaultStorageCapacity);
                if (storageCapacity.getCapacitySize() == defaultSize
                    || storageCapacity.getCapacitySize() > defaultSize) {
                    if (storageCapacity.getCapacitySize() > sc.getCapacitySize()) {
                        Long size = storageCapacity.getCapacitySize() - sc.getCapacitySize();
                        sc.setRemainingLength(size + sc.getRemainingLength());
                    }
                    if (storageCapacity.getCapacitySize() < sc.getCapacitySize()) {// 5-8
                        Long size = sc.getCapacitySize() - storageCapacity.getCapacitySize();// 3
                        sc.setRemainingLength(sc.getRemainingLength() - size);
                    }
                    sc.setCapacitySize(storageCapacity.getCapacitySize());
                    sc.setUpdateTime(new Date());
                    storageCapacityService.save(sc);
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

}
