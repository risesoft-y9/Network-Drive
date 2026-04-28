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

import net.risesoft.entity.FileTag;
import net.risesoft.entity.FileTagRelation;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileTagRelationRepository;
import net.risesoft.repository.FileTagRepository;
import net.risesoft.service.FileTagService;
import net.risesoft.y9.Y9LoginUserHolder;

@Service
@RequiredArgsConstructor
public class FileTagServiceImpl implements FileTagService {

    private final FileTagRepository fileTagRepository;
    private final FileTagRelationRepository fileTagRelationRepository;

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
        return Y9Result.success("保存标签成功");
    }

    @Override
    @Transactional
    public void deleteFileTag(List<String> idList) {
        for (String id : idList) {
            fileTagRepository.deleteById(id);
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
    public Y9Result<Object> updateFileTag(FileTag fileTag, String fileId) {
        FileTag existingTag = this.findById(fileTag.getId());
        if (existingTag != null) {
            existingTag.setTagName(fileTag.getTagName());
            existingTag.setTagColor(fileTag.getTagColor());
            existingTag.setUpdateTime(new Date());
            fileTagRepository.save(existingTag);
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
            fileTagRelationRepository.delete(fileTagRelation);
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
        return Y9Result.success("保存标签成功");
    }

}
