package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.FileTagRelation;
import net.risesoft.pojo.Y9Result;

public interface FileTagRelationService {

    /**
     * 为文件添加标签
     *
     * @param fileIdList 文件id
     * @param tagIdList 标签id
     */
    void addFileTagToFile(List<String> fileIdList, List<String> tagIdList);

    /**
     * 通过标签id查询文件id集合
     * 
     * @param tagIds
     * @return
     */
    List<String> findFileIdsByTagIds(List<String> tagIds);

    /**
     * 通过标签id查询文件标签关系
     *
     * @param tagIds
     * @return
     */
    List<FileTagRelation> findByTagIds(List<String> tagIds);

    /**
     * 单文件关联标签
     * 
     * @param tagId
     * @param fileId
     * @return
     */
    Y9Result<Object> simpleFileToTag(String tagId, String fileId);

    /**
     * 删除文件标签关系
     *
     * @param fileIdList
     */
    void deleteFileTagRelation(List<String> fileIdList);

    /**
     * 删除文件标签关系
     *
     * @param fileTagRelation
     */
    void delete(FileTagRelation fileTagRelation);
}
