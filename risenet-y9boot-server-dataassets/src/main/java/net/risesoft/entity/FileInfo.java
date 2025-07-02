package net.risesoft.entity;

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
import net.risesoft.base.BaseEntity;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_DATAASSETS_FILE_INFO",
    indexes = {@Index(name = "DATAASSETSFILEID_INDEX", columnList = "ASSETSID")})
@org.hibernate.annotations.Table(comment = "资产挂接信息表", appliesTo = "Y9_DATAASSETS_FILE_INFO")
public class FileInfo extends BaseEntity {

    private static final long serialVersionUID = 2887873103562809956L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "ASSETSID")
    @Comment("关联资产id")
    private Long assetsId;

    @Column(name = "NAME")
    @Comment("名称")
    private String name;

    @Column(name = "IDENTIFIER")
    @Comment("标识")
    private String identifier;

    @Column(name = "FILESIZE")
    @Comment("大小")
    private String fileSize;

    @Column(name = "FILETYPE")
    @Comment("类型")
    private String fileType;

    @Column(name = "FILEPATH")
    @Comment("位置")
    private String filePath;

}
