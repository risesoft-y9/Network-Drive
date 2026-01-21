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
@Table(name = "Y9_DATAASSETS_DEMAND2")
@Comment("数据需求信息表")
@NoArgsConstructor
@Data
public class DataDemandEntity2 extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "TITLE", length = 100)
    @Comment(value = "标题")
    private String title;

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

    @Column(name = "CONTACT", length = 50)
    @Comment(value = "联系方式")
    private String contact;

    @Lob
    @Column(name = "FEEDBACK")
    @Comment(value = "反馈")
    private String feedback;

}