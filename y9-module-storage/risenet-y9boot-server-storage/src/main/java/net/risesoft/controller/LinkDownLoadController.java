package net.risesoft.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileDownLoadRecord;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileShareLink;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileDownLoadRecordService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileShareLinkService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.base64.Y9Base64Util;
import net.risesoft.y9.util.mime.ContentDispositionUtil;
import net.risesoft.y9public.service.Y9FileStoreService;

/**
 * 文件直链接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/link")
public class LinkDownLoadController {

    private final Y9FileStoreService y9FileStoreService;
    private final FileNodeService fileNodeService;
    private final FileDownLoadRecordService fileDownLoadRecordService;
    private final FileShareLinkService fileShareLinkService;

    /**
     * 文件直链下载
     * 
     * @param id
     * @param tenantId
     * @param response
     */
    @RiseLog(operationName = "文件链接下载")
    @RequestMapping(value = "/df/{id}/{tenantId}")
    public void downloadFile(@PathVariable String id, @PathVariable String tenantId, HttpServletResponse response) {
        ServletOutputStream os = null;
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            FileNode fileNode = fileNodeService.findById(id);
            String fileName = fileNode.getName();
            String y9FileStoreId = fileNode.getFileStoreId();
            response.setContentType("text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", ContentDispositionUtil.standardizeAttachment(fileName));

            os = response.getOutputStream();
            y9FileStoreService.downloadFileToOutputStream(y9FileStoreId, os);
            FileDownLoadRecord fdr = new FileDownLoadRecord();
            fdr.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fdr.setFileId(id);
            fdr.setDownLoadTime(new Date());
            fdr.setDownLoadUserId("直链下载");
            fdr.setDownLoadUserName("直链下载");
            fdr.setDownLoadMode("直链");
            fileDownLoadRecordService.save(fdr);
        } catch (Exception e) {
            LOGGER.error("下载文件失败", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                LOGGER.error("关闭输出流失败", e);
            }
        }
    }

    /**
     * 文件直链密码验证
     *
     * @param id
     * @param pwd
     * @param tenantId
     * @return
     */
    @RiseLog(operationName = "文件链接密码验证")
    @RequestMapping(value = "/checkPwd")
    public Y9Result<Object> checkPwd(String id, String pwd, String tenantId) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        FileNode fileNode = fileNodeService.findById(id);
        if (null != fileNode) {
            map.put("success", false);
            map.put("msg", "密码输入错误，请输入正确的密码");
            if (fileNode.getLinkPassword().equals(pwd)) {
                map.put("success", true);
                map.put("msg", "密码验证成功，正在为您下载");
            }
        }
        return Y9Result.success(map);
    }

    @RiseLog(operationName = "验证文件分享链接和密码")
    @RequestMapping(value = "/checkLink")
    public Y9Result<Object> checkLink(String tenantId, String pwd, String linkKey) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        FileShareLink fileShareLink = fileShareLinkService.findByLinkKey(linkKey);
        if (null != fileShareLink) {
            map.put("success", false);
            map.put("msg", "密码输入错误，请输入正确的密码");
            String password = Y9Base64Util.encode(pwd);
            if (fileShareLink.getLinkPassword().equals(password)) {
                map.put("fileId", fileShareLink.getFileId());
                map.put("success", true);
                map.put("msg", "密码验证成功，正在为您下载");
            }
        }
        return Y9Result.success(map);
    }
}
