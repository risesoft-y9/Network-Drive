package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.FileNodeCollect;

public interface FileNodeCollectRepository
    extends JpaRepository<FileNodeCollect, String>, JpaSpecificationExecutor<FileNodeCollect> {

    @Query("select c.fileId from FileNodeCollect c where c.collectUserId=?1 and c.parentId in(?2)")
    public List<String> findByCollectUserIdAndParentIdIn(String userId, List<String> listNames);

    @Query("select c.fileId from FileNodeCollect c where c.parentId=?2 and c.listName =?3 ")
    public List<String> openCollectFolder(String parentId, String listName);

    @Query("select c.fileId from FileNodeCollect c where c.collectUserId=?1 ")
    public List<String> openCollectFolder(String userId);

    FileNodeCollect findByFileIdAndCollectUserIdAndListName(String fileId, String collectUserId, String listName);
}
