package net.risesoft.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.Chunk;
import net.risesoft.y9public.service.Y9FileStoreService;

@Slf4j
@RequiredArgsConstructor
public class FileUtils {

    private final Y9FileStoreService y9FileStoreService;

    public static String generatePath(String uploadFolder, Chunk chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append(uploadFolder).append("/").append(chunk.getIdentifier());
        // 判断uploadFolder/identifier 路径是否存在，不存在则创建
        if (!Files.isWritable(Paths.get(sb.toString()))) {
            LOGGER.info("path not exist,create path: {}", sb);
            try {
                Files.createDirectories(Paths.get(sb.toString()));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return sb.append("/").append(chunk.getFilename()).append("-").append(chunk.getChunkNumber()).toString();
    }

    /**
     * 文件合并
     *
     * @param targetFile
     * @param folder
     */
    public static void merge(String targetFile, String folder, String fileName) {
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
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static String convertFileSize(Long fileSize) {
        Objects.requireNonNull(fileSize, "size");
        double size = fileSize;
        String unit = "B";

        if (size >= 1024) {
            size /= 1024;
            unit = "KB";
        }

        if (size >= 1024) {
            size /= 1024;
            unit = "MB";
        }

        if (size >= 1024) {
            size /= 1024;
            unit = "GB";
        }

        if (size >= 1024) {
            size /= 1024;
            unit = "TB";
        }
        return String.format("%.2f %s", size, unit);
    }

    public static String generateRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        String randomNumber = String.valueOf(secureRandom.nextLong()) + String.valueOf(secureRandom.nextLong());
        randomNumber = randomNumber.replaceAll("-", "").substring(0, 24);
        System.out.println("生成的20位随机数是: " + randomNumber);
        return randomNumber;
    }
}
