package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

/**
 * @author yihong
 * @date 2024/10/15
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_DATAASSETS_DETAILS")
@Comment("数据资产详情表")
public class DataAssets extends BaseEntity {

    private static final long serialVersionUID = 4774402268063407277L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "CATEGORYID", length = 50)
    @Comment(value = "目录ID")
    private String categoryId;

    @Column(name = "TENANTID", length = 50)
    @Comment(value = "租户ID")
    private String tenantId;

    @Column(name = "NAME", length = 300)
    @Comment(value = "数据资产名称")
    private String name;

    @Column(name = "CODE", length = 200)
    @Comment(value = "数据资产编码")
    private String code;

    @Column(name = "PICTURE")
    @Comment(value = "资产图片")
    private String picture;

    @Lob
    @Column(name = "QRCODE")
    @Comment(value = "全球统一码二维码")
    private String qrcode;

    @Column(name = "CODE_GLOBAL", length = 300)
    @Comment(value = "全球统一码")
    private String codeGlobal;

    @Lob
    @Column(name = "REMARK")
    @Comment(value = "数据资产摘要")
    private String remark;

    @Column(name = "SHARE_TYPE")
    @Comment(value = "共享类型")
    private Integer shareType;

    @Column(name = "DATA_TYPE", length = 40)
    @Comment(value = "数据资产格式")
    private String dataType;

    @Column(name = "DATA_ORIGIN_SYSTEM", length = 255)
    @Comment(value = "数据来源系统")
    private String dataOriginSystem;

    @Column(name = "DATA_PROVIDER", length = 255)
    @Comment(value = "数据资产提供方")
    private String dataProvider;

    @Column(name = "STATUS")
    @Comment(value = "资产状态: 1-上架 0-下架")
    @ColumnDefault("0")
    private Integer status = 0;

    @Column(name = "ORDER_NUM")
    @Comment(value = "排序序号")
    @ColumnDefault("1")
    private Integer orderNum;

    @Column(name = "DATA_OWNER", length = 50)
    @Comment(value = "数据所有者")
    private String dataOwner;

    @Column(name = "DATA_MANAGER", length = 50)
    @Comment(value = "数据管理员")
    private String dataManager;

    @Column(name = "DATA_PATH", length = 200)
    @Comment(value = "存储位置")
    private String dataPath;

    @Column(name = "DATA_SIZE", length = 100)
    @Comment(value = "数据量")
    private String dataSize;

    @Column(name = "DATA_FREQUENCY", length = 100)
    @Comment(value = "更新频率")
    private String dataFrequency;

    @Column(name = "DATA_SENSITIVITY", length = 100)
    @Comment(value = "数据敏感度")
    private String dataSensitivity;

    @Column(name = "DATA_RETAIN", length = 100)
    @Comment(value = "保留期限")
    private String dataRetain;

    @Column(name = "DATA_PURPOSE", length = 200)
    @Comment(value = "主要用途")
    private String dataPurpose;

    @Column(name = "DATA_QUALITY_SCORE", length = 100)
    @Comment(value = "数据质量评分")
    private String dataQualityScore;

    @Column(name = "KEYFIELD", length = 100)
    @Comment(value = "关键字段")
    private String keyField;

    @Column(name = "DATA_COMPLIANCE", length = 100)
    @Comment(value = "合规要求")
    private String dataCompliance;

    @Column(name = "DATA_SECURITY_LEVEL", length = 100)
    @Comment(value = "数据安全等级")
    private String dataSecurityLevel;

    @Column(name = "DATA_VALUATION", length = 100)
    @Comment(value = "数据价值评估")
    private String dataValuation;

    @Column(name = "DATA_PRODUCT_URL", length = 100)
    @Comment(value = "数据产品URL")
    private String dataProductUrl;

    @Column(name = "DATA_SERVICE_URL", length = 100)
    @Comment(value = "数据服务URL")
    private String dataServiceUrl;

    @Column(name = "APPSCENARIOS", length = 20)
    @Comment(value = "应用场景")
    private String appScenarios;

    @Column(name = "DATAZONE", length = 20)
    @Comment(value = "数据专区")
    private String dataZone;

    @Column(name = "PRODUCTTYPE", length = 20)
    @Comment(value = "产品类型")
    private String productType;

    @Column(name = "MOUNTTYPE", length = 20)
    @Comment(value = "挂接类型：文件/数据/接口")
    private String mountType;

    @Column(name = "DATASTATE")
    @Comment(value = "入库状态: in-入库， out-出库")
    private String dataState;

    @Column(name = "CREATOR", length = 50)
    @Comment(value = "创建人")
    private String creator;

    @Column(name = "CREATORID", length = 50)
    @Comment(value = "创建人ID")
    private String creatorId;

    @Column(name = "UPDATE_USER", length = 50)
    @Comment(value = "更新人")
    private String updateUser;

    @Column(name = "UPDATE_USERID", length = 50)
    @Comment(value = "更新人ID")
    private String updateUserId;

    @Column(name = "DELETE_USER", length = 50)
    @Comment(value = "删除人")
    private String deleteUser;

    @Column(name = "DELETE_USERID", length = 50)
    @Comment(value = "删除人ID")
    private String deleteUserId;

    @Column(name = "IS_DELETED")
    @Comment(value = "删除标记")
    @ColumnDefault("0")
    private Boolean isDeleted = false;

    @Column(name = "CLICKNUM")
    @Comment(value = "点击次数")
    @ColumnDefault("0")
    private Integer clickNum;

    @Transient
    private String labelData;// 标注信息

}