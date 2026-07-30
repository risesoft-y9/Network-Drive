package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话会话
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_AI_CHAT_SESSION")
@org.hibernate.annotations.Table(appliesTo = "Y9_STORAGE_AI_CHAT_SESSION", comment = "AI对话会话表")
public class ChatSession implements Serializable {

    private static final long serialVersionUID = -6839277593039443243L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键id")
    private String id;

    @Column(name = "PERSON_ID", length = 38, nullable = false)
    @Comment(value = "用户ID")
    private String personId;

    @Column(name = "TENANT_ID", length = 38)
    @Comment(value = "租户ID")
    private String tenantId;

    @Column(name = "TITLE", length = 500)
    @Comment(value = "会话标题")
    private String title;

    @Column(name = "CREATE_TIME")
    @Comment(value = "创建时间")
    private Date createTime;

    @Column(name = "UPDATE_TIME")
    @Comment(value = "更新时间")
    private Date updateTime;

}
