package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataApiOnlineService;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.api.auth.annotation.ApiAuth;
import net.risesoft.entity.DataApiOnlineEntity;
import net.risesoft.entity.DataApiOnlineInfoEntity;

@Validated
@RestController
@RequestMapping(value = "/vue/apionline", produces = "application/json")
@RequiredArgsConstructor
public class ApiOnlineController {

	private final DataApiOnlineService dataApiOnlineService;
	
	@GetMapping(value = "/get")
	@ApiAuth(roles = {"admin"}, permitsPerSecond = 2.0, value = "获取信息", path = "/services/rest/get", method = "GET")
    public Y9Result<String> get() {
        return Y9Result.successMsg("hello world");
    }
	
	/**
	 * 保存接口信息
	 * @param entity
	 * @return
	 */
	@PostMapping(value = "/saveData")
	public Y9Result<DataApiOnlineEntity> saveData(@RequestBody String json) {
		Map<String, Object> map = Y9JsonUtil.readHashMap(json);
		
		DataApiOnlineEntity entity = new DataApiOnlineEntity();
		entity.setId((String)map.get("id"));
		entity.setName((String)map.get("name"));
		entity.setParentId((String)map.get("parentId"));
		entity.setType((String)map.get("type"));
		
		if(map.get("type").equals("folder")) {
			return dataApiOnlineService.saveData(entity, null);
		}
		DataApiOnlineInfoEntity infoEntity = new DataApiOnlineInfoEntity();
		infoEntity.setId(entity.getId());
		infoEntity.setFormData(Y9JsonUtil.writeValueAsString(map.get("ApiForm")));
		return dataApiOnlineService.saveData(entity, infoEntity);
	}
	
	/**
	 * 删除接口信息
	 * @param id
	 * @return
	 */
	@PostMapping(value = "/deleteData")
	public Y9Result<List<String>> deleteData(@RequestParam String id) {
		return dataApiOnlineService.deleteData(id);
	}
	
	/**
	 * 获取接口树
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/getTree")
	public Y9Result<List<Map<String, Object>>> getTree(String id) {
		return Y9Result.success(dataApiOnlineService.getTree(id));
	}
	
	/**
	 * 获取下拉框列表
	 * @return
	 */
	@GetMapping(value = "/getSelectTree")
	public Y9Result<List<Map<String, Object>>> getSelectTree() {
		return Y9Result.success(dataApiOnlineService.getSelectTree());
	}
	
	/**
	 * 获取接口信息
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/getApiInfo")
	public Y9Result<Map<String, Object>> getApiInfo(String id) {
		return dataApiOnlineService.getApiInfo(id);
	}
	
}
