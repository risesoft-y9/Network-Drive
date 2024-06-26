package net.risesoft.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.FileDownLoadRecord;
import net.risesoft.repository.FileDownLoadRecordRepository;
import net.risesoft.service.FileDownLoadRecordService;

@Service
@RequiredArgsConstructor
public class FileDownLoadRecordServiceImpl implements FileDownLoadRecordService {

    private final FileDownLoadRecordRepository fileDownLoadRecordRepository;

    @Override
    public Page<FileDownLoadRecord> getFileDownLoadRecord(String fileId, int page, int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Direction.DESC, "downLoadTime");
        return fileDownLoadRecordRepository.findByFileId(fileId, pageable);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(FileDownLoadRecord fileDownLoadRecord) {
        fileDownLoadRecordRepository.save(fileDownLoadRecord);
    }

}
