package net.risesoft.service;

import net.risesoft.entity.StorageUploadLimit;

public interface StorageUploadLimitService {

    /**
     * 获取指定文件列表类型的上传大小限制<br>
     * 先按 fileType 查询；若该类型尚未单独配置，则回落到升级前遗留的全局默认配置；都不存在时返回 null
     *
     * @param fileType 文件列表类型(my=我的文件/dept=部门文件/public=公共文件/report=文件上报)
     * @return
     */
    public StorageUploadLimit getUploadLimit(String fileType);

    /**
     * 保存或更新上传大小限制（每种文件列表类型一条记录）
     *
     * @param fileType 文件列表类型(my/dept/public/report)
     * @param uploadSize 上传大小限制(单位:字节)
     * @param operatorId 操作人id
     * @param operatorName 操作人
     * @return
     */
    public StorageUploadLimit saveOrUpdate(String fileType, Long uploadSize, String operatorId, String operatorName);
}
