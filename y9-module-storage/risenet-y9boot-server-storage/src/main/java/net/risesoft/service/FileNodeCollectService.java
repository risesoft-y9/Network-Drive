package net.risesoft.service;

import java.util.List;

public interface FileNodeCollectService {

    /**
     * 获取所有收藏的文件和文件夹
     * 
     * @param userId
     * @param listNames
     * @return
     */
    List<String> getCollectList(String userId, List<String> listNames);

    List<String> getCollectList(String userId);

    /**
     * 打开收藏目录
     * 
     * @param parentId
     * @param listNames
     * @return
     */
    List<String> openCollectFolder(String parentId, String listNames);

    /**
     * 保存收藏的文件
     * 
     * @param fileId
     * @param collectRoute
     */
    void save(String fileId, String collectRoute);

    /**
     * 取消收藏
     * 
     * @param fileId
     */
    void cancelCollect(String fileId);

    /**
     * 查询当前是否收藏过文件夹或者文件
     * 
     * @param collectUserId
     * @param fileId
     * @param listName
     * @return
     */
    boolean findByCollectUserIdAndFileIdAndListName(String collectUserId, String fileId, String listName);

    /**
     * 查询当前是否收藏过文件夹或者文件
     * 
     * @param collectUserId
     * @param fileId
     * @return
     */
    boolean findByCollectUserIdAndFileId(String collectUserId, String fileId);
}
