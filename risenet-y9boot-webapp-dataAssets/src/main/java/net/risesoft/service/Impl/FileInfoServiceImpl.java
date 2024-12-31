package net.risesoft.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileInfo;
import net.risesoft.repository.FileInfoRepository;
import net.risesoft.service.FileInfoService;

@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl implements FileInfoService {

    private final FileInfoRepository fileInfoRepository;

    @Override
    public FileInfo addFileInfo(FileInfo fileInfo) {
        return fileInfoRepository.save(fileInfo);
    }
}
