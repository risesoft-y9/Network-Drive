package net.risesoft.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.entity.Chunk;
import net.risesoft.entity.DataAssetsFile;
import net.risesoft.entity.FileInfo;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataAssetsFileService;
import net.risesoft.service.FileChunkService;
import net.risesoft.service.FileInfoService;
import net.risesoft.util.ArchiveDetection;
import net.risesoft.util.FileUtils;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/files")
public class DataAssetsFileController {

    private final FileInfoService fileInfoService;
    private final FileChunkService chunkService;
    private final Y9FileStoreService y9FileStoreService;
    private final DataAssetsFileService dataAssetsFileService;
    private final OrgUnitApi orgUnitApi;

    @GetMapping(value = "/getFileList")
    public Y9Result<List<DataAssetsFile>> getFileList(Long detailId) {
        List<DataAssetsFile> list = dataAssetsFileService.findByDetailId(detailId);
        return Y9Result.success(list, "获取列表成功");
    }

    @PostMapping(value = "/deleteFile")
    public Y9Result<String> deleteFile(String id) {
        DataAssetsFile file = dataAssetsFileService.findById(id);
        if (null != file) {
            dataAssetsFileService.deleteFile(id);
            y9FileStoreService.deleteFile(file.getFileStoreId());
        }
        return Y9Result.successMsg("删除成功");
    }

    @RequestMapping(value = "/downloadFile")
    public void downloadFile(@RequestParam(name = "id") String id, HttpServletResponse response,
        HttpServletRequest request) {
        try {
            DataAssetsFile file = dataAssetsFileService.findById(id);
            String filename = file.getFileName();
            String filePath = file.getFileStoreId();
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
            y9FileStoreService.downloadFileToOutputStream(filePath, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LOGGER.error("文件下载失败", e);
        }
    }

    public Map<String, Object> mergeMethod(String targetFile, String folder, String fileName, Long detailId) {
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
            String fileHash = ArchiveDetection.calculateHash(file);
            String fullPath = Y9FileStore.buildPath(Y9Context.getSystemName(), Y9LoginUserHolder.getTenantId(),
                userInfo.getPersonId(), detailId.toString());
            Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, fullPath, fileName);
            if (y9FileStore != null) {
                LOGGER.debug("文件 {} 上传成功, uuid:{}", y9FileStore.getFileName(), y9FileStore.getId());
                map = saveFile(detailId, fileName, fileExtension, fileSize, fileHash, y9FileStore.getId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return map;
    }

    @PostMapping(value = "/chunk")
    public Y9Result<String> uploadChunk1(Chunk chunk) {
        MultipartFile file = chunk.getFile();
        LOGGER.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());

        try {
            byte[] bytes = file.getBytes();
            String chunckPath = Y9Context.getWebRootRealPath() + "upload";
            Path path = Paths.get(FileUtils.generatePath(chunckPath, chunk));
            // 文件写入指定路径
            Files.write(path, bytes);
            LOGGER.debug("文件 {} 写入成功, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
            chunkService.saveChunk(chunk);

            return Y9Result.success("文件上传成功");
        } catch (IOException e) {
            LOGGER.error("后端异常...", e);
            e.printStackTrace();
            return Y9Result.failure("后端异常...");
        }
    }

    @GetMapping(value = "/chunk")
    public Y9Result<Object> checkChunk(Chunk chunk, HttpServletResponse response) {
        LOGGER.debug("文件 {} 验证, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
        if (chunkService.checkChunk(chunk.getIdentifier(), chunk.getChunkNumber())) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        return Y9Result.success(chunk);
    }

//    @PostMapping("/mergeFile")
//    public Y9Result<Map<String, Object>> mergeFile(FileInfo fileInfo, Long detailId) {
//        Map<String, Object> map = new HashMap<>();
//        String fileName = fileInfo.getFilename();
//        String chunckPath = Y9Context.getWebRootRealPath() + "upload";
//        String file = chunckPath + "/" + fileInfo.getIdentifier() + "/" + fileName;
//        String folder = chunckPath + "/" + fileInfo.getIdentifier();
//        // 合并文件
//        map = mergeMethod(file, folder, fileName, detailId);
//        Boolean success = Boolean.valueOf(map.get("success").toString());
//        if (success) {
//            // 保存文件信息
//            Long fileId = Long.parseLong(map.get("fileId").toString());
//            fileInfo.setLocation(file);
//            fileInfo.setArchiveFileId(fileId);
//            fileInfoService.addFileInfo(fileInfo);
//            return Y9Result.success(map, "合并成功");
//        }
//        return Y9Result.failure(500, "合并失败");
//    }

    private Map<String, Object> saveFile(Long detailId, String fileName, String fileExtension, long fileSize,
        String fileHash, String y9FileStoreId) {
        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String userId = userInfo.getPersonId(), userName = userInfo.getName(),
            tenantId = Y9LoginUserHolder.getTenantId(), positionId = Y9LoginUserHolder.getPositionId();
        try {

            boolean fileNodeExists = dataAssetsFileService.isFileExists(detailId, fileName);
            DataAssetsFile file = new DataAssetsFile();
            file.setFileType(fileExtension);
            file.setFileSize(fileSize);
            file.setFileHash(fileHash);
            file.setUploadTime(sdf.format(new Date()));
            file.setFileStoreId(y9FileStoreId);
            file.setPersonId(userId);
            file.setPersonName(userName);
            file.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            file.setDetailId(detailId);
            OrgUnit orgUnit = orgUnitApi.getParent(tenantId, positionId).getData();
            if (null != orgUnit) {
                file.setDeptId(orgUnit.getId());
                file.setDeptName(orgUnit.getName());
            }
            file.setPositionId(positionId);
            Integer tabIndex = dataAssetsFileService.getMaxTabIndex(detailId);
            file.setTabIndex(null == tabIndex ? 1 : tabIndex + 1);
            if (fileNodeExists) {
                SimpleDateFormat sdf1 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
                fileName = FilenameUtils.getBaseName(fileName) + sdf1.format(new Date()) + "." + fileExtension;
            }
            file.setFileName(fileName);
            file = dataAssetsFileService.save(file);
            map.put("fileId", file.getId());
            map.put("msg", "文件上传成功");
            map.put("success", true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            map.put("msg", "文件上传失败");
            map.put("success", false);
        }
        return map;
    }
}
