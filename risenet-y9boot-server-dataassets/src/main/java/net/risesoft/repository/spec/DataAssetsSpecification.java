package net.risesoft.repository.spec;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import net.risesoft.entity.DataAssets;

public class DataAssetsSpecification implements Specification<DataAssets> {

	private static final long serialVersionUID = -8033435846816389785L;
	
	private String tenantId;
	private String categoryId;
	private String name;
	private String code;
	private Boolean isDeleted;
	private Integer status;
	private String dataState;
	private String creatorId;
	private String searchText;
	private String appScenarios;
	private String dataZone;
	private String productType;
	
	public DataAssetsSpecification() {
		super();
	}

	public DataAssetsSpecification(String categoryId, String name, String tenantId, String code, Boolean isDeleted, Integer status,
			String dataState, String creatorId, String searchText, String appScenarios, String dataZone, String productType) {
		super();
		this.categoryId = categoryId;
		this.name = name;
		this.tenantId = tenantId;
		this.code = code;
		this.isDeleted = isDeleted;
		this.status = status;
		this.dataState = dataState;
		this.creatorId = creatorId;
		this.searchText = searchText;
		this.appScenarios = appScenarios;
		this.dataZone = dataZone;
		this.productType = productType;
	}

	@Override
	public Predicate toPredicate(Root<DataAssets> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Predicate predicate = cb.conjunction();
		List<Expression<Boolean>> expressions = predicate.getExpressions();

		if (StringUtils.isNotBlank(categoryId)) {
			expressions.add(cb.equal(root.get("categoryId").as(String.class), categoryId));
		}
		if (StringUtils.isNotBlank(name)) {
			expressions.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
		}
		if (StringUtils.isNotBlank(tenantId)) {
			expressions.add(cb.equal(root.get("tenantId").as(String.class), tenantId));
		}
		if (StringUtils.isNotBlank(code)) {
			expressions.add(cb.like(root.get("code").as(String.class), "%" + code + "%"));
		}
		if (isDeleted != null) {
			expressions.add(cb.equal(root.get("isDeleted"), isDeleted));
		}
		if (status != null) {
			expressions.add(cb.equal(root.get("status").as(Integer.class), status));
		}
		if (StringUtils.isNotBlank(dataState)) {
			expressions.add(cb.equal(root.get("dataState").as(String.class), dataState));
		}
		if (StringUtils.isNotBlank(creatorId)) {
			expressions.add(cb.equal(root.get("creatorId").as(String.class), creatorId));
		}
		if (StringUtils.isNotBlank(appScenarios)) {
			expressions.add(cb.equal(root.get("appScenarios").as(String.class), appScenarios));
		}
		if (StringUtils.isNotBlank(dataZone)) {
			expressions.add(cb.equal(root.get("dataZone").as(String.class), dataZone));
		}
		if (StringUtils.isNotBlank(productType)) {
			expressions.add(cb.equal(root.get("productType").as(String.class), productType));
		}
		if(StringUtils.isNotBlank(searchText)) {
			expressions.add(cb.or(cb.like(root.get("name").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("code").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("remark").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("dataPurpose").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("keyField").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("dataProvider").as(String.class), "%" + searchText + "%"), 
					cb.like(root.get("dataOwner").as(String.class), "%" + searchText + "%")));
		}
		return predicate;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDataState() {
		return dataState;
	}

	public void setDataState(String dataState) {
		this.dataState = dataState;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getAppScenarios() {
		return appScenarios;
	}

	public void setAppScenarios(String appScenarios) {
		this.appScenarios = appScenarios;
	}

	public String getDataZone() {
		return dataZone;
	}

	public void setDataZone(String dataZone) {
		this.dataZone = dataZone;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
}
