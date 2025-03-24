package net.risesoft.y9public.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_API_SERVICE")
@org.hibernate.annotations.Table(comment = "API接口服务表", appliesTo = "Y9_API_SERVICE")
@NoArgsConstructor
@Data
public class ApiServiceEntity extends BaseEntity {

	private static final long serialVersionUID = -1936626123122075662L;

	@Id
	@Column(name = "ID", length = 50, nullable = false)
	@Comment(value = "主键")
	private String id;
	
	@Column(name = "PARENTID", length = 50)
	@Comment(value = "目录ID")
	private String parentId;
	
	@Column(name = "DATASOURCEID", length = 50)
	@Comment(value = "数据源ID")
	private String dataSourceId;
	
	@Column(name = "APIURL", length = 200)
	@Comment(value = "接口url")
	private String apiUrl;
	
	@Column(name = "APINAME", length = 500)
	@Comment(value = "接口名称")
	private String apiName;
	
	@Column(name = "METHOD", length = 10)
	@Comment(value = "请求方式：GET/POST")
	private String method;
	
	@Lob
    @Column(name = "SQLDATA")
    @Comment(value = "执行SQL")
    private String sqlData;

	@Column(name = "PARAMS", length = 1800)
	@Comment(value = "请求参数")
	private String params;
	
	@Column(name = "REMARK", length = 1200)
	@Comment(value = "备注")
	private String remark;
	
	@Column(name = "APITYPE")
	@Comment(value = "接口类型：0-公开API，1-私有API")
	private Integer apiType;

}