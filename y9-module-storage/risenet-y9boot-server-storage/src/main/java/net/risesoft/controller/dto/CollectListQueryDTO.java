package net.risesoft.controller.dto;

import lombok.Data;

import net.risesoft.support.OrderProp;
import net.risesoft.support.OrderRequest;

/**
 * 收藏列表查询参数
 */
@Data
public class CollectListQueryDTO {

    private String id;

    private String searchName;

    private String listType;

    /**
     * 排序字段，对应 OrderProp 枚举值，如 FILE_NAME、FILE_SIZE、UPDATE_TIME、CREATE_TIME
     */
    private String orderProp;

    /**
     * 是否升序，默认 true
     */
    private Boolean orderAsc;

    /**
     * 获取排序请求对象，前端参数 orderProp/orderAsc 扁平映射到此方法
     */
    public OrderRequest getOrderRequest() {
        OrderProp prop = OrderProp.FILE_NAME;
        if (orderProp != null) {
            try {
                prop = OrderProp.valueOf(orderProp);
            } catch (IllegalArgumentException e) {
                // 无效值时使用默认
            }
        }
        boolean asc = orderAsc == null || orderAsc;
        return new OrderRequest(prop, asc);
    }
}
