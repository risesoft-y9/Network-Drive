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
 * AI 对话消息
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_STORAGE_AI_CHAT_MESSAGE")
@org.hibernate.annotations.Table(appliesTo = "Y9_STORAGE_AI_CHAT_MESSAGE", comment = "AI对话消息表")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 7860427955693988770L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键id")
    private String id;

    @Column(name = "SESSION_ID", length = 38, nullable = false)
    @Comment(value = "会话ID")
    private String sessionId;

    @Column(name = "ROLE", length = 20, nullable = false)
    @Comment(value = "角色：user-用户 assistant-AI")
    private String role;

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    @Comment(value = "消息内容")
    private String content;

    @Column(name = "CREATE_TIME")
    @Comment(value = "创建时间")
    private Date createTime;

}
