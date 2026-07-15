package net.risesoft.service;

import java.util.List;

import net.risesoft.controller.dto.FileShareLinkDTO;
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
     * @param fileShareLinkDTO
     * @return
     */
    Y9Result<FileShareLink> createLink(FileShareLinkDTO fileShareLinkDTO);

    /**
     * 获取当前用户分享的直链列表（按创建时间倒序）
     *
     * @return
     */
    List<FileShareLink> findMyShareLinks();

    /**
     * 根据ID取消分享链接
     *
     * @param idList
     * @return
     */
    Y9Result<Object> deleteAllById(List<String> idList);
}
