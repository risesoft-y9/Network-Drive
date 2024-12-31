package net.risesoft.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataAssetsTestingInfo;
import net.risesoft.repository.DataAssetsTestingInfoRepository;
import net.risesoft.service.DataAssetsTestingInfoService;

@Service("archivesTestingInfoService")
@RequiredArgsConstructor
public class DataAssetsTestingInfoServiceImpl implements DataAssetsTestingInfoService {

    private final DataAssetsTestingInfoRepository dataAssetsTestingInfoRepository;

    @Override
    public DataAssetsTestingInfo save(DataAssetsTestingInfo dataAssetsTestingInfo) {
        return dataAssetsTestingInfoRepository.save(dataAssetsTestingInfo);
    }
}
