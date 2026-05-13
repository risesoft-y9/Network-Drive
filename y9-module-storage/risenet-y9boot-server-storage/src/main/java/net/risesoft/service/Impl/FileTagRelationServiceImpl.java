package net.risesoft.service.Impl;

import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileTag;
import net.risesoft.entity.FileTagRelation;
import net.risesoft.enums.StorageAuditLogEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.AuditLogEvent;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileTagRelationRepository;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileTagRelationService;
import net.risesoft.service.FileTagService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9StringUtil;

@RequiredArgsConstructor
@Service
public class FileTagRelationServiceImpl implements FileTagRelationService {

    private final FileTagRelationRepository fileTagRelationRepository;
    @Lazy
    private final FileNodeService fileNodeService;
    private final FileTagService fileTagService;

    @Override
    @Transactional
    public void addFileTagToFile(List<String> fileIdList, List<String> tagIdList) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        for (String fileId : fileIdList) {
            FileNode fileNode = fileNodeService.findById(fileId);
            for (String tagId : tagIdList) {
                FileTag fileTag = fileTagService.findById(tagId);
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
                    AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                        .action(StorageAuditLogEnum.FILE_ADD_TAG.getAction())
                        .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_ADD_TAG.getDescription(),
                            fileNode.getName(), fileTag.getTagName()))
                        .objectId(fileTag.getId())
                        .oldObject(fileTag)
                        .currentObject(null)
                        .build();
                    Y9Context.publishEvent(auditLogEvent);
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
            FileNode fileNode = fileNodeService.findById(fileId);
            FileTag fileTag = fileTagService.findById(tagId);
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.FILE_ADD_TAG.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_ADD_TAG.getDescription(), fileNode.getName(),
                    fileTag.getTagName()))
                .objectId(fileTag.getId())
                .oldObject(fileTag)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
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
