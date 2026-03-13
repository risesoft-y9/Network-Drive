package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_TAG_RELATION", indexes = {@Index(name = "FILE_TAGID_INDEX", columnList = "TAGID")})
@org.hibernate.annotations.Table(comment = "文件标签关联表", appliesTo = "Y9_STORAGE_TAG_RELATION")
public class FileTagRelation implements Serializable {

    private static final long serialVersionUID = 7171121660427731154L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Comment("文件id")
    @Column(name = "FILEID", length = 50)
    private String fileId;

    @Comment("标签id")
    @Column(name = "TAGID", length = 50)
    private String tagId;

    @Comment("操作人id")
    @Column(name = "OPERATORID", length = 50)
    private String operatorId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OPERATETIME")
    @Comment("操作时间")
    private Date operateTime;

}
