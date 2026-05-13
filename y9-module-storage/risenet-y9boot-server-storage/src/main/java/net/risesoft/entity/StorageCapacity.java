package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_CAPACITY", indexes = {@Index(name = "CAPACITYOWNERID_INDEX", columnList = "CAPACITYOWNERID")})
@Comment("存储容量配置表")
public class StorageCapacity implements Serializable {

    private static final long serialVersionUID = -8226921317846231630L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "CAPACITYSIZE")
    @Comment("容量长度")
    private Long capacitySize;

    @Column(name = "REMAININGLENGTH")
    @Comment("剩余长度")
    private Long remainingLength;

    @Comment("容量所属人id")
    @Column(name = "CAPACITYOWNERID", length = 50)
    private String capacityOwnerId;

    @Comment("容量所属人")
    @Column(name = "CAPACITYOWNERNAME", length = 50)
    private String capacityOwnerName;

    @Comment("操作人id")
    @Column(name = "OPERATORID", length = 50)
    private String operatorId;

    @Comment("操作人")
    @Column(name = "OPERATORNAME", length = 50)
    private String operatorName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATETIME")
    @Comment("更新时间")
    private Date updateTime;

}
