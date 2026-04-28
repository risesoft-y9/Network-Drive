package net.risesoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import net.risesoft.entity.FileNode;
import net.risesoft.pojo.Y9Result;
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
     * @param parentId
     * @param fileName
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

    /**
     * 重置文件夹密码
     * 
     * @param folder
     * @return
     */
    Y9Result<Object> resetFolderPassword(FileNode folder);

    /**
     * 设置文件夹密码
     *
     * @param folder
     * @return
     */
    Y9Result<Object> setFolderPassword(FileNode folder);

    /**
     * 设置文件链接密码
     *
     * @param id
     * @param encryption
     * @param linkPassword
     * @return
     */
    Y9Result<Object> setLinkPwd(String id, boolean encryption, String linkPassword);

    /**
     * 保存大文件上传信息
     * 
     * @param parentId
     * @param fileName
     * @param fileExtension
     * @param fileSize
     * @param y9FileStoreId
     * @param listType
     * @return
     */
    Map<String, Object> saveFileNodeAndCapacity(String parentId, String fileName, String fileExtension, long fileSize,
        String y9FileStoreId, String listType);
}
