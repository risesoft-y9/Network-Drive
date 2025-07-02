package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_LABELCATALOG")
@org.hibernate.annotations.Table(comment = "标签目录表", appliesTo = "Y9_DATAASSETS_LABELCATALOG")
@NoArgsConstructor
@Data
public class LabelCatalogEntity extends BaseEntity {

	private static final long serialVersionUID = 9125796368575051925L;

	@Id
	@Column(name = "ID", length = 38, nullable = false)
	@Comment(value = "主键")
	private String id;

	@Column(name = "NAME", length = 200, nullable = false)
	@Comment(value = "目录名称")
	private String name;
	
	@Column(name = "DESCRIPTION", length = 800)
	@Comment(value = "描述")
	private String description;
	
	@Column(name = "PARENTID", length = 38)
	@Comment(value = "上级节点ID")
	@ColumnDefault("0")
	private String parentId;
	
	@Column(name = "LEVEL")
	@Comment(value = "目录级别")
	@ColumnDefault("1")
	private Integer level;
	
	@Column(name = "TABINDEX")
	@Comment(value = "排序")
	@ColumnDefault("1")
	private Integer tabIndex;

}