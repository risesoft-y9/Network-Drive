package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_SUBSCRIBE")
@org.hibernate.annotations.Table(comment = "数据订阅信息表", appliesTo = "Y9_DATAASSETS_SUBSCRIBE")
@NoArgsConstructor
@Data
public class SubscribeEntity extends BaseEntity {

	private static final long serialVersionUID = 9046045518968309264L;

	@Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "ASSETSID", nullable = false)
    @Comment(value = "资产ID")
    private Long assetsId;

    @Column(name = "PROVIDETYPE", length = 120, nullable = false)
    @Comment(value = "选择提供方式：文件下载/在线查看/接口请求/库表推送/应用地址")
    private String provideType;
    
    @Column(name = "TENANTID", length = 50, nullable = false)
    @Comment(value = "租户ID")
    private String tenantId;

    @Column(name = "USERID", length = 50, nullable = false)
    @Comment(value = "订阅人ID")
    private String userId;
    
    @Column(name = "USERNAME", length = 100, nullable = false)
    @Comment(value = "订阅人名称")
    private String userName;
    
    @Column(name = "REVIEWSTATUS", length = 100, nullable = false)
    @Comment(value = "审核状态：审核通过/待审核/审核不通过")
    private String reviewStatus;
    
    @Column(name = "REASON", length = 1000)
    @Comment(value = "不通过原因")
    private String reason;
    
    @Column(name = "PURPOSE", length = 2000, nullable = false)
    @Comment(value = "用途说明")
    private String purpose;
    
    @Column(name = "REVIEWID", length = 50)
    @Comment(value = "审核人ID")
    private String reviewId;
    
    @Column(name = "REVIEWNAME", length = 100)
    @Comment(value = "审核人名称")
    private String reviewName;
    
    @Column(name = "REVIEWDATE")
    @Comment(value = "审核时间")
    private String reviewDate;

}