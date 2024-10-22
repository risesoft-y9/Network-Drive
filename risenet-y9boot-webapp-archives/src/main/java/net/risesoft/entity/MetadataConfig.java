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

    @Comment("显示名称")
    @Column(name = "DISPLAYNAME", length = 50, nullable = false)
    private String disPlayName;

    @Column(name = "DESCRIPTION", length = 500)
    @Comment("描述")
    private String description;

    @Column(name = "FIELD_SOURCE", length = 50)
    @Comment("字段来源")
    @ColumnDefault("系统内置")
    private String fieldSource;

    @Comment("显示宽度")
    @Column(name = "DISPLAYWIDTH", length = 50, nullable = false)
    private String disPlayWidth;

    @Comment("排列")
    @Column(name = "DISPLAYALIGN", length = 10, nullable = false)
    private String disPlayAlign;

    @Comment("是否开启搜索条件") // 绑定数据库表和字段时，可开启搜索条件
    @ColumnDefault("0")
    @Column(name = "OPENSEARCH", length = 5, nullable = false)
    private Integer openSearch = 0;

    @Column(name = "ISLISTSHOW")
    @Comment("是否列表显示")
    private Integer isListShow = 0;

    @Column(name = "ISORDER")
    @Comment("是否可排序")
    private Integer isOrder = 0;

    @Column(name = "ISRECORD")
    @Comment("是否著录")
    private Integer isRecord = 0;

    @Column(name = "ISRECORDNULL")
    @Comment("是否著录必填")
    private Integer isRecordNull = 0;

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
}
