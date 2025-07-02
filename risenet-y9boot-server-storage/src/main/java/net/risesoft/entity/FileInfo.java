package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_NETWORK_FILE_INFO",
    indexes = {@Index(name = "FILE_NODE_ID_INDEX", columnList = "FILE_NODE_ID")})
@Comment("文件信息表")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 2887873103562809956L;

    @Id
    @Column(name = "ID", length = 50)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "FILE_NODE_ID", length = 50)
    @Comment("文件节点id")
    private String fileNodeId;

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
