package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.StorageUploadLimit;

public interface StorageUploadLimitRepository
    extends JpaRepository<StorageUploadLimit, String>, JpaSpecificationExecutor<StorageUploadLimit> {

    /**
     * 按文件列表类型获取上传大小限制配置
     *
     * @param fileType 文件列表类型(my/dept/public/report)
     * @return
     */
    StorageUploadLimit findTopByFileTypeOrderByIdAsc(String fileType);

    /**
     * 获取升级前遗留的全局默认配置（FILETYPE 为空），用于尚未单独配置文件列表类型的兜底
     *
     * @return
     */
    StorageUploadLimit findTopByFileTypeIsNullOrderByIdAsc();
}
