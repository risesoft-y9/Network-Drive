package net.risesoft.service.Impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileNodeCollect;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.FileNodeCollectRepository;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.service.FileNodeCollectService;
import net.risesoft.support.FileNodeType;
import net.risesoft.y9.Y9LoginUserHolder;

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
    public void save(String fileId, String collectRoute) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        FileNode node = fileNodeRepository.findById(fileId).orElse(null);
        if (null != node && StringUtils.isNotBlank(node.getId())) {
            // findChilren(node);

            FileNodeCollect collect = new FileNodeCollect();
            collect.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            collect.setFileId(fileId);
            collect.setFileName(node.getName());
            collect.setParentId(node.getListType());
            collect.setCollectRoute(collectRoute);
            collect.setListName(node.getListType());
            collect.setCollectUserId(userInfo.getPersonId());
            collect.setCollectTime(new Date());
            fileNodeCollectRepository.save(collect);
        }
    }

    @Override
    public void cancelCollect(String fileId) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        FileNode node = fileNodeRepository.findById(fileId).orElse(null);
        if (null != node && StringUtils.isNotBlank(node.getId())) {
            // cancelChildren(node);
            FileNodeCollect collect =
                fileNodeCollectRepository.findByFileIdAndCollectUserId(node.getId(), userInfo.getPersonId());
            if (null != collect && StringUtils.isNotBlank(collect.getId())) {
                fileNodeCollectRepository.delete(collect);
            }
        }
    }

    public void cancelChildren(FileNode fileNode) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileNode> fileNodeList = fileNodeRepository.findByParentId(fileNode.getId());
        if (null != fileNodeList && !fileNodeList.isEmpty()) {
            for (FileNode node : fileNodeList) {
                if (node.getFileType().equals(FileNodeType.FOLDER.getValue())) {
                    cancelChildren(node);
                }
                FileNodeCollect collect = fileNodeCollectRepository
                    .findByFileIdAndCollectUserIdAndListName(node.getId(), userInfo.getPersonId(), node.getListType());
                if (null != collect && StringUtils.isNotBlank(collect.getId())) {
                    fileNodeCollectRepository.delete(collect);
                }
            }
        }
    }

    @Override
    public boolean findByCollectUserIdAndFileIdAndListName(String collectUserId, String fileId, String listName) {
        boolean isCollect = false;
        try {
            FileNodeCollect coolect =
                fileNodeCollectRepository.findByFileIdAndCollectUserIdAndListName(fileId, collectUserId, listName);
            if (null != coolect && StringUtils.isNotBlank(coolect.getId())) {
                isCollect = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCollect;
    }

    @Override
    public boolean findByCollectUserIdAndFileId(String collectUserId, String fileId) {
        boolean isCollect = false;
        try {
            FileNodeCollect coolect = fileNodeCollectRepository.findByFileIdAndCollectUserId(fileId, collectUserId);
            if (null != coolect && StringUtils.isNotBlank(coolect.getId())) {
                isCollect = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCollect;
    }

    private void findChilren(FileNode fileNode) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileNode> fileNodeList = fileNodeRepository.findByParentId(fileNode.getId());
        if (null != fileNodeList && !fileNodeList.isEmpty()) {
            for (FileNode node : fileNodeList) {
                if (node.getFileType().equals(FileNodeType.FOLDER.getValue())) {
                    findChilren(node);
                }
                FileNodeCollect collect = new FileNodeCollect();
                collect.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                collect.setFileId(node.getId());
                collect.setFileName(node.getName());
                collect.setParentId(node.getParentId());
                // collect.setCollectRoute(collectRoute);
                collect.setListName(node.getListType());
                collect.setCollectUserId(userInfo.getPersonId());
                collect.setCollectTime(new Date());
                fileNodeCollectRepository.save(collect);
            }
        }
    }
}
