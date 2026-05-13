package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_DATAASSETS_RECENT")
@Comment("记录数据表")
@NoArgsConstructor
@Data
public class DataRecentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "OPERATIONTYPE")
    @Comment(value = "操作类型")
    private String operationType;

    @Column(name = "CONTENT", length = 2000)
    @Comment(value = "内容")
    private String content;

    @Column(name = "OPERATOR")
    @Comment(value = "操作人")
    private String operator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATEDATE")
    @Comment(value = "创建时间")
    private Date createDate;

}
