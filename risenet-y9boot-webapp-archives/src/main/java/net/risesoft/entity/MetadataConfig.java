package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author yihong
 * @date 2024/10/21
 */
@NoArgsConstructor
@Accessors(chain = true)
@Data
@Entity
@Table(name = "Y9_ARCHIVES_METADATACONFIG")
@org.hibernate.annotations.Table(comment = "元数据配置表", appliesTo = "Y9_ARCHIVES_METADATACONFIG")
public class MetadataConfig implements Serializable {

    private static final long serialVersionUID = 6023418927806462716L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("视图类型")
    @Column(name = "VIEWTYPE", length = 20, nullable = false)
    private String viewType;

    @Comment("属性名称")
    @Column(name = "COLUMNNAME", length = 50, nullable = false)
    private String columnName;

    @Comment("数据类型")
    @Column(name = "DATATYPE", length = 50, nullable = false)
    private String dataType;

    @Comment("字段长度")
    @Column(name = "FIELDLENGTH")
    private Integer fieldLength;

    @Comment("显示名称")
    @Column(name = "DISPLAYNAME", length = 50, nullable = false)
    private String disPlayName;

    @Column(name = "DESCRIPTION", length = 500)
    @Comment("描述")
    private String description;

    @Comment("字段来源")
    @Column(name = "FIELDORIGIN", length = 50)
    private String fieldOrigin;

    @Comment("列显示宽度")
    @Column(name = "DISPLAYWIDTH", length = 50, nullable = false)
    private String disPlayWidth;

    @Comment("列对齐方式")
    @Column(name = "DISPLAYALIGN", length = 10, nullable = false)
    private String disPlayAlign;

    @Comment("是否开启搜索条件") // 绑定数据库表和字段时，可开启搜索条件
    @ColumnDefault("0")
    @Column(name = "OPENSEARCH", length = 5, nullable = false)
    private Integer openSearch = 0;

    @Column(name = "ISLISTSHOW")
    @Comment("是否列表显示")
    private Integer isListShow = 1;

    @Column(name = "ISORDER")
    @Comment("是否可排序")
    private Integer isOrder = 1;

    @Column(name = "ISRECORD")
    @Comment("是否著录")
    private Integer isRecord = 1;

    @Column(name = "ISRECORDREQUIRED")
    @Comment("是否著录必填")
    private Integer isRecordRequired = 0;

    @Comment("著录输入框类型") // search-带图标前缀的搜索框,input,select,date
    @Column(name = "RE_INPUTBOXTYPE", length = 20)
    private String re_inputBoxType;

    @Comment("著录输入框宽度")
    @Column(name = "RE_INPUTBOXWIDTH", length = 50)
    private String re_inputBoxWidth;

    @Column(name = "ISCHECKEDREQUIRED")
    @Comment("是否检测必填")
    private Integer isCheckedRequired = 0;

    @Comment("是否著录一行显示")
    @Column(name = "RE_ISONELINE", length = 50)
    private Integer re_isOneLine = 0;

    @Comment("输入框类型") // search-带图标前缀的搜索框,input,select,date
    @Column(name = "INPUTBOXTYPE", length = 20)
    private String inputBoxType;

    @Comment("搜索框宽度")
    @Column(name = "SPANWIDTH", length = 50)
    private String spanWidth;

    @Comment("搜索名称") // 不填写则使用disPlayName显示名称
    @Column(name = "LABELNAME", length = 20)
    private String labelName;

    @Comment("绑定数据字典") // 输入框类型select时使用
    @Column(name = "OPTIONCLASS", length = 50)
    private String optionClass;

    @Comment("序号")
    @Column(name = "TABINDEX", length = 10)
    private Integer tabIndex;

    @Comment("操作人员id")
    @Column(name = "USERID", length = 50)
    private String userId;

    @Comment("操作人员名称")
    @Column(name = "USERNAME", length = 50)
    private String userName;

    @Comment("生成时间")
    @Column(name = "CREATETIME", length = 50)
    private String createTime;

    @Comment("更新时间")
    @Column(name = "UPDATETIME", length = 50)
    private String updateTime;

    @Comment("对应数据库表字段id")
    @Column(name = "TABLEFIELDID", length = 50)
    private String tableFieldId;

    @Column(name = "ISHIDE")
    @Comment("是否隐藏")
    private Boolean isHide = false;
}
