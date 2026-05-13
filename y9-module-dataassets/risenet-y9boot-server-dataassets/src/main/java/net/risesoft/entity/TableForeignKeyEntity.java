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
@Table(name = "Y9_DATAASSETS_TABLEFOREIGNKEY")
@Comment("表外键信息表")
@NoArgsConstructor
@Data
public class TableForeignKeyEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "TABLENAME", length = 120)
    @Comment(value = "表名")
    private String tableName;

    @Column(name = "FOREIGNKEYCOLUMN", length = 300)
    @Comment(value = "外键字段")
    private String foreignKeyColumn;

    @Column(name = "DATASOURCEID", length = 38)
    @Comment(value = "数据源id")
    private String dataSourceId;

    @Column(name = "INCREMENTFIELD", length = 100)
    @Comment(value = "增量字段")
    private String incrementField;

}