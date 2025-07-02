package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.support.FileNodeType;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.configuration.common.Y9CommonProperties;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_NETWORK_FILE", indexes = {@Index(name = "FILE_USERID_INDEX", columnList = "USERID")})
@Comment("文件节点表")
public class FileNode implements Serializable {

    private static final long serialVersionUID = -8226921317846231630L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

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

    @Column(name = "NAME", length = 100)
    @Comment("文件夹名称")
    private String name;

    @Column(name = "LISTTYPE")
    @Comment("列表类别")
    private String listType;// my：个人文件 public：公共文件 dept：所在部门文件

    @Column(name = "FILETYPE")
    @Comment("文件类别")
    private Integer fileType;// 0：文件夹 1：图片 2：文档 3：视频 4：录音 5:压缩包

    @Column(name = "FILESUFFIX", length = 10)
    @Comment("文件类别")
    private String fileSuffix;

    @Comment("文件地址Id")
    @Column(name = "FILESTOREID", length = 50)
    private String fileStoreId;

    @Column(name = "FILESIZE")
    @Comment("文件长度")
    private Long fileSize;

    @Lob
    @Comment("组织id")
    @Column(name = "ORGID")
    private String orgId;

    @Comment("创建人id")
    @Column(name = "USERID", length = 38)
    private String userId;

    @Comment("创建人姓名")
    @Column(name = "USERNAME", length = 50)
    private String userName;

    @Comment("父文件夹id")
    @Column(name = "PARENTID", length = 50)
    private String parentId;

    @Comment("软删除标记")
    @Column(name = "DELFLAG")
    private boolean deleted = false;

    @Comment("是否收藏")
    @Column(name = "COLLECT")
    private boolean collect = false;

    @Comment("是否加密")
    @Column(name = "ENCRYPTION")
    private boolean encryption = false;

    @Comment("链接密码")
    @Column(name = "LINKPASSWORD", length = 50)
    private String linkPassword;

    @Comment("文件密码")
    @Column(name = "FILEPASSWORD", length = 50)
    private String filePassword;

    @Column(name = "TABINDEX")
    @Comment("排序字段")
    private Integer tabIndex;

    public FileNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getFileUrl() {
        if (StringUtils.isNotBlank(fileStoreId) && fileType != FileNodeType.FOLDER.getValue()) {
            Y9CommonProperties commonProperties = Y9Context.getBean(Y9Properties.class).getCommon();
            String fileUrl = commonProperties.getStorageBaseUrl() + "/s/" + fileStoreId + "." + fileSuffix;
            return fileUrl;
        } else {
            return null;
        }
    }

    public String getPreviewUrl() {
        if (StringUtils.isNotBlank(fileStoreId)) {
            Y9CommonProperties commonProperties = Y9Context.getBean(Y9Properties.class).getCommon();
            String jodconverterBaseUrl = commonProperties.getJodconverterBaseUrl();
            String fileUrl = commonProperties.getStorageBaseUrl() + "/s/" + fileStoreId + "." + fileSuffix;
            return jodconverterBaseUrl + fileUrl;
        } else {
            return null;
        }
    }
}
