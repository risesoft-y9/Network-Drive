package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.FileDownLoadRecord;

public interface FileDownLoadRecordService {

    /**
     * 获取文件下载记录
     * 
     * @param fileId
     * @param page
     * @param rows
     * @return
     */
    Page<FileDownLoadRecord> getFileDownLoadRecord(String fileId, int page, int rows);

    /**
     * 保存下载记录
     * 
     * @param fileDownLoadRecord
     */
    void save(FileDownLoadRecord fileDownLoadRecord);
}
