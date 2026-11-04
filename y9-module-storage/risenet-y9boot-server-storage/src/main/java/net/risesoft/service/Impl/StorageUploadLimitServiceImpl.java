package net.risesoft.service.Impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.StorageUploadLimit;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.StorageUploadLimitRepository;
import net.risesoft.service.StorageUploadLimitService;
import net.risesoft.support.FileListType;

@Service
@RequiredArgsConstructor
public class StorageUploadLimitServiceImpl implements StorageUploadLimitService {

    private final StorageUploadLimitRepository storageUploadLimitRepository;

    /**
     * 归一化文件列表类型，为空时按"我的文件"处理
     *
     * @param fileType 文件列表类型
     * @return
     */
    private String normalizeFileType(String fileType) {
        return StringUtils.isBlank(fileType) ? FileListType.MY.getValue() : fileType.trim();
    }

    @Override
    public StorageUploadLimit getUploadLimit(String fileType) {
        String type = normalizeFileType(fileType);
        StorageUploadLimit sul = storageUploadLimitRepository.findTopByFileTypeOrderByIdAsc(type);
        if (null != sul) {
            return sul;
        }
        // 该类型尚未单独配置时，回落到升级前遗留的全局默认配置
        return storageUploadLimitRepository.findTopByFileTypeIsNullOrderByIdAsc();
    }

    @Override
    @Transactional(readOnly = false)
    public StorageUploadLimit saveOrUpdate(String fileType, Long uploadSize, String operatorId, String operatorName) {
        String type = normalizeFileType(fileType);
        StorageUploadLimit sul = storageUploadLimitRepository.findTopByFileTypeOrderByIdAsc(type);
        Date now = new Date();
        if (null == sul) {
            sul = new StorageUploadLimit();
            sul.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            sul.setCreateTime(now);
        }
        sul.setFileType(type);
        sul.setUploadSize(uploadSize);
        sul.setOperatorId(operatorId);
        sul.setOperatorName(operatorName);
        sul.setUpdateTime(now);
        return storageUploadLimitRepository.save(sul);
    }

}
