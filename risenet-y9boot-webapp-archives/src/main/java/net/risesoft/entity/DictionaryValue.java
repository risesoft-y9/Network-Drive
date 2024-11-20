package net.risesoft.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_ARCHIVES_DICTIONARY_VALUE")
@org.hibernate.annotations.Table(comment = "字典数据表", appliesTo = "Y9_ARCHIVES_DICTIONARY_VALUE")
@NoArgsConstructor
@Data
public class DictionaryValue implements Serializable {

    private static final long serialVersionUID = -1482214762841129451L;

    @Id
    @Column(name = "ID", length = 255, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键id")
    private String id;

    @Column(name = "CODE", length = 255)
    @Comment("数据代码")
    private String code;

    @Column(name = "NAME", length = 255, nullable = false)
    @Comment("主键名称")
    private String name;

    @Column(name = "TABINDEX", length = 10, nullable = false)
    @Comment("排序号")
    private Integer tabIndex;

    @Column(name = "TYPE", length = 255, nullable = false)
    @Comment("字典类型")
    private String type;

    @Column(name = "DEFAULTSELECTED", length = 2, nullable = false)
    @Comment("是否默认选中")
    private Integer defaultSelected = 0;

    @Column(name = "UPDATETIME", length = 50)
    @Comment("更新时间")
    private String updateTime;

}