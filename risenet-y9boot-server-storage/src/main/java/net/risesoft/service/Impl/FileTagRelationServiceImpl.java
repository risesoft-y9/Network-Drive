package net.risesoft.service.Impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileTagRelation;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileTagRelationRepository;
import net.risesoft.service.FileTagRelationService;
import net.risesoft.y9.Y9LoginUserHolder;

@RequiredArgsConstructor
@Service
public class FileTagRelationServiceImpl implements FileTagRelationService {

    private final FileTagRelationRepository fileTagRelationRepository;

    @Override
    @Transactional
    public void addFileTagToFile(List<String> fileIdList, List<String> tagIdList) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        for (String fileId : fileIdList) {
            for (String tagId : tagIdList) {
                FileTagRelation existingRelation = fileTagRelationRepository.findByFileIdAndTagId(fileId, tagId);
                if (existingRelation == null) {
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
    }

    @Override
    public List<String> findFileIdsByTagIds(List<String> tagIds) {
        return fileTagRelationRepository.findFileIdsByTagIds(tagIds);
    }

    @Override
    public List<FileTagRelation> findByTagIds(List<String> tagIds) {
        return fileTagRelationRepository.findByTagIdIn(tagIds);
    }

    @Override
    @Transactional
    public Y9Result<Object> simpleFileToTag(String tagId, String fileId) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        try {
            FileTagRelation fileTagRelation = new FileTagRelation();
            fileTagRelation.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileTagRelation.setFileId(fileId);
            fileTagRelation.setTagId(tagId);
            fileTagRelation.setOperatorId(userInfo.getPersonId());
            fileTagRelation.setOperateTime(new Date());
            fileTagRelationRepository.save(fileTagRelation);
            return Y9Result.success("单文件添加文件标签关系成功");
        } catch (Exception e) {
            return Y9Result.failure("单文件添加文件标签关系失败");
        }
    }

    @Override
    @Transactional
    public void deleteFileTagRelation(List<String> fileIdList) {
        fileTagRelationRepository.deleteByFileIdIn(fileIdList);
    }

    @Override
    @Transactional
    public void delete(FileTagRelation fileTagRelation) {
        fileTagRelationRepository.delete(fileTagRelation);
    }
}
