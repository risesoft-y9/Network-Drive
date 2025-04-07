package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
@Comment("文件分块记录表")
public class Chunk implements Serializable {

    private static final long serialVersionUID = -7517930847930358539L;
    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

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
