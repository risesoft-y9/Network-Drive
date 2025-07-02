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
@Table(name = "Y9_DATAASSETS_API_ONLINE_INFO")
@Comment("接口详细信息表")
@NoArgsConstructor
@Data
public class DataApiOnlineInfoEntity extends BaseEntity {

    private static final long serialVersionUID = -8854294421940798800L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Lob
    @Column(name = "FORMDATA")
    @Comment(value = "内容")
    private String formData;

}