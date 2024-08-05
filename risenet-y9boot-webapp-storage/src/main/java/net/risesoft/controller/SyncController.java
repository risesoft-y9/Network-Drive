package net.risesoft.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileNode;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.support.RootFolder;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

/**
 * 增删数据库字段 数据做同步修改
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/sync")
public class SyncController {

    private final FileNodeRepository fileNodeRepository;

    private final Y9FileStoreService y9FileStoreService;

    @RequestMapping("/fileSize")
    public Y9Result<Object> fileSize() {
        List<FileNode> fileNodeList = fileNodeRepository.findAll();
        for (FileNode fileNode : fileNodeList) {
            if (StringUtils.isNotBlank(fileNode.getFileStoreId())) {
                Y9FileStore y9FileStore = y9FileStoreService.getById(fileNode.getFileStoreId());
                if (y9FileStore != null) {
                    fileNode.setFileSize(y9FileStore.getFileSize());
                    fileNodeRepository.save(fileNode);
                }
            }
        }
        return Y9Result.success();
    }

    @RequestMapping("/rootFolder")
    public Y9Result<Object> rootFolder() {
        List<FileNode> fileNodeList = fileNodeRepository.findAll();
        for (FileNode fileNode : fileNodeList) {
            if (fileNode.getParentId() == null) {
                fileNode.setParentId(RootFolder.MY.getEnName());
                fileNodeRepository.save(fileNode);
            }
        }
        return Y9Result.success();
    }
}
