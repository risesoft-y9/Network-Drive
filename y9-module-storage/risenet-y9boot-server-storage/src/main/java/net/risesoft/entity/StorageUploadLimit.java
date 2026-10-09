package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传大小限制配置（全局一条记录，uploadSize 单位：字节）
 *
 * @author yihong
 *
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_UPLOAD_LIMIT")
@org.hibernate.annotations.Table(comment = "上传大小限制配置表", appliesTo = "Y9_STORAGE_UPLOAD_LIMIT")
public class StorageUploadLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Comment("上传大小限制(单位:字节)")
    @Column(name = "UPLOADSIZE")
    private Long uploadSize;

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
