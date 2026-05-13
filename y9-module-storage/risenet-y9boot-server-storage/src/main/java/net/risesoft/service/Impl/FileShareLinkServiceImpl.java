package net.risesoft.service.Impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileShareLink;
import net.risesoft.enums.StorageAuditLogEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.AuditLogEvent;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileShareLinkRepository;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileShareLinkService;
import net.risesoft.util.FileUtils;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9StringUtil;
import net.risesoft.y9.util.base64.Y9Base64Util;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileShareLinkServiceImpl implements FileShareLinkService {

    private final FileShareLinkRepository fileShareLinkRepository;
    private final FileNodeService fileNodeService;

    @Override
    public FileShareLink findByLinkKey(String linkKey) {
        return fileShareLinkRepository.findByLinkKey(linkKey);
    }

    @Override
    @Transactional
    public FileShareLink save(FileShareLink fileShareLink) {
        return fileShareLinkRepository.save(fileShareLink);
    }

    @Override
    @Transactional
    public Y9Result<FileShareLink> createLink(String fileId, String linkPassword) {
        try {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String randomKey = FileUtils.generateRandomNumber();
            String link = Y9Base64Util.encode(randomKey);
            FileShareLink fileShareLink = new FileShareLink();
            fileShareLink.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileShareLink.setFileId(fileId);
            fileShareLink.setLinkPassword(Y9Base64Util.encode(linkPassword));
            fileShareLink.setLinkKey(link);
            fileShareLink.setCreateTime(new Date());
            fileShareLink.setCreateUserId(userInfo.getPersonId());
            fileShareLink = this.save(fileShareLink);
            FileNode fileNode = fileNodeService.findById(fileId);
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.SHARE_LINK_CREATE.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.SHARE_LINK_CREATE.getDescription(),
                    userInfo.getName(), fileNode.getName()))
                .objectId(fileNode.getId())
                .oldObject(fileNode)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
            return Y9Result.success(fileShareLink, "直链加密链接创建成功！");
        } catch (Exception e) {
            LOGGER.error("直链加密链接创建失败！", e);
            return Y9Result.failure("直链加密链接创建失败！");
        }
    }
}
