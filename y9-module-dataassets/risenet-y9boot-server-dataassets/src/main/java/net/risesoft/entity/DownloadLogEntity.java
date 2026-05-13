package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_DOWNLOADLOG")
@org.hibernate.annotations.Table(comment = "资产下载日志表", appliesTo = "Y9_DATAASSETS_DOWNLOADLOG")
@NoArgsConstructor
@Data
public class DownloadLogEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "ASSETSID")
    @Comment(value = "资产ID")
    private Long assetsId;

    @Column(name = "FILEID", length = 50)
    @Comment(value = "文件ID")
    private Long fileId;

    @Column(name = "DOWNLOADER", length = 100)
    @Comment(value = "下载人")
    private String downloader;

    @Column(name = "DOWNLOADERID", length = 38)
    @Comment(value = "下载人ID")
    private String downloaderId;

    @Column(name = "RESULT", length = 500)
    @Comment(value = "下载结果")
    private String result;

}
