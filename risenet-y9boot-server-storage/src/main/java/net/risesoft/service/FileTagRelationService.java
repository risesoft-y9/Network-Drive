package net.risesoft.service;

import java.util.List;

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
}
