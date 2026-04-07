package net.risesoft.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.FileTagRelation;

public interface FileTagRelationRepository
    extends JpaRepository<FileTagRelation, String>, JpaSpecificationExecutor<FileTagRelation> {

    /**
     * 根据多个标签ID查询同时关联这些标签的文件ID列表
     * 
     * @param tagIds 标签ID列表
     * @return 文件ID列表
     */
    @Query("SELECT ftr.fileId FROM FileTagRelation ftr WHERE ftr.tagId IN (?1) " + "GROUP BY ftr.fileId ")
    List<String> findFileIdsByTagIdList(List<String> tagIds);

    default List<String> findFileIdsByTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return findFileIdsByTagIdList(tagIds);
    }

    /**
     * 根据文件ID查询关联的标签ID列表
     * 
     * @param fileId 文件ID
     * @return 标签ID列表
     */
    @Query("SELECT ftr.tagId FROM FileTagRelation ftr WHERE ftr.fileId = ?1")
    List<String> findTagIdsByFileId(String fileId);

    FileTagRelation findByFileIdAndTagId(String fileId, String tagId);

    FileTagRelation findByFileIdAndTagIdAndOperatorId(String fileId, String tagId, String operatorId);

    List<FileTagRelation> findByTagIdIn(List<String> tagId);

    @Query("DELETE FROM FileTagRelation WHERE fileId IN (?1)")
    void deleteByFileIdIn(List<String> fileIdList);

}
