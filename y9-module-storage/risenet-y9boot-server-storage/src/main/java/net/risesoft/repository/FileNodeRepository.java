package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.FileNode;

public interface FileNodeRepository extends JpaRepository<FileNode, String>, JpaSpecificationExecutor<FileNode> {

    boolean existsByUserIdAndParentIdAndNameAndDeletedFalse(String userId, String parentId, String fileName);

    List<FileNode> findByIdInAndParentIdAndListTypeAndDeletedFalse(List<String> fileNodeIdList, String parentId,
        String listType);

    List<FileNode> findByParentIdAndDeletedFalseOrderByNameDesc(String parentId);

    List<FileNode> findByUserIdAndDeletedTrue(String userId);

    List<FileNode> findByUserIdAndIdAndDeletedFalseOrderByNameAsc(String userId, String id);

    List<FileNode> findByUserIdAndParentId(String id, String id1);

    List<FileNode> findByParentId(String parentId);

    List<FileNode> findByIdInAndNameLikeAndDeletedFalse(List<String> ids, String searchName);

    List<FileNode> findByUserIdAndParentIdIsNullAndDeletedFalseOrderByNameDesc(String userId);

    List<FileNode> findByUserIdContainingAndNameContainingAndFileTypeAndDeletedFalse(String userId, String name,
        Integer fileType);

    FileNode findByUserIdStartingWithAndIdAndDeletedFalseOrderByNameDesc(String userId, String parentId);

    List<FileNode> findByUserIdStartingWithAndParentIdAndDeletedFalseOrderByNameAsc(String userId, String parentId);

    List<FileNode> findByUserIdStartingWithAndParentIdAndDeletedFalseOrderByNameDesc(String userId, String parentId);

    List<FileNode> findByUserIdStartingWithAndParentIdAndNameStartingWithAndDeletedFalseOrderByCreateTimeDesc(
        String userId, String parentId, String name);

    List<FileNode> findByUserIdStartingWithAndParentIdIsNullAndDeletedFalseOrderByNameAsc(String userId);

    Page<FileNode> findByUserIdStartingWithAndParentIdIsNullAndDeletedFalseOrderByNameAsc(String userId,
        Pageable pageable);

    List<FileNode> findByUserIdStartingWithAndParentIdIsNullAndNameStartingWithAndDeletedFalseOrderByNameAsc(
        String userId, String name);
}
