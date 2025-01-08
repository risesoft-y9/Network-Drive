package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "Y9_DATAASSETS_FILES")
@Comment("电子文件信息表")
public class DataAssetsFile implements Serializable {

    private static final long serialVersionUID = 2280498266929769681L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Column(name = "DETAIL_ID", nullable = false)
    @Comment("详情关联id")
    private Long detailId;

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
