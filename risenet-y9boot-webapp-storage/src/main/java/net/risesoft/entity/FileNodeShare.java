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
@Table(name = "Y9_STORAGE_NETWORK_FILE_SHARE",
    indexes = {@Index(name = "TO_ORGUNIT_ID_INDEX", columnList = "TO_ORGUNIT_ID"),
        @Index(name = "PERSON_ID_INDEX", columnList = "PERSON_ID")})
@Comment("文件夹分享表")
public class FileNodeShare implements Serializable {

    private static final long serialVersionUID = 2361460179703673267L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME")
    @Comment("创建时间")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATE_TIME")
    @Comment("更新时间")
    private Date updateTime;

    @Comment("文件节点id")
    @Column(name = "FILE_NODE_ID", length = 38)
    private String fileNodeId;

    @Comment("文件操作类型")
    @Column(name = "FILE_OPT_TYPE", length = 38)
    private String fileOptType;// 共享、分享、公开

    @Comment("分享人Id")
    @Column(name = "PERSON_ID")
    private String personId;

    @Comment("分享人姓名")
    @Column(name = "PERSON_NAME")
    private String personName;

    @Comment("被分享组织Id")
    @Column(name = "TO_ORGUNIT_ID")
    private String toOrgUnitId;

    @Comment("被分享组织名")
    @Column(name = "TO_ORGUNIT_NAME")
    private String toOrgUnitName;

}
