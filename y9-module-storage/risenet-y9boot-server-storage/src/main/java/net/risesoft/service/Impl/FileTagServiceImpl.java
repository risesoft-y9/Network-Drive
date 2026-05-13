package net.risesoft.service.Impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileTagRelationRepository;
import net.risesoft.repository.FileTagRepository;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileTagService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9StringUtil;

@Service
@RequiredArgsConstructor
public class FileTagServiceImpl implements FileTagService {

    private final FileTagRepository fileTagRepository;
    private final FileTagRelationRepository fileTagRelationRepository;
    private final FileNodeService fileNodeService;

    @Override
    public Y9Page<FileTag> list(Integer page, Integer rows, String tagName) {
        Pageable pageable = PageRequest.of(page - 1, rows, Sort.Direction.DESC, "createTime");
        Page<FileTag> pageList = null;
        if (StringUtils.isNotBlank(tagName)) {
            pageList = fileTagRepository.findByTagNameLike("%" + tagName + "%", pageable);
        } else {
            pageList = fileTagRepository.findAll(pageable);
        }
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取标签数据成功");
    }

    @Override
    public List<FileTag> listByTagName(String tagName) {
        List<FileTag> list = new ArrayList<>();
        if (StringUtils.isNotBlank(tagName)) {
            list = fileTagRepository.findByTagNameLike("%" + tagName + "%");
        } else {
            list = fileTagRepository.findAll();
        }
        return list;
    }

    @Override
    public List<FileTag> listAll() {
        return fileTagRepository.findAll();
    }

    @Override
    @Transactional
    public Y9Result<Object> save(FileTag fileTag) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        // 检查标签名称是否已存在
        List<FileTag> existingTags = fileTagRepository.findByTagName(fileTag.getTagName());

        if (StringUtils.isBlank(fileTag.getId())) {
            if (!existingTags.isEmpty()) {
                Y9Result.failure("标签名称已存在");
            }
            fileTag.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileTag.setCreateTime(new Date());
            fileTag.setCreateId(userInfo.getPersonId());
            fileTag.setCreateName(userInfo.getName());
            Integer tabIndex = fileTagRepository.getMaxTabIndex();
            fileTag.setTabIndex(null != tabIndex ? tabIndex + 1 : 1);
        } else {
            // 更新逻辑 - 从数据库获取现有记录并更新属性
            if (!existingTags.isEmpty()) {
                Y9Result.failure("标签名称已存在");
            }
            FileTag existingTag = fileTagRepository.findById(fileTag.getId()).orElse(null);
            if (existingTag != null) {
                existingTag.setTagName(fileTag.getTagName());
                existingTag.setTagColor(fileTag.getTagColor());
                existingTag.setUpdateTime(new Date());
                fileTag = existingTag;
            }
        }
        fileTagRepository.save(fileTag);
        AuditLogEvent auditLogEvent = AuditLogEvent.builder()
            .action(StorageAuditLogEnum.TAG_CREATE.getAction())
            .description(Y9StringUtil.format(StorageAuditLogEnum.TAG_CREATE.getDescription(), fileTag.getTagName()))
            .objectId(fileTag.getId())
            .oldObject(fileTag)
            .currentObject(null)
            .build();
        Y9Context.publishEvent(auditLogEvent);
        return Y9Result.success("保存标签成功");
    }

    @Override
    @Transactional
    public void deleteFileTag(List<String> idList) {
        for (String id : idList) {
            fileTagRepository.deleteById(id);
            FileTag fileTag = this.findById(id);
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.TAG_DELETE.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.TAG_DELETE.getDescription(), fileTag.getTagName()))
                .objectId(fileTag.getId())
                .oldObject(fileTag)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
        }
    }

    @Override
    public List<FileTag> getTagsByFileId(String fileId) {
        // 通过 FileTagRelation 表查询文件关联的标签ID列表
        List<String> tagIds = fileTagRelationRepository.findTagIdsByFileId(fileId);

        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据标签ID列表查询标签详情
        return fileTagRepository.findAllById(tagIds);
    }

    @Override
    public FileTag findById(String id) {
        return fileTagRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Y9Result<Object> updateCustomTag(FileTag fileTag, String fileId) {
        FileTag existingTag = this.findById(fileTag.getId());
        if (existingTag != null) {
            existingTag.setTagName(fileTag.getTagName());
            existingTag.setTagColor(fileTag.getTagColor());
            existingTag.setUpdateTime(new Date());
            fileTagRepository.save(existingTag);
            FileNode fileNode = fileNodeService.findById(fileId);
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.FILE_UPDATE_CUSTOM_TAG.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_UPDATE_CUSTOM_TAG.getDescription(),
                    fileNode.getName(), fileTag.getTagName()))
                .objectId(fileTag.getId())
                .oldObject(fileTag)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
            return Y9Result.success("更新标签成功");
        }
        return Y9Result.failure("标签不存在");
    }

    @Override
    @Transactional
    public void removeTagFromFile(String fileId, String tagId, String operatorId) {
        FileTagRelation fileTagRelation =
            fileTagRelationRepository.findByFileIdAndTagIdAndOperatorId(fileId, tagId, operatorId);
        if (fileTagRelation != null) {
            FileNode fileNode = fileNodeService.findById(fileId);
            FileTag fileTag = this.findById(tagId);
            fileTagRelationRepository.delete(fileTagRelation);
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.FILE_REMOVE_TAG.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_REMOVE_TAG.getDescription(),
                    fileNode.getName(), fileTag.getTagName()))
                .objectId(fileTag.getId())
                .oldObject(fileTag)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
        }
    }

    @Override
    @Transactional
    public Y9Result<Object> saveCustomTag(FileTag fileTag, String fileId) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String operatorId = userInfo.getPersonId(), operatorName = userInfo.getName();

        // 检查标签名称是否已存在
        List<FileTag> existingTags = fileTagRepository.findByTagName(fileTag.getTagName());

        if (StringUtils.isBlank(fileTag.getId())) {
            if (!existingTags.isEmpty()) {
                FileTag existingTag = existingTags.get(0);
                return Y9Result.success(existingTag, "标签名称已存在");
            }
            fileTag.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileTag.setCreateTime(new Date());
            fileTag.setCreateId(operatorId);
            fileTag.setCreateName(operatorName);
            Integer tabIndex = fileTagRepository.getMaxTabIndex();
            fileTag.setTabIndex(null != tabIndex ? tabIndex + 1 : 1);
        }
        fileTagRepository.save(fileTag);
        FileTagRelation fileTagRelation = new FileTagRelation();
        fileTagRelation.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        fileTagRelation.setFileId(fileId);
        fileTagRelation.setTagId(fileTag.getId());
        fileTagRelation.setOperatorId(operatorId);
        fileTagRelation.setOperateTime(new Date());
        fileTagRelationRepository.save(fileTagRelation);
        FileNode fileNode = fileNodeService.findById(fileId);
        AuditLogEvent auditLogEvent = AuditLogEvent.builder()
            .action(StorageAuditLogEnum.FILE_ADD_CUSTOM_TAG.getAction())
            .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_ADD_CUSTOM_TAG.getDescription(),
                fileNode.getName(), fileTag.getTagName()))
            .objectId(fileTag.getId())
            .oldObject(fileTag)
            .currentObject(null)
            .build();
        Y9Context.publishEvent(auditLogEvent);
        return Y9Result.success("保存标签成功");
    }

}
