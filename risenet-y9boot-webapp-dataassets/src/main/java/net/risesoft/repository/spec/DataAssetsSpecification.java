package net.risesoft.repository.spec;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import net.risesoft.entity.DataAssets;

public class DataAssetsSpecification implements Specification<DataAssets> {

    private static final long serialVersionUID = 5267610881278732102L;

    public DataAssetsSpecification() {}

    @Override
    public Predicate toPredicate(Root<DataAssets> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();

        return predicate;
    }
}
