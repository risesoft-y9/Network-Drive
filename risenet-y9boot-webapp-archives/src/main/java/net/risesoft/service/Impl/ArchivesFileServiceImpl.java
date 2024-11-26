package net.risesoft.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.ArchivesFile;
import net.risesoft.repository.ArchivesFileRepository;
import net.risesoft.service.ArchivesFileService;

@Service("fileInfoService")
@RequiredArgsConstructor
public class ArchivesFileServiceImpl implements ArchivesFileService {

    private final ArchivesFileRepository archivesFileRepository;

    @Override
    public ArchivesFile save(ArchivesFile archivesFile) {
        return archivesFileRepository.save(archivesFile);
    }

    @Override
    public boolean isArchivesFileExists(Long archiveId, String fileName) {
        ArchivesFile archivesFile = archivesFileRepository.findByArchivesIdAndFileName(archiveId, fileName);
        return null != archivesFile && null != archivesFile.getArchivesId();
    }

    @Override
    public Integer getMaxTabIndex(Long archiveId) {
        return archivesFileRepository.getMaxTabIndex(archiveId);
    }

    @Override
    public List<ArchivesFile> findByArchivesId(Long archivesId) {
        return archivesFileRepository.findByArchivesId(archivesId);
    }

    @Override
    public void deleteFile(String id) {
        archivesFileRepository.deleteById(id);
    }

    @Override
    public ArchivesFile findById(String id) {
        return archivesFileRepository.findById(id).orElse(null);
    }
}
