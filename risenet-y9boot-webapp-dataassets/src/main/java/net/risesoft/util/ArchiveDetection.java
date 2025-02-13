package net.risesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.service.AudioFileService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsFileService;
import net.risesoft.service.DocumentFileService;
import net.risesoft.service.ImageFileService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.service.VideoFileService;
import net.risesoft.y9.Y9Context;
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

    public static boolean isGarbled(String input, Charset charset) {
        byte[] bytes = input.getBytes(charset); // 将字符串编码为字节数组
        String decodedString = new String(bytes, charset); // 重新解码为字符串

        // 比较两个字符串是否相等
        return !input.equals(decodedString); // 如果不相等，则认为是乱码
    }
}
