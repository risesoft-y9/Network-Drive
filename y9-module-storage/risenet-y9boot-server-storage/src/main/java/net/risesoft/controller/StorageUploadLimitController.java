package net.risesoft.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.StorageUploadLimit;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.StorageUploadLimitService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 上传大小限制配置接口
 *
 * @author yihong
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/vue/uploadLimit")
public class StorageUploadLimitController {

    private final StorageUploadLimitService storageUploadLimitService;

    /** 配置文件中的默认单次上传限制(字节)，数据库无记录时回退使用 */
    @Value("${y9.app.storage.singleUploadLimit}")
    private String singleUploadLimit;

    /**
     * 获取上传大小限制，数据库无记录时返回配置文件中的默认值
     *
     * @return
     */
    @RiseLog(operationName = "获取上传大小限制")
    @PostMapping(value = "/getUploadLimit")
    public Y9Result<StorageUploadLimit> getUploadLimit() {
        StorageUploadLimit sul;
        try {
            sul = storageUploadLimitService.getUploadLimit();
            if (null == sul) {
                // 数据库无记录时回退到配置文件的默认值
                sul = new StorageUploadLimit();
                try {
                    sul.setUploadSize(Long.parseLong(singleUploadLimit));
                } catch (NumberFormatException e) {
                    // 配置缺失或非法时默认 120MB
                    sul.setUploadSize(120L * 1024 * 1024);
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取上传大小限制失败", e);
            return Y9Result.failure("获取上传大小限制失败");
        }
        return Y9Result.success(sul, "获取上传大小限制成功");
    }

    /**
     * 保存上传大小限制
     *
     * @param uploadSize 上传大小限制(单位:字节)
     * @return
     */
    @RiseLog(operationName = "保存上传大小限制")
    @PostMapping(value = "/updateUploadLimit")
    public Y9Result<Object> updateUploadLimit(@RequestParam Long uploadSize) {
        if (null == uploadSize || uploadSize <= 0) {
            return Y9Result.failure("上传大小限制必须大于0");
        }
        try {
            UserInfo user = Y9LoginUserHolder.getUserInfo();
            String operatorId = null != user ? user.getPersonId() : "";
            String operatorName = null != user ? user.getName() : "";
            storageUploadLimitService.saveOrUpdate(uploadSize, operatorId, operatorName);
        } catch (Exception e) {
            LOGGER.error("保存上传大小限制失败", e);
            return Y9Result.failure("保存上传大小限制失败");
        }
        return Y9Result.success(null, "保存上传大小限制成功");
    }

}
