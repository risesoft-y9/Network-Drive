package net.risesoft.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileNodeCollectService;

/**
 * 文件收藏接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/fileNodeCollect")
public class FileNodeCollectController {

    private final FileNodeCollectService fileNodeCollectService;

    /**
     * 取消收藏文件或者文件夹
     *
     * @param fileId
     * @return
     */
    @RequestMapping(value = "/cancelCollect")
    public Y9Result<String> cancelCollect(String fileId) {
        try {
            fileNodeCollectService.cancelCollect(fileId);
            return Y9Result.success("取消收藏成功！");
        } catch (Exception e) {
            LOGGER.error("取消收藏失败！", e);
            return Y9Result.failure("取消收藏失败！");
        }
    }

    /**
     * 收藏文件或者文件夹
     *
     * @param fileId
     * @return
     */
    @RequestMapping(value = "/setCollect")
    public Y9Result<String> setCollect(String fileId) {
        fileNodeCollectService.save(fileId, "");
        return Y9Result.success("收藏成功！");
    }
}
