package net.risesoft.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.y9.Y9Context;

@Configuration
@EnableScheduling
@Slf4j
public class DeleteChunkFileUtil {

    Calendar calendar = Calendar.getInstance();

    public DeleteChunkFileUtil() {

    }

    /**
     * 定时任务，定时删除临时分块或合并文件
     */
    @Scheduled(cron = "0 0 */5 * * ?") // 每5个小时执行一次
    // @Scheduled(cron = "0 */2 * * * ?") // 每1个小时执行一次
    public void deleteChunkFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            LOGGER.info("******************定时任务删除临时分块或合并文件开始：{}******************", sdf.format(date));
            calendar.clear();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, -5);// 当前时间减去5个小时，当前时间的前五个小时
            // calendar.add(Calendar.MINUTE, -2);// 当前时间减去5个小时，当前时间的前五个小时
            long timeInMillis = calendar.getTimeInMillis();

            String chunckPath = Y9Context.getWebRootRealPath() + "upload";
            LOGGER.info("********************chunckPath：{}********************", chunckPath);

            File realFile = new File(chunckPath);
            deleteFileOrFolder(realFile);
            LOGGER.info("******************定时任务删除临时分块或合并文件结束：{}*", sdf.format(date));
        } catch (Exception e) {
            LOGGER.error("定时任务删除临时分块或合并文件异常：{}", e.getMessage());
        }
    }

    public void deleteFileOrFolder(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                // 如果是文件夹，递归删除文件夹中的所有文件和子文件夹
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteFileOrFolder(f);
                    }
                }
            }
            // 尝试删除文件或空文件夹
            if (file.delete()) {
                LOGGER.info("已删除: {}", file.getAbsolutePath());
            } else {
                // 如果删除失败，尝试更改文件权限后再删除
                try {
                    file.setWritable(true);
                    file.setReadable(true);
                    file.setExecutable(true);
                    if (file.delete()) {
                        LOGGER.info("已删除: {}", file.getAbsolutePath());
                    } else {
                        LOGGER.info("删除失败: {}", file.getAbsolutePath());
                    }
                } catch (SecurityException e) {
                    LOGGER.info("权限更改失败: {}", file.getAbsolutePath());
                }
            }
        } else {
            LOGGER.info("路径不存在: {}", file.getAbsolutePath());
        }
    }

}
