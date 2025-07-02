package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_STORAGE_NETWORK_FILECOLLECT", indexes = {@Index(name = "FILE_FILEID_INDEX", columnList = "FILEID")})
@org.hibernate.annotations.Table(comment = "文件收藏记录表", appliesTo = "Y9_STORAGE_NETWORK_FILECOLLECT")
@NoArgsConstructor
@Data
public class FileNodeCollect implements Serializable {


    private static final long serialVersionUID = 3258301873386052590L;
    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "FILEID", length = 50)
    @Comment("文件Id")
    private String fileId;

    @Column(name = "FILENAME", length = 200)
    @Comment("文件名称")
    private String fileName;

    @Column(name = "PARENTID", length = 50)
    @Comment("文件父Id")
    private String parentId;

    @Column(name = "COLLECTUSERID", length = 50)
    @Comment("收藏人Id")
    private String collectUserId;

    @Column(name = "COLLECTROUTE", length = 1000)
    @Comment("收藏路径")
    private String collectRoute;

    @Column(name = "LISTNAME", length = 50)
    @Comment("列表名称")
    private String listName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COLLECTTIME")
    @Comment("收藏时间")
    private Date collectTime;

}
