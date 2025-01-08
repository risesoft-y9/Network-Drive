package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "Y9_DATAASSETS_TABLE_FIELD")
@Comment("门类表字段定义")
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class CategoryTableField implements Serializable {

    private static final long serialVersionUID = 8204363825131431319L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "FIELDNAME", length = 30)
    @Comment("字段英文名称")
    private String fieldName;

    @Column(name = "OLDFIELDNAME", length = 30)
    @Comment("旧字段英文名称")
    private String oldFieldName;

    @Column(name = "FIELDCNNAME", length = 30)
    @Comment("字段中文名称")
    private String fieldCnName;

    @Column(name = "TABLEID", length = 50)
    @Comment("表Id")
    private String tableId;

    @Column(name = "TABLENAME", length = 50)
    @Comment("表名称")
    private String tableName;

    @Column(name = "FIELDTYPE", length = 50)
    @Comment("字段类型")
    private String fieldType;

    @Column(name = "FIELDLENGTH")
    @Comment("字段长度")
    private Integer fieldLength = 0;

    /**
     * // 1为是，0为否
     */
    @Column(name = "ISMAYNULL")
    @Comment("是否允许为空")
    private Integer isMayNull = 1;

    @Column(name = "ISSYSTEMFIELD")
    @Comment("是否系统字段")
    private Integer isSystemField = 0;

    /**
     * // -1未生成表字段，1为已经生成表字段
     */
    @Column(name = "STATE")
    @Comment("字段状态")
    private Integer state = -1;

    @Column(name = "DISPLAYORDER")
    @Comment("显示排序")
    private Integer displayOrder;

}
