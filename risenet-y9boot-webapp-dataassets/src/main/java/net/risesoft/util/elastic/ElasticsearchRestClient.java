package net.risesoft.util.elastic;

import net.risesoft.y9.json.Y9JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elastic Rest API调用
 * @author pzx
 *
 */
public class ElasticsearchRestClient {
	
	private String url;// elastic连接地址
	private String username;// 用户名
	private String password;// 密码
	
	public ElasticsearchRestClient(String url, String username, String password) {
		super();
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * 获取所有索引
	 * @return
	 * @throws Exception
	 */
	public List<String> getIndexs() throws Exception {
		String data = HttpClientEsUtil.httpGet(url + "/_cat/indices?format=json", username, password);
		List<String> list = new ArrayList<String>();
		if(!data.equals("failed")) {
			List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(data);
			for(Map<String, Object> map : listMap) {
				if("yellow".equals(map.get("health").toString())) {
					list.add(map.get("index").toString());
				}
			}
		}
    	return list;
    }
  
    /**
     * 根据索引名称获取映射
     * @param indexName
     * @return {"my_index":{"mappings":{"properties":{"age":{"type":"keyword"},"name":{"type":"text","analyzer":"ik_smart"}}}}}
     * @throws Exception
     */
    public String getMapping(String indexName) throws Exception {
    	return HttpClientEsUtil.httpGet(url + "/" + indexName + "/_mapping", username, password);
    }
    
    /**
     * 根据id获取文档
     * @param id
     * @param indexName
     * @return
     * @throws Exception
     */
    public String getDocument(String id, String indexName) throws Exception {
    	String response = HttpClientEsUtil.httpGet(url + "/" + indexName + "/_doc/" + id, username, password);
    	if(response.equals("failed")) {
    		throw new Exception("根据id获取文档失败");
    	}
    	Map<String, Object> map = Y9JsonUtil.readHashMap(response);
    	String data = Y9JsonUtil.writeValueAsString(map.get("_source"));
    	return data;
    }
    
    /**
     * 获取索引表数据量
     * @param indexName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public int getCount(String indexName, String query) throws Exception {
    	String data = HttpClientEsUtil.httpPost(query, url + "/" + indexName + "/_search", username, password);
    	if(!data.equals("failed")) {
    		Map<String, Object> map = Y9JsonUtil.readHashMap(data);
    		Integer total = 0;
    		Map<String, Object> hits = (Map<String, Object>) map.get("hits");
    		if(hits != null) {
    			try {
					total = (Integer) hits.get("total");
				} catch (Exception e) {
					Map<String, Object> totalMap = (Map<String, Object>) hits.get("total");
					total = (Integer) totalMap.get("value");
				}
    		}
    		return total;
    	}else {
    		throw new Exception("获取索引表数据量失败");
    	}		
    }
    
    /**
     * 查询数据
     * @param queryModel
     * @param indexName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Map<String, Object> search(String indexName) throws Exception {
    	String response = HttpClientEsUtil.httpPost("", url + "/" + indexName + "/_search", username, password);
    	if(response.equals("failed")) {
    		throw new Exception("索引表-" + indexName + "查询报错，请检查查询接口报错信息");
    	}else {
    		Map<String, Object> map = new HashMap<String, Object>();
    		Map<String, Object> data = Y9JsonUtil.readHashMap(response);
    		Integer total = 0;
    		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
    		Map<String, Object> hits = (Map<String, Object>) data.get("hits");
    		if(hits != null) {
    			try {
					total = (Integer) hits.get("total");
				} catch (Exception e) {
					Map<String, Object> totalMap = (Map<String, Object>) hits.get("total");
					total = (Integer) totalMap.get("value");
				}
    			List<Map<String, Object>> hits_list = (List<Map<String, Object>>) hits.get("hits");
    			for(Map<String, Object> hit : hits_list) {
    				listMap.add((Map<String, Object>) hit.get("_source"));
    			}
    		}
    		map.put("total", total);
    		map.put("data", listMap);
        	return map;
    	}
    }
}
