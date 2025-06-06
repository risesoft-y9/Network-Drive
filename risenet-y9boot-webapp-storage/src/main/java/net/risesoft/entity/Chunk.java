package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_NETWORK_FILE_CHUNKS",
    indexes = {@Index(name = "FILE_NODE_ID_INDEX", columnList = "FILE_NODE_ID")})
@org.hibernate.annotations.Table(comment = "文件分块记录表", appliesTo = "Y9_STORAGE_NETWORK_FILE_CHUNKS")
public class Chunk implements Serializable {

    private static final long serialVersionUID = -7517930847930358539L;
    @Id
    @Column(name = "ID", length = 50)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "FILE_NODE_ID", length = 50)
    @Comment("文件节点id")
    private String fileNodeId;

    @Column(name = "FILE_STORE_ID", length = 50)
    @Comment("文件存储id")
    private String fileStoreId;

    /**
     * 当前文件块，从1开始
     */
    @Column(nullable = false)
    @Comment("当前文件块")
    private Integer chunkNumber;
    /**
     * 分块大小
     */
    @Column(nullable = false)
    @Comment("分块大小")
    private Long chunkSize;
    /**
     * 当前分块大小
     */
    @Column(nullable = false)
    @Comment("当前分块大小")
    private Long currentChunkSize;
    /**
     * 总大小
     */
    @Column(nullable = false)
    @Comment("总大小")
    private Long totalSize;
    /**
     * 文件标识
     */
    @Column(nullable = false)
    @Comment("文件标识")
    private String identifier;
    /**
     * 文件名
     */
    @Column(nullable = false)
    @Comment("文件名")
    private String filename;
    /**
     * 相对路径
     */
    @Column(nullable = false)
    @Comment("相对路径")
    private String relativePath;
    /**
     * 总块数
     */
    @Column(nullable = false)
    @Comment("总块数")
    private Integer totalChunks;
    /**
     * 文件类型
     */
    @Column
    @Comment("文件类型")
    private String type;

    /**
     * 文件对象
     */
    @Transient
    private MultipartFile file;
}
