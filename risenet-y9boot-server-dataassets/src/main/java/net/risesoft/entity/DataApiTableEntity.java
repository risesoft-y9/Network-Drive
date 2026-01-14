package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_APITABLE")
@org.hibernate.annotations.Table(comment = "数据接口表", appliesTo = "Y9_DATAASSETS_APITABLE")
@NoArgsConstructor
@Data
public class DataApiTableEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "DATASOURCE_ID", length = 38, nullable = false)
    @Comment(value = "数据源ID")
    private String dataSourceId;

    @Column(name = "TABLE_NAME", length = 200, nullable = false)
    @Comment(value = "表名称")
    private String tableName;

    @Lob
    @Column(name = "RETURN_FIELDS")
    @Comment(value = "返回字段列表")
    private String returnFields;

    @Lob
    @Column(name = "QUERY_FIELDS")
    @Comment(value = "查询字段列表")
    private String queryFields;

    @Column(name = "QUERY_PARAMS", length = 500)
    @Comment(value = "固定查询条件")
    private String queryParams;

    @Column(name = "OWNER", length = 50)
    @Comment(value = "所属者")
    private String owner;

    @Column(name = "REMARK", length = 1000)
    @Comment(value = "备注")
    private String remark;

    @Column(name = "TENANTID", length = 50)
    @Comment(value = "租户ID")
    private String tenantId;

    @Column(name = "CREATOR", length = 50)
    @Comment(value = "创建人")
    private String creator;

    @Column(name = "CREATORID", length = 50)
    @Comment(value = "创建人ID")
    private String creatorId;

    @Column(name = "UPDATE_USER", length = 50)
    @Comment(value = "更新人")
    private String updateUser;

    @Column(name = "UPDATE_USERID", length = 50)
    @Comment(value = "更新人ID")
    private String updateUserId;

    @Column(name = "IS_DELETED")
    @Comment(value = "删除标记")
    @ColumnDefault("0")
    private Boolean isDeleted = false;

    @Column(name = "SUBSCRIBE_ID", length = 38)
    @Comment(value = "订阅ID")
    private String subscribeId;

    @Transient
    private String incrementField;
}
