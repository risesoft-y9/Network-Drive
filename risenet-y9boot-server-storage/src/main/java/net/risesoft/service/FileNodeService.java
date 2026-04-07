package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import net.risesoft.entity.FileNode;
import net.risesoft.support.FileNodeType;
import net.risesoft.support.OrderRequest;

public interface FileNodeService {

    void cancelShare(List<String> fileNodeIdList);

    /**
     * 获取回收站的文件列表
     *
     * @param userId
     * @return
     */
    List<FileNode> deletedList(String userId);

    void emptyRecycleBin();

    /**
     * 根据Id查询文件实体
     *
     * @param id
     * @return
     */
    FileNode findById(String id);

    FileNode getParent(String parentId);

    void logicDelete(List<String> idList);

    void move(List<String> idList, String targetId);

    /**
     * 彻底删除文件
     *
     * @param idList
     */
    void permanentlyDelete(List<String> idList);

    List<FileNode> recursiveToRoot(String parentId);

    /**
     * 还原删除的文件
     *
     * @param idList
     */
    void restore(List<String> idList);

    /**
     * 当前节点是否存在同名文件
     *
     * @param fileNode
     */
    boolean isFileNodeExists(String parentId, String fileName);

    /**
     * 保存文件夹实体
     *
     * @param fileNode
     */
    FileNode saveFolder(FileNode fileNode);

    FileNode saveNode(FileNode fileNode);

    Map<String, Object> saveUploadFile(MultipartFile file, String folderId, String listType);

    void share(List<String> fileNodeIdList);

    List<FileNode> subFolderList(String id);

    List<FileNode> subPublicFolderList(String id);

    /**
     * 根据条件查询文件列表
     * 
     * @param positionId
     * @param id
     * @param fileType
     * @param searchName
     * @param listType
     * @param orderRequest
     * @return
     */
    List<FileNode> subList(String positionId, String id, FileNodeType fileType, String searchName, String listType,
        OrderRequest orderRequest);

    List<FileNode> subList(String positionId, String id, FileNodeType fileType, String searchName, List<String> tagIds,
        String listType, OrderRequest orderRequest);

    List<FileNode> subCollectList(String id, String searchName, String listType, OrderRequest orderRequest);

    List<FileNode> subCollectList(List<String> id, String search, OrderRequest orderRequest);

    /**
     * 根据多个标签ID和搜索条件查询文件列表
     * 
     * @param positionId 位置ID
     * @param id 文件ID
     * @param fileNodeType 文件类型
     * @param searchName 搜索名称
     * @param tagIds 标签ID列表
     * @param listType 列表类型
     * @param orderRequest 排序请求
     * @return 文件节点列表
     */
    List<FileNode> listFilesByTagIds(String positionId, String id, FileNodeType fileNodeType, String searchName,
        List<String> tagIds, String listType, OrderRequest orderRequest);

    /**
     * 根据条件查询公共文件列表
     *
     * @param id
     * @param tagIds
     * @param fileType
     * @param searchName
     * @param startTime
     * @param endTime
     * @param listType
     * @param orderRequest
     * @return
     */
    List<FileNode> subPublicList(String id, List<String> tagIds, FileNodeType fileType, String searchName,
        String startTime, String endTime, String listType, OrderRequest orderRequest);

    /**
     * 根据条件查询公共管理文件列表
     *
     * @param id
     * @param tagIds
     * @param fileType
     * @param searchName
     * @param startTime
     * @param endTime
     * @param listType
     * @param orderRequest
     * @return
     */
    List<FileNode> subManageList(String id, List<String> tagIds, FileNodeType fileType, String searchName,
        String startTime, String endTime, String listType, OrderRequest orderRequest);

    // 添加标签相关方法
    List<FileNode> subListByTag(String positionId, String parentId, String tagId, String listType,
        OrderRequest orderRequest);

}
