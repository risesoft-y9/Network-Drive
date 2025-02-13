package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yihong
 * @date 2024/10/15
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_DATAASSETS_DETAILS")
@Comment("数据资产详情表")
public class DataAssets implements Serializable {

    private static final long serialVersionUID = 4774402268063407277L;

    @Id
    @Column(name = "DATAASSETS_ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long dataassetsId;

    @Column(name = "CATEGORY_ID", length = 50)
    @Comment("门类ID")
    private String categoryId;

    @Column(name = "CATEGORY_CODE", length = 50)
    @Comment("门类标识")
    private String categoryCode;

    @Column(name = "TENANT_ID", length = 50)
    @Comment("租户ID")
    private String tenantId;

    @Column(name = "NAME", length = 200)
    @Comment("数据资产名称")
    private String name;

    @Column(name = "CODE", length = 200)
    @Comment("数据资产编码")
    private String code;

    @Column(name = "ASSETS_NO", length = 50)
    @Comment("资产编号")
    private String assetsNo;

    @Lob
    @Column(name = "QRCODE")
    @Comment("全球统一码二维码")
    private String qrcode;

    @Column(name = "CODE_GLOBAL", length = 300)
    @Comment("数据资产编码")
    private String codeGlobal;

    @Column(name = "TAG", length = 31)
    @Comment("标签")
    private String tag;

    @Lob
    @Column(name = "ASSETS_EXCERPT")
    @Comment("数据资产摘要")
    private String assetsExcerpt;

    @Column(name = "SHARE_TYPE", length = 40)
    @Comment("共享类型")
    private String shareType;

    @Column(name = "DATA_TYPE_CATEGORY", length = 40)
    @Comment("数据资产格式分类")
    private String dataTypeCategory;

    @Column(name = "DATA_TYPE", length = 40)
    @Comment("数据资产格式")
    private String dataType;

    @Column(name = "DATA_ORIGIN_SYSTEM", length = 255)
    @Comment("数据来源系统")
    private String dataOriginSystem;

    @Column(name = "DATA_OWNER", length = 255)
    @Comment("数据资产提供方")
    private String dataOwner;

    @Column(name = "DELETE_USER", length = 50)
    @Comment("删除人")
    private String deleteUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Comment("删除时间")
    @Column(name = "DELETE_TIME", updatable = false)
    private Date deleteTime;

    @Column(name = "IS_DELETED")
    @Comment("删除标记")
    private Boolean isDeleted = false;

    @Column(name = "ASSETS_STATUS", length = 50)
    @Comment("资产状态")
    private Integer assetsStatus = 0;// 1-上架 0-下架

    @Column(name = "ASSETS_SCORE", length = 50)
    @Comment("资产评分")
    private String assetsScore;

    @Column(name = "ORDER_NUM")
    @Comment("排序序号")
    private Integer orderNum;

    @Column(name = "CREATOR", length = 50)
    @Comment("创建人")
    private String creator;

    @Column(name = "UPDATE_USER", length = 50)
    @Comment("更新人")
    private String updateUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Comment("创建时间")
    @Column(name = "CREATE_TIME", updatable = false)
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Comment("更新时间")
    @Column(name = "UPDATE_TIME", updatable = false)
    private Date updateTime;

    @Column(name = "THUMBNAIL", length = 300)
    @Comment("缩略图")
    private String thumbnail;

    @Column(name = "LINK_MASTER_DATA_ID", length = 1200)
    @Comment("主数据ID")
    private String linkMasterDataId;

    @Column(name = "LINK_MASTER_DATA", length = 1200)
    @Comment("主数据")
    private String linkMasterData;

    @Column(name = "AS_DATA_OWNER", length = 50)
    @Comment("数据所有者")
    private String asDataOwner;

    @Column(name = "AS_DATA_MANAGER", length = 50)
    @Comment("数据管理员")
    private String asDataManager;

    @Column(name = "AS_DATA_PATH", length = 100)
    @Comment("存储位置")
    private String asDataPath;

    @Column(name = "AS_DATA_SIZE", length = 100)
    @Comment("数据量")
    private String asDataSize;

    @Column(name = "AS_DATA_FREQUENCY", length = 100)
    @Comment("更新频率")
    private String asDataFrequency;

    @Column(name = "AS_DATA_SENSITIVITY", length = 100)
    @Comment("数据敏感度")
    private String asDataSensitivity;

    @Column(name = "AS_DATA_RETENTION", length = 100)
    @Comment("保留期限")
    private String asDataRetention;

    @Column(name = "AS_DATA_PURPOSE", length = 100)
    @Comment("主要用途")
    private String asDataPurpose;

    @Column(name = "AS_DATA_QUALITY_SCORE", length = 100)
    @Comment("数据质量评分")
    private String asDataQualityScore;

    @Column(name = "AS_DATA_FIELD", length = 100)
    @Comment("关键字段")
    private String asDataField;

    @Column(name = "AS_DATA_COMPLIANCE", length = 100)
    @Comment("合规要求")
    private String asDataCompliance;

    @Column(name = "AS_DATA_SECURITY_LEVEL", length = 100)
    @Comment("数据安全等级")
    private String asDataSecurityLevel;

    @Column(name = "AS_DATA_VALUE_ASSESSMENT", length = 100)
    @Comment("数据价值评估")
    private String asDataValueAssessment;

    @Column(name = "AS_DATA_PRODUCT_URL", length = 100)
    @Comment("数据产品URL")
    private String asDataProductUrl;

    @Column(name = "AS_DATA_SERVICE_URL", length = 100)
    @Comment("数据服务URL")
    private String asDataServiceUrl;

    @Column(name = "LINK_FILE", length = 300)
    @Comment("文件")
    private String linkFile;

    @Column(name = "LINK_API_ID", length = 300)
    @Comment("API ID")
    private String linkApiId;

    @Column(name = "LINK_API_NAME", length = 300)
    @Comment("API名称")
    private String linkApiName;

}
