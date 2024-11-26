package net.risesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.Archives;
import net.risesoft.entity.ArchivesFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.service.ArchivesFileService;

@Slf4j
@RequiredArgsConstructor
public class ArchiveDetection {

    private final ArchivesFileService archivesFileService;

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
     * 检测电子文件完整性
     *
     * @param archivesList
     */
    public void checkForNoElectronicOriginal(List<Archives> archivesList) {
        for (Archives archives : archivesList) {
            List<ArchivesFile> fileList = archivesFileService.findByArchivesId(archives.getArchivesId());
            if (null == fileList && !fileList.isEmpty()) {
                throw new RuntimeException("档案内没有上传电子原文");
            }
        }
    }

    /**
     * 检测必填项
     *
     * @param archive
     * @param metadataConfig
     */
    public void checkRequired(JSONObject archive, MetadataConfig metadataConfig) throws JSONException {
        if (metadataConfig.getIsRecord() == 1 && metadataConfig.getIsRecordNull() == 1) {
            String property = archive.getString(metadataConfig.getColumnName());
            if (StringUtils.isEmpty(property)) {
                throw new RuntimeException("检测元数据必著项时出错");
            }
        }
    }
}
