package net.risesoft.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 存储模块审计日志枚举类
 *
 * @author risesoft
 */
@Getter
@RequiredArgsConstructor
public enum StorageAuditLogEnum {
    // 文件节点操作
    FILE_UPLOAD("FILE_UPLOAD", "文件 [{}] 上传"),
    FOLDER_CREATE("FOLDER_CREATE", "文件夹 [{}] 创建"),
    FILE_DELETE("FILE_DELETE", "文件 [{}] 删除"),
    FOLDER_DELETE("FOLDER_DELETE", "文件夹 [{}] 删除"),
    FILE_MOVE("FILE_MOVE", "文件 [{}] 移动到 [{}]"),
    FOLDER_MOVE("FOLDER_MOVE", "文件夹 [{}] 移动到 [{}]"),
    FILE_RENAME("FILE_RENAME", "文件 [{}] 重命名为 [{}]"),
    FOLDER_RENAME("FOLDER_RENAME", "文件夹 [{}] 重命名为 [{}]"),
    FILE_RESTORE("FILE_RESTORE", "文件 [{}] 恢复"),
    FOLDER_RESTORE("FOLDER_RESTORE", "文件夹 [{}] 恢复"),
    FILE_PERMANENT_DELETE("FILE_PERMANENT_DELETE", "文件 [{}] 永久删除"),
    FOLDER_PERMANENT_DELETE("FOLDER_PERMANENT_DELETE", "文件夹 [{}] 永久删除"),
    RECYCLE_BIN_EMPTY("RECYCLE_BIN_EMPTY", "[{}]清空回收站"),

    // 文件夹密码操作
    FOLDER_SET_PASSWORD("FOLDER_SET_PASSWORD", "文件夹 [{}] 设置密码"),
    FOLDER_CANCEL_PASSWORD("FOLDER_CANCEL_PASSWORD", "文件夹 [{}] 取消密码"),
    FOLDER_DECRYPT_PASSWORD("FOLDER_DECRYPT_PASSWORD", "文件夹 [{}] 解密"),
    FOLDER_RESET_PASSWORD("FOLDER_RESET_PASSWORD", "文件夹 [{}] 重置密码"),
    // 文件收藏操作
    FILE_COLLECT("FILE_COLLECT", "收藏文件 [{}]"),
    FILE_CANCEL_COLLECT("FILE_CANCEL_COLLECT", "取消收藏文件 [{}]"),

    // 文件标签操作
    TAG_CREATE("TAG_CREATE", "标签 [{}] 创建"),
    TAG_DELETE("TAG_DELETE", "标签 [{}] 删除"),
    TAG_UPDATE("TAG_UPDATE", "标签 [{}] 更新为 [{}]"),
    FILE_ADD_TAG("FILE_ADD_TAG", "文件 [{}] 添加标签 [{}]"),
    FILE_REMOVE_TAG("FILE_REMOVE_TAG", "文件 [{}] 移除标签 [{}]"),
    FILE_ADD_CUSTOM_TAG("FOLDER_ADD_CUSTOM_TAG", "文件夹 [{}] 添加自定义标签 [{}]"),
    FILE_UPDATE_CUSTOM_TAG("FOLDER_ADD_CUSTOM_TAG", "文件夹 [{}] 更新自定义标签 [{}]"),

    // 文件分享操作
    FILE_SHARE("FILE_SHARE", "文件 [{}] 分享给组织 [{}]"),
    FOLDER_SHARE("FOLDER_SHARE", "文件夹 [{}] 分享给组织 [{}]"),
    FILE_CANCEL_SHARE("FILE_CANCEL_SHARE", "文件 [{}] 取消分享"),
    FOLDER_CANCEL_SHARE("FOLDER_CANCEL_SHARE", "文件夹 [{}] 取消分享"),
    FILE_PUBLIC_SHARE("FILE_PUBLIC_SHARE", "文件 [{}] 公开分享给组织 [{}]"),
    FILE_PUBLIC("FILE_PUBLIC", "文件 [{}] 设为公开"),
    FOLDER_PUBLIC("FOLDER_PUBLIC", "文件夹 [{}] 设为公开"),
    FILE_CANCEL_PUBLIC("FILE_CANCEL_PUBLIC", "文件 [{}] 取消公开"),

    // 分享链接操作
    SHARE_LINK_CREATE("SHARE_LINK_CREATE", "[{}] 创建文件 [{}] 分享链接"),
    SHARE_LINK_DELETE("SHARE_LINK_DELETE", "删除分享链接 [{}]"),
    SHARE_LINK_SET_PASSWORD("SHARE_LINK_SET_PASSWORD", "分享链接 [{}] 设置密码"),
    SHARE_LINK_DOWNLOAD("SHARE_LINK_DOWNLOAD", "分享链接 [{}] 下载文件 [{}]"),

    // 文件下载操作
    FILE_DOWNLOAD("FILE_DOWNLOAD", "下载文件 [{}]"),

    // 存储容量操作
    STORAGE_CAPACITY_UPDATE("STORAGE_CAPACITY_UPDATE", "操作人 [{}] 对 [{}]存储容量更新为 [{}]");

    private final String action;
    private final String description;
}
