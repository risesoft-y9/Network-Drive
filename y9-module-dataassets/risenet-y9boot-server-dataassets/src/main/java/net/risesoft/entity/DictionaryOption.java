package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Y9_DATAASSETS_DICTIONARY_OPTION")
@Comment("字典类型表")
@NoArgsConstructor
@Data
public class DictionaryOption implements Serializable {

    private static final long serialVersionUID = 1763561621577230870L;

    @Id
    @Column(name = "TYPE", length = 255, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键，类型名称")
    private String type;

    @Column(name = "NAME", length = 255, nullable = false)
    @Comment("中文名称")
    private String name;

}