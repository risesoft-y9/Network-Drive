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
@Table(name = "Y9_DATAASSETS_ASSETSLABEL")
@org.hibernate.annotations.Table(comment = "资产标注数据表", appliesTo = "Y9_DATAASSETS_ASSETSLABEL")
@NoArgsConstructor
@Data
public class AssetsLabelEntity extends BaseEntity {

	private static final long serialVersionUID = 5862867168735406592L;

	@Id
	@Column(name = "ID", length = 38, nullable = false)
	@Comment(value = "主键")
	private String id;
	
	@Column(name = "ASSETSID")
	@Comment(value = "资产ID")
	private Long assetsId;

	@Column(name = "LABELIDS", length = 1200)
	@Comment(value = "标签ID")
	private String labelIds;

}