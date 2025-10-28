package net.risesoft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileDataUtil {

    /**
     * 获取文件大小
     * 
     * @param fileS
     * @return
     */
    public static String getFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSize = "0B";
        if (fileS == 0) {
            return fileSize;
        }
        if (fileS < 1024) {
            fileSize = df.format((double)fileS) + "B";
        } else if (fileS < 1048576) {
            fileSize = df.format((double)fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSize = df.format((double)fileS / 1048576) + "MB";
        } else {
            fileSize = df.format((double)fileS / 1073741824) + "GB";
        }
        return fileSize;
    }

    /**
     * 保存文件到指定位置
     * 
     * @param file
     * @param savePath
     * @return
     */
    public static boolean saveFile(MultipartFile file, String savePath, String fileName) {
        try {
            File f = new File(savePath);
            if (!f.exists()) {
                f.mkdirs();
            }
            if (StringUtils.isBlank(fileName)) {
                fileName = file.getOriginalFilename();
            }
            String filePath = savePath + fileName;
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
