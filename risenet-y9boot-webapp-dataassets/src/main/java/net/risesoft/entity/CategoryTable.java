package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_DATAASSETS_TABLE")
@org.hibernate.annotations.Table(comment = "门类表定义", appliesTo = "Y9_DATAASSETS_TABLE")
@NoArgsConstructor
@Data
public class CategoryTable implements Serializable {

    private static final long serialVersionUID = 6044556428114089122L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "TABLENAME", length = 30)
    @Comment("表英文名称")
    private String tableName;

    @Column(name = "OLDTABLENAME", length = 30)
    @Comment("老表英文名称")
    private String oldTableName;

    @Column(name = "TABLECNNAME", length = 30)
    @Comment("表中文名称")
    private String tableCnName;

    @Column(name = "TABLEALIAS", length = 4)
    @Comment("表别名")
    private String tableAlias;

    @Column(name = "TABLEMEMO", length = 50)
    @Comment("表备注")
    private String tableMemo;

    @Column(name = "CATEGORY_MARK", length = 50)
    @Comment("门类标识")
    private String categoryMark;

    @Column(name = "CATEGORY_NAME", length = 50)
    @Comment("门类名称")
    private String categoryName;

    @Column(name = "CREATETIME", length = 50)
    @Comment("创建时间")
    private String createTime;

}
