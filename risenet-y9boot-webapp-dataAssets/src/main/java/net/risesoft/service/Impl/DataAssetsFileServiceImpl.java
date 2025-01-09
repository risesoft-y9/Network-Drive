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
    public DataAssetsFile save(DataAssetsFile dataAssetsFile) {
        return dataAssetsFileRepository.save(dataAssetsFile);
    }

    @Override
    public boolean isFileExists(Long detailId, String fileName) {
        DataAssetsFile file = dataAssetsFileRepository.findByDetailIdAndFileName(detailId, fileName);
        return null != file && null != file.getDetailId();
    }

    @Override
    public Integer getMaxTabIndex(Long detailId) {
        return dataAssetsFileRepository.getMaxTabIndex(detailId);
    }

    @Override
    public List<DataAssetsFile> findByDetailId(Long detailId) {
        return dataAssetsFileRepository.findByDetailId(detailId);
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
