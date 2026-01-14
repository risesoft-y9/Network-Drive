package net.risesoft.service.Impl;

import net.risesoft.entity.DataRecentEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.DataRecentRepository;
import net.risesoft.service.DataRecentService;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecentServiceImpl implements DataRecentService {

    private final DataRecentRepository dataRecentRepository;

    @Override
    @Async
    public void saveAsync(DataRecentEntity dataRecentEntity) {
        if (dataRecentEntity == null) {
            return;
        }
        if (StringUtils.isBlank(dataRecentEntity.getId())) {
            dataRecentEntity.setId(Y9IdGenerator.genId());
        }
        if (dataRecentEntity.getCreateDate() == null) {
            dataRecentEntity.setCreateDate(new Date());
        }
        dataRecentRepository.save(dataRecentEntity);
    }

    @Override
    public Page<DataRecentEntity> findAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createDate"));
        return dataRecentRepository.findAll(pageable);
    }

}
