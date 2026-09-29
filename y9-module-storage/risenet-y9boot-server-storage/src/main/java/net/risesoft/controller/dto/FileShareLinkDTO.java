package net.risesoft.controller.dto;

import lombok.Data;

@Data
public class FileShareLinkDTO {

    private String id;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 链接期限
     */
    private String linkExpireTime;

    /**
     * 链接密钥
     */
    private String linkKey;

    /**
     * 链接密码
     */
    private String linkPassword;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 浏览次数
     */
    private Integer browseCount;

    /**
     * 有效期设置
     */
    private String validPeriod;

    /**
     * 过期时间
     */
    private String expireTime;
}
