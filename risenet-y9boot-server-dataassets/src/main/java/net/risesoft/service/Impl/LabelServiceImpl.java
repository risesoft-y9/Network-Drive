package net.risesoft.service.Impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import net.risesoft.entity.AssetsLabelEntity;
import net.risesoft.entity.LabelCatalogEntity;
import net.risesoft.entity.LabelDataEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.AssetsLabelRepository;
import net.risesoft.repository.LabelCatalogRepository;
import net.risesoft.repository.LabelDataRepository;
import net.risesoft.service.LabelService;

@Service(value = "labelService")
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
	
	private final LabelCatalogRepository labelCatalogRepository;
	private final LabelDataRepository labelDataRepository;
	private final AssetsLabelRepository assetsLabelRepository;
	
	@Override
	public LabelCatalogEntity getById(String id) {
		return labelCatalogRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteData(String id) {
		// 检验目录和子目录下是否存在文件
		String msg = "";
		LabelCatalogEntity catalogEntity = getById(id);
		if(catalogEntity != null) {
			if(labelDataRepository.countByParentId(id) > 0) {
				msg = "目录[" + catalogEntity.getName() + "]下存在标签，无法删除";
			}else {
				msg = checkData(id, msg);
			}
		}
		if(StringUtils.isNotBlank(msg)) {
			return Y9Result.failure(msg);
		}
		labelCatalogRepository.deleteById(id);
		deleteChild(id);
		return Y9Result.successMsg("删除成功");
	}
	
	private void deleteChild(String parentId) {
		List<LabelCatalogEntity> list = labelCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(LabelCatalogEntity catalog : list) {
			labelCatalogRepository.delete(catalog);
			deleteChild(catalog.getId());
		}
	}
	
	private String checkData(String parentId, String msg) {
		// 查找子节点
		List<LabelCatalogEntity> list = labelCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(LabelCatalogEntity catalog : list) {
			if(labelDataRepository.countByParentId(catalog.getId()) > 0) {
				return "目录[" + catalog.getName() + "]下存在标签，无法删除";
			}else {
				checkData(catalog.getId(), msg);
			}
		}
		return "";
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<LabelCatalogEntity> saveData(LabelCatalogEntity entity) {
		try {
			LabelCatalogEntity info = labelCatalogRepository.findByNameAndParentId(entity.getName(), entity.getParentId());
			if(info != null && !info.getId().equals(entity.getId())) {
				return Y9Result.failure("该节点下已存在[" + entity.getName() + "]节点，不能重复添加");
			}
			if (StringUtils.isBlank(entity.getId())) {
				entity.setId(Y9IdGenerator.genId());
			}
			if(entity.getTabIndex() == null) {
				Integer tabIndex = labelCatalogRepository.getMaxTabIndex(entity.getParentId());
				if(tabIndex == null) {
					tabIndex = 1;
				}else {
					tabIndex = tabIndex + 1;
				}
				entity.setTabIndex(tabIndex);
			}
			if(entity.getLevel() == null) {
				LabelCatalogEntity catalogEntity = getById(entity.getParentId());
				if(catalogEntity == null) {
					entity.setLevel(1);
				}else {
					entity.setLevel(catalogEntity.getLevel() + 1);
				}
			}
			return Y9Result.success(labelCatalogRepository.save(entity), "保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> getTree(String parentId, String searchName) {
		if(StringUtils.isBlank(parentId)) {
			parentId = "0";
		}
		List<String> ids = null;
		if(StringUtils.isNotBlank(searchName)) {
			ids = searchTree(searchName);
		}
		return getListMap(parentId, ids);
	}
	
	private List<Map<String, Object>> getListMap(String parentId, List<String> ids) {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		List<LabelCatalogEntity> catalogEntities = labelCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		for(LabelCatalogEntity catalogEntity : catalogEntities) {
			if(ids != null && !ids.contains(catalogEntity.getId())) {
				continue;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", catalogEntity.getId());
			map.put("name", catalogEntity.getName());
			map.put("parentId", catalogEntity.getParentId());
			map.put("description", catalogEntity.getDescription());
			map.put("tabIndex", catalogEntity.getTabIndex());
			map.put("level", catalogEntity.getLevel());
			map.put("isChild", checkChild(catalogEntity.getId()));
			// 获取子节点
			if(ids != null && ids.size() > 0) {
				map.put("children", getListMap(catalogEntity.getId(), ids));
			}
			listMap.add(map);
		}
		return listMap;
	}
	
	private boolean checkChild(String parentId) {
		List<LabelCatalogEntity> catalogEntities = labelCatalogRepository.findByParentIdOrderByTabIndex(parentId);
		if(catalogEntities != null && catalogEntities.size() > 0) {
			return true;
		}
		return false;
	}
	
	private List<String> searchTree(String searchName) {
		List<String> ids = new ArrayList<String>();
		List<LabelCatalogEntity> catalogEntities = labelCatalogRepository.findByNameContaining(searchName);
		for(LabelCatalogEntity catalogEntity : catalogEntities) {
			if(!ids.contains(catalogEntity.getId())) {
				ids.add(catalogEntity.getId());
			}
			// 递归获取所有上级节点
			getParentId(ids, catalogEntity.getParentId());
		}
		return ids;
	}
	
	private void getParentId(List<String> ids, String id) {
		LabelCatalogEntity catalogEntity = getById(id);
		if(catalogEntity != null) {
			if(!ids.contains(id)) {
				ids.add(id);
			}
			getParentId(ids, catalogEntity.getParentId());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveLabelData(LabelDataEntity labelDataEntity) {
		try {
			if(StringUtils.isBlank(labelDataEntity.getId())) {
				labelDataEntity.setId(Y9IdGenerator.genId());
			}
			labelDataRepository.save(labelDataEntity);
			return Y9Result.successMsg("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteLabelData(String id) {
		labelDataRepository.deleteById(id);
		return Y9Result.successMsg("删除成功");
	}

	@Override
	public Page<LabelDataEntity> searchPage(String parentId, int page, int size) {
		if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "tabIndex"));
		return labelDataRepository.findByParentId(parentId, pageable);
	}

	@Override
	public Y9Result<List<Map<String, Object>>> getLabelDataList() {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		List<LabelDataEntity> dataEntities = labelDataRepository.findAll();
		for(LabelDataEntity entity : dataEntities) {
			Map<String, Object> map = new HashMap<String, Object>();
			LabelCatalogEntity labelCatalogEntity = getById(entity.getParentId());
			map.put("label", labelCatalogEntity.getName() + "-" + entity.getName());
			map.put("key", entity.getId());
			listMap.add(map);
		}
		return Y9Result.success(listMap);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveAssetsLabel(String ids, Long assetsId) {
		try {
			AssetsLabelEntity assetsLabelEntity = assetsLabelRepository.findByAssetsId(assetsId);
			if(assetsLabelEntity == null) {
				assetsLabelEntity = new AssetsLabelEntity();
				assetsLabelEntity.setId(Y9IdGenerator.genId());
			}
			assetsLabelEntity.setAssetsId(assetsId);
			assetsLabelEntity.setLabelIds(ids);
			assetsLabelRepository.save(assetsLabelEntity);
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
		return Y9Result.successMsg("保存成功");
	}

	@Override
	public Y9Result<List<String>> getLabels(Long assetsId) {
		List<String> ids = new ArrayList<String>();
		AssetsLabelEntity assetsLabelEntity = assetsLabelRepository.findByAssetsId(assetsId);
		if(assetsLabelEntity != null) {
			ids = Arrays.asList(assetsLabelEntity.getLabelIds().split(","));
		}
		return Y9Result.success(ids);
	}

	@Override
	public String getLabelData(Long assetsId) {
		String labelData = "";
		AssetsLabelEntity assetsLabelEntity = assetsLabelRepository.findByAssetsId(assetsId);
		if(assetsLabelEntity != null) {
			String[] ids = assetsLabelEntity.getLabelIds().split(",");
			for(String id : ids) {
				LabelDataEntity labelDataEntity = labelDataRepository.findById(id).orElse(null);
				if(labelDataEntity != null) {
					labelData = labelDataEntity.getName() + "/" + labelData;
				}
			}
		}
		return StringUtils.isNotBlank(labelData) ? labelData.substring(0, labelData.length() - 1) : "";
	}

}