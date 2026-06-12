package net.risesoft.controller.dto;

import lombok.Data;

import net.risesoft.support.OrderRequest;

/**
 * 收藏列表查询参数
 */
@Data
public class CollectListQueryDTO {

    private String id;

    private String searchName;

    private String listType;

    private OrderRequest orderRequest;
}
