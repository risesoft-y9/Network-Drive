package net.risesoft.controller.dto;

import java.util.List;

import lombok.Data;

import net.risesoft.support.FileNodeType;
import net.risesoft.support.OrderRequest;

/**
 * 文件列表查询参数
 */
@Data
public class FileListQueryDTO {

    private String id;

    private FileNodeType fileNodeType;

    private String searchName;

    private List<String> tagIds;

    private String startTime;

    private String endTime;

    private String listType;

    private OrderRequest orderRequest;
}
