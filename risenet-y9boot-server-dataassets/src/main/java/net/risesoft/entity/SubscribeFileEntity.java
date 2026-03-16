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
@Table(name = "Y9_DATAASSETS_SUBSCRIBEFILE")
@org.hibernate.annotations.Table(comment = "订阅-附件表", appliesTo = "Y9_DATAASSETS_SUBSCRIBEFILE")
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
