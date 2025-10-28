package net.risesoft.repository.spec;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import net.risesoft.entity.DataDemandEntity;

public class DataDemandSpecification implements Specification<DataDemandEntity> {

    private static final long serialVersionUID = 3960072132630601786L;

    private Integer industry;
    private Integer budget;
    private String publisherId;
    private Integer examine;
    private Integer dataType;
    private Integer status;
    private String searchText;

    public DataDemandSpecification() {
        super();
    }

    public DataDemandSpecification(
        String searchText,
        Integer dataType,
        Integer industry,
        Integer budget,
        Integer status,
        String publisherId,
        Integer examine) {
        super();
        this.industry = industry;
        this.budget = budget;
        this.publisherId = publisherId;
        this.examine = examine;
        this.status = status;
        this.searchText = searchText;
        this.dataType = dataType;
    }

    @Override
    public Predicate toPredicate(Root<DataDemandEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();

        if (industry != null) {
            expressions.add(cb.equal(root.get("industry").as(Integer.class), industry));
        }
        if (budget != null) {
            expressions.add(cb.equal(root.get("budget").as(Integer.class), budget));
        }
        if (status != null) {
            expressions.add(cb.equal(root.get("status").as(Integer.class), status));
        }
        if (examine != null) {
            expressions.add(cb.equal(root.get("examine").as(Integer.class), examine));
        }
        if (dataType != null) {
            expressions.add(cb.equal(root.get("dataType").as(Integer.class), dataType));
        }
        if (StringUtils.isNotBlank(publisherId)) {
            expressions.add(cb.equal(root.get("publisherId").as(String.class), publisherId));
        }
        if (StringUtils.isNotBlank(searchText)) {
            expressions.add(cb.or(cb.like(root.get("title").as(String.class), "%" + searchText + "%"),
                cb.like(root.get("scenario").as(String.class), "%" + searchText + "%"),
                cb.like(root.get("description").as(String.class), "%" + searchText + "%"),
                cb.like(root.get("company").as(String.class), "%" + searchText + "%")));
        }
        return predicate;
    }

    public Integer getIndustry() {
        return industry;
    }

    public void setIndustry(Integer industry) {
        this.industry = industry;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public Integer getExamine() {
        return examine;
    }

    public void setExamine(Integer examine) {
        this.examine = examine;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

}
