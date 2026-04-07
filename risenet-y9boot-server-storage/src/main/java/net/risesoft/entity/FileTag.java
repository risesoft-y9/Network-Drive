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
import javax.persistence.Transient;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_TAG", indexes = {@Index(name = "FILE_CREATEID_INDEX", columnList = "CREATEID")})
@org.hibernate.annotations.Table(comment = "文件标签表", appliesTo = "Y9_STORAGE_TAG")
public class FileTag implements Serializable {

    private static final long serialVersionUID = -7940713192401728738L;

    @Id
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATETIME")
    @Comment("更新时间")
    private Date updateTime;

    @Column(name = "TAGNAME", length = 100)
    @Comment("标签名称")
    private String tagName;

    @Column(name = "TAGTYPE", length = 50)
    @Comment("标签类型")
    private String tagType;

    @Column(name = "TAGCOLOR", length = 50)
    @Comment("标签颜色")
    private String tagColor;

    @Column(name = "DESCRIPTION", length = 200)
    @Comment("标签描述")
    private String description;

    @Comment("创建人id")
    @Column(name = "CREATEID", length = 38)
    private String createId;

    @Comment("创建人姓名")
    @Column(name = "CREATENAME", length = 50)
    private String createName;

    @Column(name = "TABINDEX")
    @Comment("排序字段")
    private Integer tabIndex;

    @Transient
    private String listType;

}
