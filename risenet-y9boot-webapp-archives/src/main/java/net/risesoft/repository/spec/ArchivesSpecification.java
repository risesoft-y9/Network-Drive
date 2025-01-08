package net.risesoft.repository.spec;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import net.risesoft.entity.Archives;

public class ArchivesSpecification implements Specification<Archives> {

    private static final long serialVersionUID = 5267610881278732102L;

    public ArchivesSpecification() {}

    @Override
    public Predicate toPredicate(Root<Archives> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();

        return predicate;
    }
}
