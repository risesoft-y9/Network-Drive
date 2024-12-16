package net.risesoft.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.ArchivesTestingInfo;
import net.risesoft.repository.ArchivesTestingInfoRepository;
import net.risesoft.service.ArchivesTestingInfoService;

@Service("archivesTestingInfoService")
@RequiredArgsConstructor
public class ArchivesTestingInfoServiceImpl implements ArchivesTestingInfoService {

    private final ArchivesTestingInfoRepository archivesTestingInfoRepository;

    @Override
    public ArchivesTestingInfo save(ArchivesTestingInfo archivesTestingInfo) {
        return archivesTestingInfoRepository.save(archivesTestingInfo);
    }
}
