package net.risesoft.controller;

import org.apache.commons.lang3.StringUtils;
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
import net.risesoft.support.FileListType;
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
     * 获取指定文件列表类型的上传大小限制<br>
     * 该类型未单独配置时回落到升级前的全局默认配置，数据库无记录时返回配置文件中的默认值
     *
     * @param fileType 文件列表类型(my=我的文件/dept=部门文件/public=公共文件/report=文件上报)
     * @return
     */
    @RiseLog(operationName = "获取上传大小限制")
    @PostMapping(value = "/getUploadLimit")
    public Y9Result<StorageUploadLimit> getUploadLimit(@RequestParam(required = false) String fileType) {
        StorageUploadLimit sul;
        try {
            String type = StringUtils.isBlank(fileType) ? FileListType.MY.getValue() : fileType.trim();
            sul = storageUploadLimitService.getUploadLimit(type);
            if (null == sul) {
                // 数据库无记录时回退到配置文件的默认值
                sul = new StorageUploadLimit();
                sul.setFileType(type);
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
     * 保存指定文件列表类型的上传大小限制（每种文件列表类型保存一条配置）
     *
     * @param fileType 文件列表类型(my=我的文件/dept=部门文件/public=公共文件/report=文件上报)
     * @param uploadSize 上传大小限制(单位:字节)
     * @return
     */
    @RiseLog(operationName = "保存上传大小限制")
    @PostMapping(value = "/updateUploadLimit")
    public Y9Result<Object> updateUploadLimit(@RequestParam String fileType, @RequestParam Long uploadSize) {
        if (StringUtils.isBlank(fileType)) {
            return Y9Result.failure("文件列表类型不能为空");
        }
        if (null == uploadSize || uploadSize <= 0) {
            return Y9Result.failure("上传大小限制必须大于0");
        }
        try {
            UserInfo user = Y9LoginUserHolder.getUserInfo();
            String operatorId = null != user ? user.getPersonId() : "";
            String operatorName = null != user ? user.getName() : "";
            storageUploadLimitService.saveOrUpdate(fileType.trim(), uploadSize, operatorId, operatorName);
        } catch (Exception e) {
            LOGGER.error("保存上传大小限制失败", e);
            return Y9Result.failure("保存上传大小限制失败");
        }
        return Y9Result.success(null, "保存上传大小限制成功");
    }

}
