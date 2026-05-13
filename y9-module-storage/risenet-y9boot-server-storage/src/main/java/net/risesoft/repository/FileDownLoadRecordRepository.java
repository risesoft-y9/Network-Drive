package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.FileDownLoadRecord;

public interface FileDownLoadRecordRepository
    extends JpaRepository<FileDownLoadRecord, String>, JpaSpecificationExecutor<FileDownLoadRecord> {

    @Query("select count(t.id) from FileDownLoadRecord t where t.fileId=?1")
    public Integer getFileDownLoadCounts(String fileId);

    FileDownLoadRecord findByFileId(String fileId);

    public Page<FileDownLoadRecord> findByFileId(String fileId, Pageable pageable);
}
