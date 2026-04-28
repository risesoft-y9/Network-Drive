package net.risesoft.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileShareLink;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileShareLinkService;

/**
 * 文件分享链接接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/fileShareLink")
public class FileShareLinkController {

    private final FileShareLinkService fileShareLinkService;

    /**
     * 直链创建
     *
     * @param fileId
     * @return
     */
    @RiseLog(operationName = "创建文件分享链接")
    @PostMapping(value = "/createLink")
    public Y9Result<FileShareLink> createLink(String fileId, String linkPassword) {
        return fileShareLinkService.createLink(fileId, linkPassword);
    }

}
