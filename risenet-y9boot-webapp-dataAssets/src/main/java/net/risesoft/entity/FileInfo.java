package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_DATAASSETS_FILE_INFO",
    indexes = {@Index(name = "DATAASSETSFILEID_INDEX", columnList = "ARCHIVEFILEID")})
@org.hibernate.annotations.Table(comment = "文件信息表", appliesTo = "Y9_DATAASSETS_FILE_INFO")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 2887873103562809956L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "ARCHIVEFILEID")
    @Comment("档案文件id")
    private Long archiveFileId;

    @Column(nullable = false)
    @Comment("文件名称")
    private String filename;

    @Column(nullable = false)
    @Comment("文件标识")
    private String identifier;

    @Column(nullable = false)
    @Comment("总大小")
    private Long totalSize;

    @Column(nullable = false)
    @Comment("类型")
    private String type;

    @Column(nullable = false)
    @Comment("文件位置")
    private String location;

}
