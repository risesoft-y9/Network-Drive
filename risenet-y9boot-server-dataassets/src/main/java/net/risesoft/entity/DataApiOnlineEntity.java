package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_API_ONLINE")
@Comment("接口在线信息表")
@NoArgsConstructor
@Data
public class DataApiOnlineEntity extends BaseEntity {

	private static final long serialVersionUID = -4109523700335204141L;

	@Id
	@Column(name = "ID", length = 38, nullable = false)
	@Comment(value = "主键")
	private String id;

	@Column(name = "NAME", length = 500, nullable = false)
	@Comment(value = "名称")
	private String name;
	
	@Column(name = "PARENTID", length = 50, nullable = false)
	@Comment(value = "父节点ID")
	private String parentId;
	
	@Column(name = "TYPE", length = 50, nullable = false)
	@Comment(value = "类别：folder/api")
	private String type;

	@Column(name = "CREATOR", length = 50)
    @Comment(value = "创建人")
    private String creator;

    @Column(name = "CREATORID", length = 50)
    @Comment(value = "创建人ID")
    private String creatorId;

}