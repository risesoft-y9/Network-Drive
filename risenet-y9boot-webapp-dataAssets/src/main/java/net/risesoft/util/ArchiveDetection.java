package net.risesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DataAssetsFile;
import net.risesoft.entity.DocumentFile;
import net.risesoft.entity.ImageFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.entity.TestingItem;
import net.risesoft.entity.VideoFile;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Person;
import net.risesoft.service.AudioFileService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsFileService;
import net.risesoft.service.DocumentFileService;
import net.risesoft.service.ImageFileService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.service.VideoFileService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.service.Y9FileStoreService;

@Slf4j
@RequiredArgsConstructor
@Component
public class ArchiveDetection {

    private static PersonApi personApi;
    private static Y9FileStoreService y9FileStoreService;
    private static DataAssetsFileService dataAssetsFileService;
    private static MetadataConfigService metadataConfigService;
    private static CategoryTableService categoryTableService;
    private static DocumentFileService documentFileService;
    private static ImageFileService imageFileService;
    private static AudioFileService audioFileService;
    private static VideoFileService videoFileService;

    public ArchiveDetection(PersonApi personApi, Y9FileStoreService y9FileStoreService,
        DataAssetsFileService dataAssetsFileService, MetadataConfigService metadataConfigService,
        CategoryTableService categoryTableService, DocumentFileService documentFileService,
        ImageFileService imageFileService, AudioFileService audioFileService, VideoFileService videoFileService) {
        ArchiveDetection.personApi = personApi;
        ArchiveDetection.y9FileStoreService = y9FileStoreService;
        ArchiveDetection.dataAssetsFileService = dataAssetsFileService;
        ArchiveDetection.metadataConfigService = metadataConfigService;
        ArchiveDetection.categoryTableService = categoryTableService;
        ArchiveDetection.documentFileService = documentFileService;
        ArchiveDetection.imageFileService = imageFileService;
        ArchiveDetection.audioFileService = audioFileService;
        ArchiveDetection.videoFileService = videoFileService;
    }

    /**
     * 检测电子文件完整性
     * 
     * @param file
     * @param expectedHash
     * @return
     */
    public static boolean checkAuthenticity(File file, String expectedHash) {
        try {
            String fileHash = calculateHash(file);
            return fileHash.equals(expectedHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.error("检测电子文件完整性时出错", e);
            return false;
        }
    }

    // 计算文件哈希值
    public static String calculateHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 检测卷内文件完整性
     *
     * @param archiveEntries
     */
    // public void checkForNoFileLevelArchives(List<Archives> archiveEntries) {
    // for (Archives entry : archiveEntries) {
    // if (!entry.getFileGrade()) {
    // throw new RuntimeException("案卷内没有著录卷内文件");
    // }
    // }
    // }

    /**
     * 检测当前档案是否有电子文件、文件上传人是否存在、档案文件上传时保存的哈希值和存储文件的哈希值是否一致
     *
     * @param archives
     */
    public static void checkArchivesFile(String testingInfoId, DataAssets archives) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<DataAssetsFile> fileList = dataAssetsFileService.findByArchivesId(archives.getArchivesId());
        if (null != fileList && fileList.size() > 0) {
            // TODO 验证档案文件上传人是否存在
            for (DataAssetsFile file : fileList) {
                Person person = personApi.get(tenantId, file.getPersonId()).getData();
                String testingReason = "";
                if (null == person) {
                    testingReason = "档案文件上传人不存在";
                } else {
                    testingReason = "";
                }
                createTestingItem(testingInfoId, "GD-1-1", testingReason);
                // 验证档案文件上传时保存的哈希值和存储文件的哈希值是否一致
                String fileStoreId = file.getFileStoreId();
                String fileHashValue = getArchiveFileHashValue(fileStoreId);
                if (StringUtils.isNotBlank(fileHashValue) && fileHashValue.equals(file.getFileHash())) {
                    createTestingItem(testingInfoId, "GD-1-10", "");
                    createTestingItem(testingInfoId, "GD-1-14", "");
                    createTestingItem(testingInfoId, "GD-2-2", "");
                } else {
                    createTestingItem(testingInfoId, "GD-1-10", "档案文件上传时保存的哈希值和存储文件的哈希值不一致");
                    createTestingItem(testingInfoId, "GD-1-14", "档案文件上传时保存的哈希值和存储文件的哈希值不一致");
                    createTestingItem(testingInfoId, "GD-2-2", "档案文件上传时保存的哈希值和存储文件的哈希值不一致");
                }

            }
            createTestingItem(testingInfoId, "GD-1-11-1", "");
            createTestingItem(testingInfoId, "GD-2-1", "");
        } else {
            createTestingItem(testingInfoId, "GD-1-1", "档案没有附件");
            createTestingItem(testingInfoId, "GD-1-10", "档案没有附件");
            createTestingItem(testingInfoId, "GD-1-14", "档案没有附件");
            createTestingItem(testingInfoId, "GD-1-11-1", "档案没有附件");
            createTestingItem(testingInfoId, "GD-2-1", "档案没有附件");
            createTestingItem(testingInfoId, "GD-2-2", "档案没有附件");
            createTestingItem(testingInfoId, "GD-3-4", "档案没有附件");
        }
    }

    private static TestingItem createTestingItem(String testingInfoId, String testingNo, String testingReason) {
        TestingItem testingItem = new TestingItem();
        testingItem.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        testingItem.setTestingInfoId(testingInfoId);
        testingItem.setTestingNo(testingNo);
        if (StringUtils.isNotBlank(testingReason)) {
            testingItem.setTestingResult("不通过");
            testingItem.setTestingReason(testingReason);
        } else {
            testingItem.setTestingResult("通过");
        }
        return testingItem;
    }

    private static String getArchiveFileHashValue(String fileStoreId) {
        String filePath = Y9Context.getWebRootRealPath() + "upload";
        File file = new File(filePath);
        FileOutputStream fileOutputStream = null;
        try {
            byte[] bytes = y9FileStoreService.downloadFileToBytes(fileStoreId);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            System.out.println("文件哈希值：" + ArchiveDetection.calculateHash(file));
            return ArchiveDetection.calculateHash(file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 检测数据表字段是否与元数据配置匹配(以防有直接在数据库修改数据表字段)
     *
     * @param testingInfoId
     * @param archives
     * @return
     */
    public static void checkDataTable(String testingInfoId, DataAssets archives) {
        List<Map<String, Object>> list_map = EntityOrTableUtils.getTableFieldList(DataAssets.class, "");
        if (archives.getCategoryCode().equals(CategoryEnums.DOCUMENT.getEnName())) {
            list_map.addAll(EntityOrTableUtils.getTableFieldList(DocumentFile.class, ""));
        } else if (archives.getCategoryCode().equals(CategoryEnums.IMAGE.getEnName())) {
            list_map.addAll(EntityOrTableUtils.getTableFieldList(ImageFile.class, ""));
        } else if (archives.getCategoryCode().equals(CategoryEnums.AUDIO.getEnName())) {
            list_map.addAll(EntityOrTableUtils.getTableFieldList(AudioFile.class, ""));
        } else if (archives.getCategoryCode().equals(CategoryEnums.VIDEO.getEnName())) {
            list_map.addAll(EntityOrTableUtils.getTableFieldList(VideoFile.class, ""));
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(archives.getCategoryCode());
            if (null != categoryTable) {
                list_map.addAll(EntityOrTableUtils.getTableFieldList(null, categoryTable.getTableName()));
            }
        }
        for (Map<String, Object> map : list_map) {
            List<MetadataConfig> metadataConfigList = metadataConfigService.listByViewType(archives.getCategoryCode());
            for (MetadataConfig metadataConfig : metadataConfigList) {
                String fieldName = map.get("fieldName").toString();
                String fieldType = map.get("fieldType").toString();
                int fieldLength = Integer.parseInt(map.get("fieldLength").toString());
                if (fieldName.equals(metadataConfig.getColumnName())) {
                    if (!fieldType.equals(metadataConfig.getDataType())) {
                        createTestingItem(testingInfoId, "GD-1-3", "元数据项数据类型、格式不符合要求");
                    }
                    if (fieldLength != metadataConfig.getFieldLength()) {
                        createTestingItem(testingInfoId, "GD-1-2", "元数据项数据长度不符合要求");
                    }
                }
            }
        }
    }

    /**
     * 检测必填项
     *
     * @param testingInfoId
     * @param archives 【GD-1-4、GD-1-5、GD-1-6、GD-2-11、GD-2-3、GD-2-4、GD-2-6】查询档案元数据的必填值
     */
    public static void checkRequiredMetadata(String testingInfoId, DataAssets archives) {
        List<MetadataConfig> metadataConfigList =
            metadataConfigService.getMetadataCheckedRequiredConfigList(archives.getCategoryCode());
        Map<String, Object> map = new HashMap<>();
        if (archives.getCategoryCode().equals(CategoryEnums.DOCUMENT.getEnName())) {
            map.putAll(documentFileService.findByDetailId(archives.getArchivesId()));
        } else if (archives.getCategoryCode().equals(CategoryEnums.IMAGE.getEnName())) {
            map.putAll(imageFileService.findByDetailId(archives.getArchivesId()));
        } else if (archives.getCategoryCode().equals(CategoryEnums.AUDIO.getEnName())) {
            map.putAll(audioFileService.findByDetailId(archives.getArchivesId()));
        } else if (archives.getCategoryCode().equals(CategoryEnums.VIDEO.getEnName())) {
            map.putAll(videoFileService.findByDetailId(archives.getArchivesId()));
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(archives.getCategoryCode());
            if (null != categoryTable) {
                List<Map<String, Object>> list_categoryTable = categoryTableService
                    .getTableData(categoryTable.getTableName(), archives.getArchivesId().toString());
                for (Map<String, Object> map_categoryTable : list_categoryTable) {
                    map.putAll(map_categoryTable);
                }
            }
        }
        for (MetadataConfig metadataConfig : metadataConfigList) {
            for (String key : map.keySet()) {
                String keyColumnName = key.toUpperCase();
                if (keyColumnName.equals(metadataConfig.getColumnName())) {
                    if (map.get(key) == null || map.get(key).toString().trim().equals("")) {
                        // 【GD-1-4、GD-1-5、GD-1-6、GD-2-11、GD-2-3、GD-2-4、GD-2-6】查询档案元数据的必填值
                        createTestingItem(testingInfoId, "GD-1-4",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-1-5",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-1-6",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-2-11",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-2-3",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-2-4",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                        createTestingItem(testingInfoId, "GD-2-6",
                            "元数据项数据检测必填项【" + metadataConfig.getDisPlayName() + "】不能为空");
                    } else {
                        if (metadataConfig.getDataType().equals("varchar")
                            || metadataConfig.getDataType().equals("char")
                            || metadataConfig.getDataType().equals("text")
                            || metadataConfig.getDataType().equals("longtext")
                            || metadataConfig.getDataType().equals("mediumtext")
                            || metadataConfig.getDataType().equals("tinytext")
                            || metadataConfig.getDataType().equals("binary")
                            || metadataConfig.getDataType().equals("varbinary")
                            || metadataConfig.getDataType().equals("blob")
                            || metadataConfig.getDataType().equals("longblob")
                            || metadataConfig.getDataType().equals("mediumblob")
                            || metadataConfig.getDataType().equals("tinyblob")) {
                            if (!isGarbled(map.get(key).toString(), StandardCharsets.UTF_8)) {
                                createTestingItem(testingInfoId, "GD-3-1",
                                    "元数据项数据检测【" + metadataConfig.getDisPlayName() + "】为乱码");
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isGarbled(String input, Charset charset) {
        byte[] bytes = input.getBytes(charset); // 将字符串编码为字节数组
        String decodedString = new String(bytes, charset); // 重新解码为字符串

        // 比较两个字符串是否相等
        return !input.equals(decodedString); // 如果不相等，则认为是乱码
    }
}
