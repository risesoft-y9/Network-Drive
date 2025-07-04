package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.json.Y9JsonUtil;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/service/cms")
public class CmsController {
	
    @SuppressWarnings("unchecked")
	@GetMapping("/getNewsData")
	public Y9Result<List<Map<String, Object>>> getNewsData(String code) {
    	List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
    	try {
    		String serverUrl = Y9Context.getProperty("y9.service.cms.directUrl");
    		String tenantId = Y9Context.getProperty("y9.app.dataAssets.tenantId");
			// 创建CloseableHttpClient
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpGet httpGet = new HttpGet(serverUrl + "/visit/pageDocList/" + tenantId +"?path="+code+"&page=1&rows=10");

			CloseableHttpResponse response = client.execute(httpGet);
			String obj = EntityUtils.toString(response.getEntity());
			Map<String, Object> map = Y9JsonUtil.readHashMap(obj);
			if((Boolean)map.get("success")) {
				List<Map<String, Object>> rows = (List<Map<String, Object>>) map.get("rows");
				for(Map<String, Object> data : rows) {
					Map<String, Object> rmap = new HashMap<String, Object>();
					rmap.put("id", data.get("id"));
					rmap.put("title", data.get("title"));
					rmap.put("time", data.get("releaseDate"));
					rmap.put("imgUrl", data.get("imgUrl"));
					rmap.put("path", data.get("url"));
					listMap.add(rmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Y9Result.success(listMap);
    }

}
