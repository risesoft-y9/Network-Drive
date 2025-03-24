package net.risesoft.pojo;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApiServiceModel {

	private String id;
	
	private String parentId;
	
	private String dataSourceId;
	
	private String apiUrl;
	
	private String apiName;
	
	private String method;
	
    private String sqlData;

	private List<Map<String, Object>> params;
	
	private String remark;
	
	private Integer apiType;

}