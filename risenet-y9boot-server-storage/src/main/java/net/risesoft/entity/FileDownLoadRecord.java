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
@Table(name = "Y9_STORAGE_DOWNLOAD_RECORD", indexes = {@Index(name = "FILE_FILEID_INDEX", columnList = "FILEID")})
@org.hibernate.annotations.Table(comment = "文档下载记录表", appliesTo = "Y9_STORAGE_DOWNLOAD_RECORD")
@NoArgsConstructor
@Data
public class FileDownLoadRecord implements Serializable {

    private static final long serialVersionUID = 5099096503555848002L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "FILEID", length = 50)
    @Comment("文件Id")
    private String fileId;

    @Column(name = "DOWNLOADMODE", length = 50)
    @Comment("下载模式")
    private String downLoadMode;

    @Column(name = "DOWNLOADUSERID", length = 50)
    @Comment("下载人Id")
    private String downLoadUserId;

    @Column(name = "DOWNLOADUSERNAME", length = 50)
    @Comment("下载人")
    private String downLoadUserName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DOWNLOADTIME")
    @Comment("下载时间")
    private Date downLoadTime;

}
