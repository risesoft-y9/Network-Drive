package net.risesoft.controller;

import static net.risesoft.util.FileUtils.generatePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.entity.Chunk;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.StorageCapacity;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ChunkService;
import net.risesoft.service.FileInfoService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

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
    private final OrgUnitApi orgUnitApi;
    private final PositionApi positionApi;

    private Map<String, Object> mergeMethod(String targetFile, String folder, String fileName, String parentId,
        String listType) {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", "文件合并失败");
        map.put("success", false);
        if (StringUtils.isBlank(targetFile) || StringUtils.isBlank(folder) || StringUtils.isBlank(fileName)) {
            map.put("msg", "合并参数不完整");
            return map;
        }
        try {
            Path targetPath = Paths.get(targetFile);
            if (!Files.exists(targetPath)) {
                Files.createFile(targetPath);
            }
        } catch (IOException e) {
            LOGGER.error("创建目标文件失败, targetFile={}", targetFile, e);
            map.put("msg", "创建目标文件失败");
            return map;
        }
        try (Stream<Path> stream = Files.list(Paths.get(folder))) {
            stream.filter(path -> !path.getFileName().toString().equals(fileName)).sorted((o1, o2) -> {
                String p1 = o1.getFileName().toString();
                String p2 = o2.getFileName().toString();
                int i1 = p1.lastIndexOf("-");
                int i2 = p2.lastIndexOf("-");
                return Integer.valueOf(p1.substring(i1)).compareTo(Integer.valueOf(p2.substring(i2)));
            }).forEach(path -> {
                try {
                    // 以追加的形式写入文件
                    Files.write(Paths.get(targetFile), Files.readAllBytes(path), StandardOpenOption.APPEND);
                    // 合并后删除该块
                    Files.delete(path);
                } catch (IOException e) {
                    LOGGER.error("合并文件块失败, path={}", path, e);
                }
            });
            LOGGER.debug("合并文件 {} 成功,目标目录: {}", fileName, targetFile);
            File file = new File(targetFile);
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String fileExtension = FilenameUtils.getExtension(fileName);
            long fileSize = file.length();

            String fullPath = Y9FileStore.buildPath(Y9Context.getSystemName(), Y9LoginUserHolder.getTenantId(),
                userInfo.getPersonId(), parentId);
            Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, fullPath, fileName);
            if (y9FileStore != null) {
                LOGGER.debug("文件 {} 上传成功, uuid:{}", y9FileStore.getFileName(), y9FileStore.getId());
                map = fileNodeService.saveFileNodeAndCapacity(parentId, fileName, fileExtension, fileSize,
                    y9FileStore.getId(), listType);
            } else {
                map.put("msg", "文件上传存储失败");
            }
        } catch (Exception e) {
            LOGGER.error("合并文件失败, targetFile={}, folder={}, fileName={}", targetFile, folder, fileName, e);
        }
        return map;
    }

    @RiseLog(operationName = "上传文件块")
    @PostMapping(value = "/chunk")
    public Y9Result<String> uploadChunk(Chunk chunk, HttpServletResponse response) {
        MultipartFile file = chunk.getFile();
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Y9Result.failure("上传文件不能为空");
        }
        LOGGER.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        try {
            Long fileSize = chunk.getTotalSize();
            LOGGER.debug("##########################文件大小: {}", fileSize);
            StorageCapacity capacity = storageCapacityService.findByCapacityOwnerId(userInfo.getPersonId());
            if (capacity != null && capacity.getRemainingLength() != null && capacity.getRemainingLength() < fileSize) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return Y9Result.failure("存储空间不够，无法上传，请联系存储空间管理员扩容");
            }
            byte[] bytes = file.getBytes();
            String chunckPath = Y9Context.getWebRootRealPath() + "upload";
            Path path = Paths.get(generatePath(chunckPath, chunk));
            // 文件写入指定路径
            Files.write(path, bytes);
            LOGGER.debug("文件 {} 写入成功, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
            chunk.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            chunkService.saveChunk(chunk);

            return Y9Result.success("文件上传成功");
        } catch (IOException e) {
            LOGGER.error("后端异常...", e);
            return Y9Result.failure("后端异常...");
        }
    }

    @RiseLog(operationName = "验证文件块")
    @GetMapping(value = "/chunk")
    public Y9Result<Object> checkChunk(Chunk chunk, HttpServletResponse response) {
        if (StringUtils.isBlank(chunk.getIdentifier()) || chunk.getChunkNumber() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Y9Result.failure("参数不完整");
        }
        LOGGER.debug("文件 {} 验证, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
        if (chunkService.checkChunk(chunk.getIdentifier(), chunk.getChunkNumber())) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return Y9Result.success("文件块已存在");
        }
        return Y9Result.success("文件块不存在");
    }

    @RiseLog(operationName = "合并文件")
    @PostMapping("/mergeFile")
    public Y9Result<Map<String, Object>> mergeFile(FileInfo fileInfo, @RequestParam String parentId,
        @RequestParam String listType, HttpServletResponse response) {
        if (fileInfo == null || StringUtils.isBlank(fileInfo.getFilename())
            || StringUtils.isBlank(fileInfo.getIdentifier())) {
            return Y9Result.failure("文件信息不完整");
        }
        String fileName = fileInfo.getFilename();
        String chunckPath = Y9Context.getWebRootRealPath() + "upload";
        String file = chunckPath + "/" + fileInfo.getIdentifier() + "/" + fileName;
        String folder = chunckPath + "/" + fileInfo.getIdentifier();

        // 合并文件
        Map<String, Object> map = mergeMethod(file, folder, fileName, parentId, listType);
        LOGGER.info("########### mergeMethod result: {}", map);
        Boolean success = (Boolean)map.get("success");
        String message = map.get("msg") != null ? map.get("msg").toString() : "未知错误";
        String fileId = map.get("fileId") != null ? map.get("fileId").toString() : "";
        if (Boolean.TRUE.equals(success)) {
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

}
