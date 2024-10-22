package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author yihong
 * @date 2024/10/21
 */
@Entity
@Table(name = "Y9_ARCHIVES_METADATA_FIELD")
@org.hibernate.annotations.Table(comment = "元数据字段定义", appliesTo = "Y9_ARCHIVES_METADATA_FIELD")
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class MetadataField implements Serializable {

    private static final long serialVersionUID = -8257542480755687177L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "FIELD_NAME", length = 30)
    @Comment("字段英文名称")
    private String fieldName;

    @Column(name = "FIELD_CNNAME", length = 30)
    @Comment("字段中文名称")
    private String fieldCnName;

    @Column(name = "FIELD_DESCRIPTION", length = 500)
    @Comment("字段描述")
    private String fieldDescription;

    @Column(name = "FIELD_TYPE", length = 50)
    @Comment("字段类型")
    private String fieldType;

    @Column(name = "FIELD_SOURCE", length = 50)
    @Comment("字段来源")
    @ColumnDefault("系统内置")
    private String fieldSource;

    @Column(name = "FIELD_LENGTH")
    @Comment("字段长度")
    private Integer fieldLength = 0;

    @Column(name = "ISLISTSHOW")
    @Comment("是否列表显示")
    private Integer isListShow = 0;

    @Column(name = "ISORDER")
    @Comment("是否可排序")
    private Integer isOrder = 0;

    @Column(name = "ISRECORD")
    @Comment("是否著录")
    private Integer isRecord = 0;

    @Column(name = "ISRECORDNULL")
    @Comment("是否著录必填")
    private Integer isRecordNull = 0;

    @Column(name = "ISSEARCH")
    @Comment("是否为查询条件")
    private Integer isSearch = 0;

    @Column(name = "USER_ID", length = 50)
    @Comment("操作人id")
    private String userId;

    @Column(name = "USER_NAME", length = 50)
    @Comment("操作人")
    private String userName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATETIME")
    @Comment("更新时间")
    private Date updateTime;

}
