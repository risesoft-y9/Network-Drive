package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "Y9_ARCHIVES_FILES")
@org.hibernate.annotations.Table(comment = "档案电子文件信息表", appliesTo = "Y9_ARCHIVES_FILES")
public class ArchivesFile implements Serializable {

    private static final long serialVersionUID = 2280498266929769681L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("档案关联id")
    @Column(name = "ARCHIVESID", length = 50)
    private Long archivesId;

    @Comment("文件仓库Id")
    @Column(name = "FILESTOREID", length = 50)
    private String fileStoreId;

    @Comment("文件名称")
    @Column(name = "FILENAME", length = 100)
    private String fileName;

    @Comment("文件大小")
    @Column(name = "FILESIZE")
    private Long fileSize;

    @Comment("文件类型")
    @Column(name = "FILETYPE", length = 20)
    private String fileType;

    @Comment("文件哈希值")
    @Column(name = "FILEHASH", length = 200)
    private String fileHash;

    @Comment("上传时间")
    @Column(name = "UPLOADTIME", length = 100)
    private String uploadTime;

    @Comment("上传人")
    @Column(name = "PERSONNAME", length = 100)
    private String personName;

    @Comment("上传人员Id")
    @Column(name = "PERSONID", length = 50)
    private String personId;

    @Comment("上传人员部门Id")
    @Column(name = "DEPTID", length = 50)
    private String deptId;

    @Comment("上传人员部门名称")
    @Column(name = "DEPTNAME", length = 100)
    private String deptName;

    @Comment("岗位id")
    @Column(name = "POSITIONID", length = 50)
    private String positionId;

    @Transient
    private String positionName;

    @Comment("文件描述")
    @Column(name = "DESCRIBES", length = 255)
    private String describes;

    @Comment("文件索引")
    @Column(name = "TABINDEX", length = 10)
    private Integer tabIndex;

    @Comment("软删除标记")
    @Column
    private Integer deleted = 0;

    @Comment("删除时间")
    @Column(name = "DELETETIME", length = 100)
    private String deleteTime;

    @Comment("删除操作的人员id")
    @Column(name = "DETELEUSERID", length = 38)
    private String deteleUserId = "";

    @Transient
    private Integer serialNumber;

}
