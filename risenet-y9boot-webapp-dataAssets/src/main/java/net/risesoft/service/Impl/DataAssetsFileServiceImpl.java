package net.risesoft.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataAssetsFile;
import net.risesoft.repository.DataAssetsFileRepository;
import net.risesoft.service.DataAssetsFileService;

@Service("fileInfoService")
@RequiredArgsConstructor
public class DataAssetsFileServiceImpl implements DataAssetsFileService {

    private final DataAssetsFileRepository dataAssetsFileRepository;

    @Override
    public DataAssetsFile save(DataAssetsFile archivesFile) {
        return dataAssetsFileRepository.save(archivesFile);
    }

    @Override
    public boolean isArchivesFileExists(Long archiveId, String fileName) {
        DataAssetsFile archivesFile = dataAssetsFileRepository.findByArchivesIdAndFileName(archiveId, fileName);
        return null != archivesFile && null != archivesFile.getDetailId();
    }

    @Override
    public Integer getMaxTabIndex(Long archiveId) {
        return dataAssetsFileRepository.getMaxTabIndex(archiveId);
    }

    @Override
    public List<DataAssetsFile> findByDetailId(Long archivesId) {
        return dataAssetsFileRepository.findByDetailId(archivesId);
    }

    @Override
    public void deleteFile(String id) {
        dataAssetsFileRepository.deleteById(id);
    }

    @Override
    public DataAssetsFile findById(String id) {
        return dataAssetsFileRepository.findById(id).orElse(null);
    }
}
