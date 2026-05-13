package net.risesoft.model.storage;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class StorageCapacityModel implements Serializable {

    private static final long serialVersionUID = -489412460378438937L;

    /**
     * 主键id
     */
    private String id;

    /**
     * 容量长度
     */
    private Long capacitySize;

    /**
     * 剩余长度
     */
    private Long remainingLength;

    /**
     * 容量所属人id
     */
    private String capacityOwnerId;

    /**
     * 容量所属人
     */
    private String capacityOwnerName;

    /**
     * 操作人id
     */
    private String operatorId;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
