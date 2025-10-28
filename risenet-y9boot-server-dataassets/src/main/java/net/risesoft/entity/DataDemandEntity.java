package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_DEMAND")
@Comment( "数据需求信息表")
@NoArgsConstructor
@Data
public class DataDemandEntity extends BaseEntity {

    private static final long serialVersionUID = -6901909013953033589L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "TITLE", length = 100)
    @Comment(value = "标题")
    private String title;

    @Column(name = "SCENARIO", length = 500)
    @Comment(value = "场景")
    private String scenario;

    @Lob
    @Column(name = "DESCRIPTION")
    @Comment(value = "描述")
    private String description;

    @Column(name = "PUBLISHERID", length = 50)
    @Comment(value = "发布人ID")
    private String publisherId;

    @Column(name = "PUBLISHER", length = 10)
    @Comment(value = "发布人")
    private String publisher;

    @Column(name = "COMPANY", length = 50)
    @Comment(value = "发布人公司")
    private String company;

    @Column(name = "CONTACT", length = 50)
    @Comment(value = "联系方式")
    private String contact;

    @Column(name = "DATATYPE")
    @Comment(value = "需求类型：1-数据服务，2-数据采购")
    @ColumnDefault("1")
    private Integer dataType;

    @Column(name = "INDUSTRY")
    @Comment(value = "行业领域")
    private Integer industry;

    @Column(name = "BUDGET")
    @Comment(value = "需求预算")
    private Integer budget;

    @Column(name = "STATUS")
    @Comment(value = "需求状态：1-征集中，2-已结束")
    @ColumnDefault("1")
    private Integer status;

    @Column(name = "CLICKNUM")
    @Comment(value = "点击次数")
    @ColumnDefault("0")
    private Integer clickNum;

    @Column(name = "EXAMINE")
    @Comment(value = "审核状态：0-审核中，1-审核通过，2-审核不通过")
    @ColumnDefault("0")
    private Integer examine;

}