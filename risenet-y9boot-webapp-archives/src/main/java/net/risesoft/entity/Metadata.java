package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_ARCHIVES_METADATA")
@org.hibernate.annotations.Table(comment = "元数据表", appliesTo = "Y9_ARCHIVES_METADATA")
public class Metadata implements Serializable {

    private static final long serialVersionUID = -8226921317846231630L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("数据id")
    private Long id;

    @Column(name = "NAME", length = 100)
    @Comment("电子文件名称")
    private String name;

    @Comment("MD5值")
    @Column(name = "MD5", length = 100)
    private String md5;

    @Column(name = "DATATYPE", length = 100)
    @Comment("元数据类型")
    private String dataType;

    @Column(name = "SYSTEMNAME", length = 100)
    @Comment("系统标识")
    private String systemName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATETIME")
    @Comment("创建时间")
    private Date createTime;

}
