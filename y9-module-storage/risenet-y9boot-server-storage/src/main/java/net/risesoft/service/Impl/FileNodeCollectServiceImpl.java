package net.risesoft.service.Impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileNodeCollect;
import net.risesoft.enums.StorageAuditLogEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.AuditLogEvent;
import net.risesoft.repository.FileNodeCollectRepository;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.service.FileNodeCollectService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9StringUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileNodeCollectServiceImpl implements FileNodeCollectService {

    private final FileNodeCollectRepository fileNodeCollectRepository;

    private final FileNodeRepository fileNodeRepository;

    @Override
    public List<String> getCollectList(String userId, List<String> listNames) {
        return fileNodeCollectRepository.findByCollectUserIdAndParentIdIn(userId, listNames);
    }

    @Override
    public List<String> getCollectList(String userId) {
        return fileNodeCollectRepository.openCollectFolder(userId);
    }

    @Override
    public List<String> openCollectFolder(String parentId, String listName) {
        return fileNodeCollectRepository.openCollectFolder(parentId, listName);
    }

    @Override
    @Transactional
    public void save(String fileId, String collectRoute) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        if (userInfo == null || StringUtils.isBlank(userInfo.getPersonId())) {
            LOGGER.warn("保存收藏失败：当前用户未登录");
            return;
        }
        FileNode node = fileNodeRepository.findById(fileId).orElse(null);
        if (null != node && StringUtils.isNotBlank(node.getId())) {
            // 检查是否已经收藏，防止重复记录
            FileNodeCollect existing =
                fileNodeCollectRepository.findByFileIdAndCollectUserId(fileId, userInfo.getPersonId());
            if (existing != null && StringUtils.isNotBlank(existing.getId())) {
                LOGGER.info("文件[{}]已被用户[{}]收藏，跳过重复收藏", fileId, userInfo.getPersonId());
                return;
            }

            FileNodeCollect collect = new FileNodeCollect();
            collect.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            collect.setFileId(fileId);
            collect.setFileName(node.getName());
            collect.setParentId(node.getParentId());
            collect.setCollectRoute(collectRoute);
            collect.setListName(node.getListType());
            collect.setCollectUserId(userInfo.getPersonId());
            collect.setCollectTime(new Date());
            fileNodeCollectRepository.save(collect);

            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.FILE_COLLECT.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.FILE_COLLECT.getDescription(), node.getName()))
                .objectId(fileId)
                .oldObject(collect)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
        }
    }

    @Override
    @Transactional
    public void cancelCollect(String fileId) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        FileNode node = fileNodeRepository.findById(fileId).orElse(null);
        if (null != node && StringUtils.isNotBlank(node.getId())) {
            FileNodeCollect collect =
                fileNodeCollectRepository.findByFileIdAndCollectUserId(node.getId(), userInfo.getPersonId());
            if (null != collect && StringUtils.isNotBlank(collect.getId())) {
                fileNodeCollectRepository.delete(collect);
                AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                    .action(StorageAuditLogEnum.FILE_CANCEL_COLLECT.getAction())
                    .description(
                        Y9StringUtil.format(StorageAuditLogEnum.FILE_CANCEL_COLLECT.getDescription(), node.getName()))
                    .objectId(fileId)
                    .oldObject(collect)
                    .currentObject(null)
                    .build();
                Y9Context.publishEvent(auditLogEvent);
            }
        }
    }

    @Override
    public boolean findByCollectUserIdAndFileIdAndListName(String collectUserId, String fileId, String listName) {
        try {
            FileNodeCollect collect =
                fileNodeCollectRepository.findByFileIdAndCollectUserIdAndListName(fileId, collectUserId, listName);
            if (null != collect && StringUtils.isNotBlank(collect.getId())) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("查询收藏记录失败, fileId={}, userId={}, listName={}", fileId, collectUserId, listName, e);
        }
        return false;
    }

    @Override
    public boolean findByCollectUserIdAndFileId(String collectUserId, String fileId) {
        try {
            FileNodeCollect collect = fileNodeCollectRepository.findByFileIdAndCollectUserId(fileId, collectUserId);
            if (null != collect && StringUtils.isNotBlank(collect.getId())) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("查询收藏记录失败, fileId={}, userId={}", fileId, collectUserId, e);
        }
        return false;
    }

}
