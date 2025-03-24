package net.risesoft.y9public.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_API_SERVICE_LOG")
@org.hibernate.annotations.Table(comment = "API接口服务日志表", appliesTo = "Y9_API_SERVICE_LOG")
@NoArgsConstructor
@Data
public class ApiServiceLogEntity extends BaseEntity {

	private static final long serialVersionUID = -6254919064578738209L;

	@Id
	@Column(name = "ID", length = 50, nullable = false)
	@Comment(value = "主键")
	private String id;
	
	@Column(name = "APPNAME", length = 100, nullable = false)
	@Comment(value = "调用者")
	private String appName;
	
	@Column(name = "REQUESTURL", length = 500)
	@Comment(value = "请求的url")
	private String requestUrl;
	
	@Column(name = "HOSTIP", length = 50)
	@Comment(value = "请求者IP")
	private String hostIp;
	
	@Column(name = "SERVERIP", length = 50)
	@Comment(value = "接口服务器IP")
	private String serverIp;

	@Column(name = "REQUESTPARAMS", length = 800)
	@Comment(value = "请求参数")
	private String requestParams;
	
	@Column(name = "RESULT", length = 500)
	@Comment(value = "请求结果")
	private String result;

}