package net.risesoft.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.controller.dto.FileShareLinkDTO;
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
     * @param fileShareLinkDTO
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "创建文件分享链接")
    @PostMapping(value = "/createLink")
    public Y9Result<FileShareLink> createLink(FileShareLinkDTO fileShareLinkDTO) {
        return fileShareLinkService.createLink(fileShareLinkDTO);
    }

    /**
     * 获取当前用户分享的直链列表（按创建时间倒序）
     *
     * @return {@link Y9Result<List<FileShareLink>>}
     */
    @RiseLog(operationName = "获取我分享的直链列表")
    @GetMapping(value = "/myShareLinks")
    public Y9Result<List<FileShareLink>> myShareLinks() {
        List<FileShareLink> list = fileShareLinkService.findMyShareLinks();
        return Y9Result.success(list, "查询成功");
    }

    /**
     * 根据ID取消分享链接
     *
     * @param idList
     * @return {@link Y9Result<Object>}
     */
    @RiseLog(operationName = "取消分享链接")
    @PostMapping(value = "/cancelLink")
    public Y9Result<Object> cancelLink(@RequestParam(name = "ids") List<String> idList) {
        return fileShareLinkService.deleteAllById(idList);
    }

}
