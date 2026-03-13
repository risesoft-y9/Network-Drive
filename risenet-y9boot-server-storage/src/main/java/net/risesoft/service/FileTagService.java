package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileTag;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

public interface FileTagService {

    Y9Page<FileTag> list(Integer page, Integer rows, String tagName);

    List<FileTag> listAll();

    Y9Result<Object> save(FileTag fileTag);

    void deleteFileTag(List<String> idList);

    List<FileTag> getTagsByFileId(String fileId);

    // 标签管理
    FileTag createTag(String tagName, String creatorId);

    List<FileTag> getAllTags();

    FileTag getTagById(String tagId);

    void deleteTag(String tagId);

    // 文件标签操作
    void addTagToFile(String fileId, String tagId, String operatorId);

    void removeTagFromFile(String fileId, String tagId, String operatorId);

    void updateFileTag(FileTag fileTag);

    List<FileNode> getFilesByTagId(String tagId);

    // 批量操作
    void batchAddTagToFile(List<String> fileIds, String tagId, String operatorId);

    void batchRemoveTagFromFile(List<String> fileIds, String tagId, String operatorId);

}
