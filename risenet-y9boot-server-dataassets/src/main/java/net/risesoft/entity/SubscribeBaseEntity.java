package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;
import net.risesoft.converter.EncryptConverter;

@Entity
@Table(name = "Y9_DATAASSETS_SUBSCRIBEBASE")
@org.hibernate.annotations.Table(comment = "订阅-库表推送信息表", appliesTo = "Y9_DATAASSETS_SUBSCRIBEBASE")
@NoArgsConstructor
@Data
public class SubscribeBaseEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "SUBSCRIBEID", length = 38)
    @Comment(value = "订阅ID")
    private String subscribeId;

    @Column(name = "URL", length = 500)
    @Comment(value = "库表推送URL")
    private String url;

    @Column(name = "USERNAME", length = 50)
    @Comment(value = "用户名")
    private String username;

    @Column(name = "PASSWORD", length = 300)
    @Comment(value = "密码")
    @Convert(converter = EncryptConverter.class)
    private String password;

    @Column(name = "REMARK", length = 1500)
    @Comment(value = "备注")
    private String remark;

}