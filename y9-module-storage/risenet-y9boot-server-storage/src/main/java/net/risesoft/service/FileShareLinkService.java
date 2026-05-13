package net.risesoft.service;

import net.risesoft.entity.FileShareLink;
import net.risesoft.pojo.Y9Result;

public interface FileShareLinkService {

    /**
     * 根据分享链接的key获取分享链接对象
     *
     * @param linkKey
     * @return
     */
    FileShareLink findByLinkKey(String linkKey);

    /**
     * 保存分享链接对象
     * 
     * @param fileShareLink
     */
    FileShareLink save(FileShareLink fileShareLink);

    /**
     * 创建分享链接
     *
     * @param fileId
     * @param linkPassword
     * @return
     */
    Y9Result<FileShareLink> createLink(String fileId, String linkPassword);
}
