package net.risesoft.model.storage;

import java.io.Serializable;

import lombok.Data;

/**
 * 存储容量使用比例Model（用于饼状图显示）
 *
 * @author yihong
 *
 */
@Data
public class StorageUsageRatioModel implements Serializable {

    private static final long serialVersionUID = 7632189476124987632L;

    /**
     * 总容量(字节)
     */
    private Long capacitySize;

    /**
     * 已使用容量(字节)
     */
    private Long usedLength;

    /**
     * 剩余容量(字节)
     */
    private Long remainingLength;

    /**
     * 总容量(格式化字符串)
     */
    private String capacitySizeStr;

    /**
     * 已使用容量(格式化字符串)
     */
    private String usedLengthStr;

    /**
     * 剩余容量(格式化字符串)
     */
    private String remainingLengthStr;

    /**
     * 已使用百分比
     */
    private Double usedPercentage;

    /**
     * 剩余百分比
     */
    private Double remainingPercentage;

}
