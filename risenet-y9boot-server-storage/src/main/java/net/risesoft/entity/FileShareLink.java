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

@Entity
@Table(name = "Y9_STORAGE_SHARE_LINK", indexes = {@Index(name = "FILE_SHARE_LINK_FILEID_INDEX", columnList = "FILEID")})
@Comment("文档分享链接表")
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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

}
