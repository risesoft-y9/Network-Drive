package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "Y9_DATAASSETS_CATEGORY")
@Comment("门类管理表")
public class Category implements Serializable {

    private static final long serialVersionUID = 4808283868156401772L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("门类标识")
    @Column(name = "MARK", length = 50, nullable = false, unique = true)
    private String mark;

    @Comment("门类名称")
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @Comment("门类来源")
    @Column(name = "CATEGORY_SOURCE", length = 50)
    private String categorySource;

    @Comment("操作人ID")
    @Column(name = "USERID", length = 50)
    private String userId;

    @Comment("操作人名称")
    @Column(name = "USERNAME", length = 50)
    private String userName;

    @Comment("创建时间")
    @Column(name = "CREATEDATE")
    private String createDate;

    @Comment("修改时间")
    @Column(name = "MODIFYDATE")
    private String modifyDate;

    @Comment("删除标记")
    @Column(name = "DELETED")
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "TENANT_ID", length = 50)
    @Comment("租户ID")
    private String tenantId;
}
