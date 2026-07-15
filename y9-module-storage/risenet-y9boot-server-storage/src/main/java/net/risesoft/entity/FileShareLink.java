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
import javax.persistence.Transient;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_STORAGE_SHARE_LINK", indexes = {@Index(name = "FILE_SHARE_LINK_FILEID_INDEX", columnList = "FILEID")})
@org.hibernate.annotations.Table(comment = "文档分享链接表", appliesTo = "Y9_STORAGE_SHARE_LINK")
@NoArgsConstructor
@Data
public class FileShareLink implements Serializable {

    private static final long serialVersionUID = 3853746180869241759L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "FILEID", length = 50)
    @Comment("文件Id")
    private String fileId;

    @Column(name = "LINKEXPIRETIME", length = 50)
    @Comment("链接期限")
    private String linkExpireTime;

    @Column(name = "LINKKEY", length = 100)
    @Comment("链接密钥")
    private String linkKey;

    @Column(name = "LINKPASSWORD", length = 50)
    @Comment("链接密码")
    private String linkPassword;

    @Column(name = "CREATEUSERID", length = 50)
    @Comment("创建人id")
    private String createUserId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "BROWSECOUNT", length = 50)
    @Comment("浏览次数")
    private Integer browseCount;

    @Column(name = "VALIDPERIOD", length = 50)
    @Comment("有效期设置")
    private String validPeriod;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRETIME")
    @Comment("过期时间")
    private Date expireTime;

    /**
     * 链接地址: 前端访问链接
     */
    @Transient
    private String linkUrl;

    /**
     * 文件名称
     */
    @Transient
    private FileNode fileNode;

}
