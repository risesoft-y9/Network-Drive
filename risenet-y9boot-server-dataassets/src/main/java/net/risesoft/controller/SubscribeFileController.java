package net.risesoft.controller;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.SubscribeFileEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.SubscribeFileRepository;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/subscribeFile", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubscribeFileController {

    private final SubscribeFileRepository subscribeFileRepository;
    private final Y9FileStoreService y9FileStoreService;

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "上传文件", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/fileUpload")
    public Y9Result<String> fileUpload(@RequestParam MultipartFile[] files, @RequestParam String subscribeId) {
        String fileName = "";
        try {
            if (files.length == 0) {
                return Y9Result.failure("请选择文件");
            }
            for (MultipartFile file : files) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                fileName = file.getOriginalFilename();// 文件名称
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件类型
                if (fileType.indexOf("js") > -1 || fileType.indexOf("htm") > -1 || fileType.indexOf("jar") > -1) {
                    return Y9Result.failure("不支持上传的文件格式：" + fileName);
                } else {
                    // 文件存储地址
                    String savePath = Y9Context.getSystemName() + "/subscribe/" + sdf.format(new Date()) + "/";
                    // 保存文件
                    Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file.getInputStream(), savePath, fileName);
                    if (y9FileStore != null) {
                        SubscribeFileEntity subscribeFileEntity = new SubscribeFileEntity();
                        subscribeFileEntity.setId(Y9IdGenerator.genId());
                        subscribeFileEntity.setSubscribeId(subscribeId);
                        subscribeFileEntity.setFileId(y9FileStore.getId());
                        subscribeFileEntity.setFileName(fileName);
                        subscribeFileRepository.save(subscribeFileEntity);
                    } else {
                        return Y9Result.failure("上传失败：" + fileName + "，请重新上传");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure(fileName + "-上传失败：" + e.getMessage());
        }
        return Y9Result.successMsg("上传成功：" + fileName);
    }

    @RiseLog(operationName = "分页获取订阅文件列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getFilePage")
    public Y9Page<SubscribeFileEntity> getFilePage(@RequestParam String subscribeId, Integer page, Integer size) {
        if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createTime"));
        Page<SubscribeFileEntity> pageList = subscribeFileRepository.findBySubscribeId(subscribeId, pageable);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "下载文件", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/fileDownload")
    public void fileDownload(String id, HttpServletResponse response, HttpServletRequest request) {
        try {
            SubscribeFileEntity subscribeFileEntity = subscribeFileRepository.findById(id).orElse(null);
            if (subscribeFileEntity == null) {
                return;
            }
            String filename = subscribeFileEntity.getFileName();
            String fileId = subscribeFileEntity.getFileId();
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO8859-1");// 火狐浏览器
            } else {
                filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            }
            OutputStream out = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            y9FileStoreService.downloadFileToOutputStream(fileId, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除订阅文件", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/delete")
    public Y9Result<String> deleteSubscribeFile(@RequestParam String id) {
        try {
            SubscribeFileEntity subscribeFileEntity = subscribeFileRepository.findById(id).orElse(null);
            if (subscribeFileEntity == null) {
                return Y9Result.failure("文件不存在");
            }
            y9FileStoreService.deleteFile(subscribeFileEntity.getFileId());
            subscribeFileRepository.deleteById(id);
            return Y9Result.successMsg("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("删除失败：" + e.getMessage());
        }
    }

}
