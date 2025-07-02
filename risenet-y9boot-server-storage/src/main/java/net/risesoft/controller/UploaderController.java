package net.risesoft.controller;

import static net.risesoft.util.FileUtils.generatePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.entity.Chunk;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.StorageCapacity;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ChunkService;
import net.risesoft.service.FileInfoService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.support.FileListType;
import net.risesoft.util.FileNodeUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

import y9.client.rest.platform.org.OrgUnitApiClient;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/uploader")
public class UploaderController {

    private final FileInfoService fileInfoService;
    private final ChunkService chunkService;
    private final Y9FileStoreService y9FileStoreService;
    private final StorageCapacityService storageCapacityService;
    private final FileNodeService fileNodeService;
    private final OrgUnitApiClient orgUnitApiClient;
    private final PositionApi positionApi;
    @Value("${y9.app.storage.defaultStorageCapacity}")
    private String defaultStorageCapacity;
    @Value("${y9.app.storage.singleUploadLimit}")
    private String singleUploadLimit;

    public Map<String, Object> mergeMethod(String targetFile, String folder, String fileName, String parentId,
        String listType) {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", "文件合并失败");
        map.put("success", false);
        try {
            Files.createFile(Paths.get(targetFile));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try (Stream<Path> stream = Files.list(Paths.get(folder))) {
            stream.filter(path -> !path.getFileName().toString().equals(fileName)).sorted((o1, o2) -> {
                String p1 = o1.getFileName().toString();
                String p2 = o2.getFileName().toString();
                int i1 = p1.lastIndexOf("-");
                int i2 = p2.lastIndexOf("-");
                return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
            }).forEach(path -> {
                try {
                    // 以追加的形式写入文件
                    Files.write(Paths.get(targetFile), Files.readAllBytes(path), StandardOpenOption.APPEND);
                    // 合并后删除该块
                    Files.delete(path);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
            LOGGER.debug("合并文件 {} 成功,目标目录: {}", targetFile);
            File file = new File(targetFile);
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String fileExtension = FilenameUtils.getExtension(fileName);
            long fileSize = file.length();

            String fullPath = Y9FileStore.buildPath(Y9Context.getSystemName(), Y9LoginUserHolder.getTenantId(),
                userInfo.getPersonId(), parentId);
            Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, fullPath, fileName);
            if (y9FileStore != null) {
                LOGGER.debug("文件 {} 上传成功, uuid:{}", y9FileStore.getFileName(), y9FileStore.getId());
                map =
                    saveFileNodeAndCapacity(parentId, fileName, fileExtension, fileSize, y9FileStore.getId(), listType);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return map;
    }

    @PostMapping(value = "/chunk")
    public String uploadChunk1(Chunk chunk, HttpServletResponse response) {
        MultipartFile file = chunk.getFile();
        LOGGER.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        try {
            Long fileSize = chunk.getTotalSize();
            LOGGER.debug("##########################文件大小: {}", fileSize);
            StorageCapacity capacity = storageCapacityService.findByCapacityOwnerId(userInfo.getPersonId());
            if (null != capacity) {
                if (capacity.getRemainingLength() < fileSize) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return "存储空间不够，无法上传，请联系存储空间管理员扩容";
                }
            }
            byte[] bytes = file.getBytes();
            String chunckPath = Y9Context.getWebRootRealPath() + "upload";
            Path path = Paths.get(generatePath(chunckPath, chunk));
            // 文件写入指定路径
            Files.write(path, bytes);
            LOGGER.debug("文件 {} 写入成功, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
            chunk.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            chunkService.saveChunk(chunk);

            return "文件上传成功";
        } catch (IOException e) {
            LOGGER.error("后端异常...", e);
            return "后端异常...";
        }
    }

    @GetMapping(value = "/chunk")
    public Object checkChunk(Chunk chunk, HttpServletResponse response) {
        LOGGER.debug("文件 {} 验证, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
        if (chunkService.checkChunk(chunk.getIdentifier(), chunk.getChunkNumber())) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        return chunk;
    }

    @PostMapping("/mergeFile")
    public Y9Result<Map<String, Object>> mergeFile(FileInfo fileInfo, String parentId, String listType,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        String fileName = fileInfo.getFilename();
        String chunckPath = Y9Context.getWebRootRealPath() + "upload";
        String file = chunckPath + "/" + fileInfo.getIdentifier() + "/" + fileName;
        String folder = chunckPath + "/" + fileInfo.getIdentifier();

        // 合并文件
        map = mergeMethod(file, folder, fileName, parentId, listType);
        LOGGER.info("########### mergeMethod result: {}", map);
        Boolean success = Boolean.valueOf(map.get("success").toString());
        String message = map.get("msg").toString();
        String fileId = null != map.get("fileId") ? map.get("fileId").toString() : "";
        if (success) {
            // 保存文件信息
            fileInfo.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileInfo.setLocation(file);
            fileInfo.setFileNodeId(fileId);
            fileInfoService.addFileInfo(fileInfo);
            return Y9Result.success(map, "合并成功");
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Y9Result.failure(500, "合并失败:" + message);
    }

    private Map<String, Object> saveFileNodeAndCapacity(String parentId, String fileName, String fileExtension,
        long fileSize, String y9FileStoreId, String listType) {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", "文件上传失败");
        map.put("success", false);
        map.put("fileId", null);
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String userId = userInfo.getPersonId(), userName = userInfo.getName();
        try {
            if (parentId.equals(FileListType.MY.getValue())) {
                StorageCapacity capacity = storageCapacityService.findByCapacityOwnerId(userId);
                if (null == capacity) {
                    StorageCapacity sc = new StorageCapacity();
                    sc.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                    sc.setCapacityOwnerId(userId);
                    sc.setCapacityOwnerName(userName);
                    sc.setCapacitySize(Long.valueOf(defaultStorageCapacity));
                    sc.setRemainingLength(Long.valueOf(defaultStorageCapacity));
                    sc.setCreateTime(new Date());
                    storageCapacityService.save(sc);
                } else {
                    if (capacity.getRemainingLength() > fileSize) {
                        capacity.setRemainingLength(capacity.getRemainingLength() - fileSize);
                        storageCapacityService.save(capacity);
                    }
                }
            }
            Integer type = FileNodeUtil.fileTypeConvert(fileExtension);
            boolean fileNodeExists = fileNodeService.isFileNodeExists(parentId, fileName);

            FileNode fileNode = new FileNode();
            fileNode.setFileSuffix(fileExtension);
            fileNode.setFileSize(fileSize);
            fileNode.setCreateTime(new Date());
            fileNode.setUpdateTime(new Date());
            fileNode.setFileStoreId(y9FileStoreId);
            fileNode.setListType(listType);
            fileNode.setUserId(userInfo.getPersonId());
            fileNode.setUserName(userInfo.getName());
            fileNode.setFileType(type);
            fileNode.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            if (StringUtils.isNotBlank(parentId)) {
                fileNode.setParentId(parentId);
            }
            if (fileNodeExists) {
                SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
                fileName = FilenameUtils.getBaseName(fileName) + sdf.format(new Date()) + "." + fileExtension;
            }
            fileNode.setName(fileName);
            fileNode = fileNodeService.saveNode(fileNode);
            map.put("fileId", fileNode.getId());
            map.put("msg", "文件上传成功");
            map.put("success", true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

        }
        return map;
    }
}
