package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_LABELDATA")
@Comment("标签数据表")
@NoArgsConstructor
@Data
public class LabelDataEntity extends BaseEntity {

    private static final long serialVersionUID = 1458969100933524314L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "NAME", length = 200, nullable = false)
    @Comment(value = "名称")
    private String name;

    @Column(name = "CODE", length = 100, nullable = false)
    @Comment(value = "代码")
    private String code;

    @Column(name = "PARENTID", length = 38)
    @Comment(value = "父ID")
    private String parentId;

    @Column(name = "TABINDEX")
    @Comment(value = "排序")
    @ColumnDefault("1")
    private Integer tabIndex;

}