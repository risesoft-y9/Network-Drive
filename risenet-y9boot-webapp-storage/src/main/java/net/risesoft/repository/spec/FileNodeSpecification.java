package net.risesoft.repository.spec;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import net.risesoft.entity.FileNode;
import net.risesoft.support.FileNodeType;

public class FileNodeSpecification implements Specification<FileNode> {

    private static final long serialVersionUID = 5267610881278732102L;

    private String orgId;
    private String userId;
    private String parentId;
    private FileNodeType fileType;
    private String name;
    private String listType;
    private Boolean deleted;
    private Date startDate;
    private Date endDate;

    public FileNodeSpecification() {}

    public FileNodeSpecification(String userId, String parentId, boolean deleted) {
        this.userId = userId;
        this.parentId = parentId;
        this.deleted = deleted;
    }

    public FileNodeSpecification(boolean deleted, String parentId, String listType, String searchName) {
        this.name = searchName;
        this.parentId = parentId;
        this.listType = listType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String userId, String parentId, FileNodeType fileType, boolean deleted) {
        this.userId = userId;
        this.parentId = parentId;
        this.fileType = fileType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String parentId, FileNodeType fileType, boolean deleted) {
        this.parentId = parentId;
        this.fileType = fileType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(boolean deleted, String parentId, String searchName) {
        // this.listType = listType;
        this.parentId = parentId;
        this.name = searchName;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String personId, String parentId, FileNodeType fileType, String listType,
        String searchName, boolean deleted) {
        this.userId = personId;
        this.parentId = parentId;
        this.fileType = fileType;
        this.name = searchName;
        this.listType = listType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String parentId, FileNodeType fileType, String listType, String searchName,
        boolean deleted) {
        this.orgId = orgId;
        this.parentId = parentId;
        this.fileType = fileType;
        this.name = searchName;
        this.listType = listType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String parentId, FileNodeType fileType, String listType, String searchName,
        String orgId, boolean deleted) {
        this.orgId = orgId;
        this.parentId = parentId;
        this.fileType = fileType;
        this.name = searchName;
        this.listType = listType;
        this.deleted = deleted;
    }

    public FileNodeSpecification(String parentId, FileNodeType fileType, String listType, String searchName,
        Date startDate, Date endDate, boolean deleted) {
        this.parentId = parentId;
        this.fileType = fileType;
        this.name = searchName;
        this.listType = listType;
        this.deleted = deleted;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public FileNodeSpecification(String userId, String parentId, String name, boolean deleted) {
        this.userId = userId;
        this.parentId = parentId;
        this.name = name;
        this.deleted = deleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public FileNodeType getFileType() {
        return fileType;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setFileType(FileNodeType fileType) {
        this.fileType = fileType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public Predicate toPredicate(Root<FileNode> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();

        if (StringUtils.isNotBlank(userId)) {
            expressions.add(cb.equal(root.get("userId").as(String.class), userId));
        }

        if (StringUtils.isNotBlank(orgId)) {
            expressions.add(cb.equal(root.get("orgId").as(String.class), orgId));
        }

        if (StringUtils.isNotBlank(name)) {
            expressions.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
        } else {
            if (StringUtils.isNotBlank(parentId)) {
                expressions.add(cb.equal(root.get("parentId").as(String.class), parentId));
            }
        }

        if (fileType != null) {
            expressions.add(cb.equal(root.get("fileType").as(Integer.class), fileType.getValue()));
        }

        if (StringUtils.isNotBlank(listType)) {
            expressions.add(cb.equal(root.get("listType").as(String.class), listType));
        }

        if (endDate != null && startDate != null) {
            expressions.add(cb.between(root.get("createTime"), startDate, endDate));
        }

        if (deleted != null) {
            expressions.add(cb.equal(root.get("deleted"), deleted));
        }
        return predicate;
    }
}
