package net.risesoft.service.Impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileTagRelation;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.FileTagRelationRepository;
import net.risesoft.service.FileTagRelationService;
import net.risesoft.y9.Y9LoginUserHolder;

@RequiredArgsConstructor
@Service
public class FileTagRelationServiceImpl implements FileTagRelationService {

    private final FileTagRelationRepository fileTagRelationRepository;

    @Override
    public void addFileTagToFile(List<String> fileIdList, List<String> tagIdList) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        for (String fileId : fileIdList) {
            for (String tagId : tagIdList) {
                // 添加文件标签关系
                FileTagRelation fileTagRelation = new FileTagRelation();
                fileTagRelation.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                fileTagRelation.setFileId(fileId);
                fileTagRelation.setTagId(tagId);
                fileTagRelation.setOperatorId(userInfo.getPersonId());
                fileTagRelation.setOperateTime(new Date());
                fileTagRelationRepository.save(fileTagRelation);
            }
        }
    }

    @Override
    public List<String> findFileIdsByTagIds(List<String> tagIds) {
        return fileTagRelationRepository.findFileIdsByTagIds(tagIds);
    }
}
