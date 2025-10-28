package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_DEMAND_ASK")
@Comment("数据需求留言表")
@NoArgsConstructor
@Data
public class DataDemandAskEntity extends BaseEntity {

    private static final long serialVersionUID = 3064893331970567101L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "DEMANDID", length = 38, nullable = false)
    @Comment(value = "需求表ID")
    private String demandId;

    @Lob
    @Column(name = "TEXT", nullable = false)
    @Comment(value = "内容")
    private String text;

    @Column(name = "PUBLISHERID", length = 50)
    @Comment(value = "发布人ID")
    private String publisherId;

    @Column(name = "PUBLISHER", length = 10)
    @Comment(value = "发布人")
    private String publisher;

    @Column(name = "COMPANY", length = 50)
    @Comment(value = "发布人公司")
    private String company;

}