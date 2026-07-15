package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.controller.dto.FileShareLinkDTO;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileShareLink;
import net.risesoft.enums.StorageAuditLogEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.AuditLogEvent;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.repository.FileShareLinkRepository;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileShareLinkService;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9StringUtil;
import net.risesoft.y9.util.base64.Y9Base64Util;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileShareLinkServiceImpl implements FileShareLinkService {

    private final FileShareLinkRepository fileShareLinkRepository;
    private final FileNodeRepository fileNodeRepository;
    private final FileNodeService fileNodeService;
    @Value("${y9.common.vueStorageBaseUrl}")
    private String linkUrl;

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
    public List<FileShareLink> findMyShareLinks() {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileShareLink> list =
            fileShareLinkRepository.findByCreateUserIdOrderByCreateTimeDesc(userInfo.getPersonId());
        // 批量查询文件名称并回填，排除已删除的文件
        if (!list.isEmpty()) {
            List<String> fileIds = list.stream().map(FileShareLink::getFileId).distinct().collect(Collectors.toList());
            Map<String,
                FileNode> fileNodeMap = fileNodeRepository.findAllById(fileIds)
                    .stream()
                    .collect(Collectors.toMap(FileNode::getId, Function.identity()));
            list = list.stream().filter(link -> {
                FileNode fileNode = fileNodeMap.get(link.getFileId());
                if (fileNode != null) {
                    link.setFileNode(fileNode);
                    link.setLinkPassword(Y9Base64Util.decode(link.getLinkPassword()));
                    link.setLinkUrl(linkUrl);
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    @Transactional
    public Y9Result<FileShareLink> createLink(FileShareLinkDTO fileShareLinkDTO) {
        try {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            FileShareLink fileShareLink = new FileShareLink();
            fileShareLink.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileShareLink.setFileId(fileShareLinkDTO.getFileId());
            fileShareLink.setLinkPassword(Y9Base64Util.encode(fileShareLinkDTO.getLinkPassword()));
            fileShareLink.setLinkKey(fileShareLinkDTO.getLinkKey());
            fileShareLink.setCreateTime(new Date());
            fileShareLink.setCreateUserId(userInfo.getPersonId());
            fileShareLink.setValidPeriod(fileShareLinkDTO.getValidPeriod());
            fileShareLink.setExpireTime(new SimpleDateFormat("yyyy-MM-dd").parse(fileShareLinkDTO.getExpireTime()));
            fileShareLink = this.save(fileShareLink);
            fileShareLink.setLinkUrl(linkUrl);
            FileNode fileNode = fileNodeService.findById(fileShareLinkDTO.getFileId());
            AuditLogEvent auditLogEvent = AuditLogEvent.builder()
                .action(StorageAuditLogEnum.SHARE_LINK_CREATE.getAction())
                .description(Y9StringUtil.format(StorageAuditLogEnum.SHARE_LINK_CREATE.getDescription(),
                    userInfo.getName(), fileNode.getName()))
                .objectId(fileNode.getId())
                .oldObject(fileNode)
                .currentObject(null)
                .build();
            Y9Context.publishEvent(auditLogEvent);
            return Y9Result.success(fileShareLink, "直链链接创建成功！");
        } catch (Exception e) {
            LOGGER.error("直链链接创建失败！", e);
            return Y9Result.failure("直链链接创建失败！");
        }
    }

    @Override
    @Transactional
    public Y9Result<Object> deleteAllById(List<String> idList) {
        try {
            fileShareLinkRepository.deleteAllById(idList);
            return Y9Result.success(null, "取消分享链接成功！");
        } catch (Exception e) {
            LOGGER.error("取消分享链接失败！", e);
            return Y9Result.failure("取消分享链接失败！");
        }
    }
}
