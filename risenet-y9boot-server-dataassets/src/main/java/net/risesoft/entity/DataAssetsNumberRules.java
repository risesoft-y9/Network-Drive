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
@Table(name = "Y9_DATAASSETS_NUMBER_RULES")
@Comment("资产编号规则表")
public class DataAssetsNumberRules implements Serializable {

    private static final long serialVersionUID = -687877807119334107L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("门类标识")
    @Column(name = "CATEGORYMARK", length = 50, nullable = false)
    private String categoryMark;

    @Comment("结构顺序")
    @Column(name = "STRUCTUREORDER", length = 10, nullable = false)
    private Integer structureOrder;

    @Comment("元数据字段名称")
    @Column(name = "FIELDNAME", length = 100, nullable = false)
    private String fieldName;

    @Comment("元数据字段中文名称")
    @Column(name = "FIELDCNAME", length = 100)
    private String fieldCName;

    @Comment("连接符")
    @Column(name = "CONNECTORSYMBOL", length = 50)
    private String connectorSymbol;

    @Comment("操作人ID")
    @Column(name = "USERID", length = 50)
    private String userId;

    @Comment("创建时间")
    @Column(name = "CREATEDATE")
    private String createDate;

}
