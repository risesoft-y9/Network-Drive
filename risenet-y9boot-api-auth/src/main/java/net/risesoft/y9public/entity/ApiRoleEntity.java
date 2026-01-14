package net.risesoft.y9public.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_API_ROLE")
@org.hibernate.annotations.Table(comment = "API接口权限表", appliesTo = "Y9_API_ROLE")
@NoArgsConstructor
@Data
public class ApiRoleEntity extends BaseEntity {

    private static final long serialVersionUID = -2019506383587773292L;

    @Id
    @Column(name = "APPNAME", length = 50, nullable = false)
    @Comment(value = "调用者名称")
    private String appName;

    @Column(name = "APPKEY", length = 100, nullable = false)
    @Comment(value = "密钥")
    private String appKey;

    @Column(name = "APIIDS", length = 1500)
    @Comment(value = "调用api")
    private String apiIds;

    @Column(name = "IPADDR", length = 800)
    @Comment(value = "白名单IP")
    private String ipAddr;

    @Column(name = "REMARK", length = 800)
    @Comment(value = "备注")
    private String remark;

    @Column(name = "PERMITS_PER_SECOND", nullable = false)
    @Comment(value = "每秒请求数")
    private double permitsPerSecond;

    @Column(name = "STATUS", nullable = false)
    @Comment(value = "状态：1-禁用，0-未禁用")
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(name = "EXPIRE_DATE")
    @Comment(value = "过期日期")
    private Date expireDate;

}