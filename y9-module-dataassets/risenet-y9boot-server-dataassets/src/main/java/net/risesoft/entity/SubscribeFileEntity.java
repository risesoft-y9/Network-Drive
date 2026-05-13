package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_SUBSCRIBEFILE")
@Comment("订阅-附件表")
@NoArgsConstructor
@Data
public class SubscribeFileEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "SUBSCRIBEID", length = 38)
    @Comment(value = "订阅ID")
    private String subscribeId;

    @Column(name = "FILEID", length = 38)
    @Comment(value = "文件ID")
    private String fileId;

    @Column(name = "FILENAME", length = 255)
    @Comment(value = "文件名称")
    private String fileName;

}
