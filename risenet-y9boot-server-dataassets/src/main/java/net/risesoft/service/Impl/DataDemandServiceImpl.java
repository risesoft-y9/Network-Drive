package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.entity.DataDemandAskEntity;
import net.risesoft.entity.DataDemandEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Person;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataDemandAskRepository;
import net.risesoft.repository.DataDemandRepository;
import net.risesoft.repository.spec.DataDemandSpecification;
import net.risesoft.service.DataDemandService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;
import y9.client.rest.platform.permission.PersonRoleApiClient;

@Service(value = "dataDemandService")
@RequiredArgsConstructor
public class DataDemandServiceImpl implements DataDemandService {
	
	private final DataDemandRepository dataDemandRepository;
	private final DataDemandAskRepository dataDemandAskRepository;
	private final PersonApi personApi;
	private final PersonRoleApiClient personRoleApiClient;
	
	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveData(DataDemandEntity dataDemandEntity) {
		try {
			if(StringUtils.isBlank(dataDemandEntity.getId())) {
				dataDemandEntity.setId(Y9IdGenerator.genId());
				// 获取当前登录人信息
				Person person = personApi.get(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId()).getData();
				if(person == null) {
					return Y9Result.failure("该账号不能发布需求");
				}
				if(StringUtils.isBlank(person.getDescription())) {
					return Y9Result.failure("该账号没认证公司信息，不能发布需求，请点击右上角的登录人员在个人中心填写公司名称");
				}
				dataDemandEntity.setCompany(person.getDescription());
				dataDemandEntity.setPublisher(Y9LoginUserHolder.getUserInfo().getName());
				dataDemandEntity.setPublisherId(Y9LoginUserHolder.getPersonId());
				dataDemandEntity.setClickNum(0);
				dataDemandEntity.setExamine(0);
				dataDemandRepository.save(dataDemandEntity);
				return Y9Result.successMsg("保存成功");
			}else {
				DataDemandEntity demandEntity = dataDemandRepository.findById(dataDemandEntity.getId()).orElse(null);
				if(demandEntity == null) {
					return Y9Result.failure("修改失败：数据不存在");
				}
				if(!demandEntity.getPublisherId().equals(Y9LoginUserHolder.getPersonId())) {
					return Y9Result.failure("不是发布者本人，没有操作权限");
				}
				Y9BeanUtil.copyProperties(dataDemandEntity, demandEntity);
				dataDemandRepository.save(demandEntity);
				return Y9Result.successMsg("修改成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteData(String id) {
		dataDemandAskRepository.deleteByDemandId(id);
		dataDemandRepository.deleteById(id);
		return Y9Result.successMsg("删除成功");
	}

	@Override
	public Y9Page<Map<String, Object>> searchPage(String searchText, Integer dataType, Integer industry, Integer budget,
			Integer status, Integer sort, int page, int size) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		Pageable pageable = null;
		if(sort == null) {
			pageable = PageRequest.of(page - 1, size);
		}else if(sort == 1) {
			pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
		}else if(sort == 2) {
			pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "clickNum"));
		}
		DataDemandSpecification spec = new DataDemandSpecification(searchText, dataType, industry, budget, status, "", 1);
		Page<DataDemandEntity> dataPage = dataDemandRepository.findAll(spec, pageable);
		for(DataDemandEntity data : dataPage) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", data.getId());
			map.put("title", data.getTitle());
			map.put("scenario", data.getScenario());
			map.put("description", data.getDescription());
			map.put("date", sdf.format(data.getCreateTime()));
			map.put("status", data.getStatus() == 1 ? "征集中" : "已结束");
			map.put("dataType", data.getDataType() == 1 ? "数据服务" : "数据采购");
			map.put("company", data.getCompany());
			map.put("industry", data.getIndustry());
			map.put("budget", data.getBudget());
			listMap.add(map);
		}
		return Y9Page.success(page, dataPage.getTotalPages(), dataPage.getTotalElements(), listMap, "获取数据成功");
	}

	@Override
	public Y9Result<DataDemandEntity> getById(String id) {
		DataDemandEntity demandEntity = dataDemandRepository.findById(id).orElse(null);
		return Y9Result.success(demandEntity);
	}

	@Override
	public Y9Page<Map<String, Object>> searchPage2(String searchText, int page, int size) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
		// 判断是否系统管理员
		boolean isAdmin = personRoleApiClient.hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId()).getData();
		DataDemandSpecification spec = new DataDemandSpecification(searchText, null, null, null, null, 
				isAdmin ? "" : Y9LoginUserHolder.getPersonId(), null);
		Page<DataDemandEntity> dataPage = dataDemandRepository.findAll(spec, pageable);
		for(DataDemandEntity data : dataPage) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", data.getId());
			map.put("title", data.getTitle());
			map.put("description", data.getDescription());
			map.put("date", sdf.format(data.getCreateTime()));
			map.put("status", data.getStatus());
			map.put("examine", data.getExamine());
			map.put("publisher", data.getPublisher() + "(" + data.getCompany() + ")");
			if(Y9LoginUserHolder.getPersonId().equals(data.getPublisherId())) {
				map.put("isOwner", true);
			}else {
				map.put("isOwner", false);
			}
			map.put("isAdmin", isAdmin);
			listMap.add(map);
		}
		return Y9Page.success(page, dataPage.getTotalPages(), dataPage.getTotalElements(), listMap, "获取数据成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> examineData(String id, Integer examine) {
		try {
			// 判断是否系统管理员
			boolean isAdmin = personRoleApiClient.hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员",
					Y9LoginUserHolder.getPersonId()).getData();
			if(!isAdmin) {
				return Y9Result.failure("没权限");
			}
			DataDemandEntity demandEntity = dataDemandRepository.findById(id).orElse(null);
			if(demandEntity == null) {
				return Y9Result.failure("信息不存在");
			}
			demandEntity.setExamine(examine);
			dataDemandRepository.save(demandEntity);
			return Y9Result.successMsg("审核完成");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("审核失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> stopData(String id) {
		try {
			DataDemandEntity demandEntity = dataDemandRepository.findById(id).orElse(null);
			if(demandEntity == null) {
				return Y9Result.failure("信息不存在");
			}
			if(!demandEntity.getPublisherId().equals(Y9LoginUserHolder.getPersonId())) {
				return Y9Result.failure("不是发布者本人，没有操作权限");
			}
			demandEntity.setStatus(2);
			dataDemandRepository.save(demandEntity);
			return Y9Result.successMsg("操作完成");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("操作失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveText(String demandId, String text) {
		try {
			// 获取当前登录人信息
			Person person = personApi.get(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPersonId()).getData();
			if(person == null) {
				return Y9Result.failure("该账号不能发布信息");
			}
			if(StringUtils.isBlank(person.getDescription())) {
				return Y9Result.failure("该账号没认证公司信息，不能发布，请点击右上角的登录人员在个人中心填写公司名称");
			}
			// 查询主体信息
			DataDemandEntity demandEntity = dataDemandRepository.findById(demandId).orElse(null);
			if(demandEntity == null || demandEntity.getExamine() != 1) {
				return Y9Result.failure("信息不存在，请刷新列表");
			}else {
				demandEntity.setClickNum(demandEntity.getClickNum() + 1);
				dataDemandRepository.save(demandEntity);
			}
			DataDemandAskEntity askEntity = new DataDemandAskEntity();
			askEntity.setId(Y9IdGenerator.genId());
			askEntity.setDemandId(demandId);
			askEntity.setText(text);
			askEntity.setCompany(person.getDescription());
			askEntity.setPublisher(Y9LoginUserHolder.getUserInfo().getName());
			askEntity.setPublisherId(Y9LoginUserHolder.getPersonId());
			dataDemandAskRepository.save(askEntity);
			return Y9Result.successMsg("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	public Y9Result<List<Map<String, Object>>> getAskList(String demandId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		List<DataDemandAskEntity> askList = dataDemandAskRepository.findByDemandIdOrderByCreateTime(demandId);
		// 查询发布者信息
		DataDemandEntity demandEntity = dataDemandRepository.findById(demandId).orElse(null);
		String id = demandEntity.getPublisherId();
		for(DataDemandAskEntity askEntity : askList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("text", askEntity.getText());
			map.put("date", sdf.format(askEntity.getCreateTime()));
			if(askEntity.getPublisherId().equals(id)) {
				map.put("publisher", "发布者");
			}else {
				map.put("publisher", askEntity.getCompany() + "（" + askEntity.getPublisher() + "）");
			}
			listMap.add(map);
		}
		return Y9Result.success(listMap);
	}

	@Override
	public Y9Page<Map<String, Object>> searchPage3(String searchText, int page, int size) {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
		List<String> ids = dataDemandAskRepository.findByPublisherId(Y9LoginUserHolder.getPersonId());
		Page<DataDemandEntity> dataPage = null;
		if(StringUtils.isBlank(searchText)) {
			dataPage = dataDemandRepository.findByIdInAndExamine(ids, 1, pageable);
		}else {
			dataPage = dataDemandRepository.findByIdInAndTitleContainingAndExamine(ids, searchText, 1, pageable);
		}
		for(DataDemandEntity demandEntity : dataPage) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", demandEntity.getId());
			map.put("title", demandEntity.getTitle());
			map.put("description", demandEntity.getDescription());
			map.put("publisher", demandEntity.getPublisher() + "(" + demandEntity.getCompany() + ")");
			map.put("status", demandEntity.getStatus());
			listMap.add(map);
		}
		return Y9Page.success(page, dataPage.getTotalPages(), dataPage.getTotalElements(), listMap, "获取数据成功");
	}

}