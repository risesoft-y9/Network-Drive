package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.Category;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.enums.DataSourceEnums;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Manager;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.CategoryRepository;
import net.risesoft.service.CategoryService;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.org.ManagerApiClient;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ManagerApiClient managerApiClient;

    @Override
    public Page<Category> pageAll(int page, int rows) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createDate");
        PageRequest pageable = PageRequest.of(page > 0 ? page - 1 : 0, rows, sort);
        return categoryRepository.findAll(new Specification<Category>() {
            @Override
            public Predicate toPredicate(Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> list = new ArrayList<>();
                Predicate[] predicates = new Predicate[list.size()];
                list.toArray(predicates);
                return builder.and(predicates);
            }
        }, pageable);
    }

    @Override
    public Category findByMark(String mark) {
        return categoryRepository.findByMark(mark);
    }

    @Override
    public void remove(String id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            category.setDeleted(true);
            categoryRepository.save(category);
        }
    }

    @Override
    public Category saveOrUpdate(Category category) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            UserInfo person = Y9LoginUserHolder.getUserInfo();
            String id = category.getId();
            if (StringUtils.isNotEmpty(id)) {
                Category oldof = categoryRepository.findById(id).orElse(null);
                if (null != oldof) {
                    oldof.setModifyDate(sdf.format(new Date()));
                    oldof.setName(category.getName());
                    oldof.setUserName(null == person ? "" : person.getName());
                    categoryRepository.save(oldof);
                    return oldof;
                } else {
                    return categoryRepository.save(category);
                }
            }

            Category newof = new Category();
            newof.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            newof.setMark(category.getMark());
            newof.setCreateDate(sdf.format(new Date()));
            newof.setModifyDate(sdf.format(new Date()));
            newof.setName(category.getName());
            newof.setUserId(person.getPersonId());
            newof.setUserName(person.getName());
            newof.setCategorySource(DataSourceEnums.SYSTEM_ADD.getValue());
            categoryRepository.save(newof);

            return newof;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Category> findByCategorySource(String categorySource) {
        return categoryRepository.findByCategorySource(categorySource);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public void initCategoryData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CategoryEnums> categoryEnumsList =
            EnumSet.allOf(CategoryEnums.class).stream().collect(Collectors.toList());
        String tenantId = Y9LoginUserHolder.getTenantId();
        for (CategoryEnums categoryEnums : categoryEnumsList) {
            LOGGER.info("categoryEnums: " + categoryEnums.getEnName() + " - " + categoryEnums.getCnName());
            Category newof = new Category();
            newof.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            newof.setMark(categoryEnums.getEnName());
            newof.setCreateDate(sdf.format(new Date()));
            newof.setModifyDate(sdf.format(new Date()));
            newof.setName(categoryEnums.getCnName());
            Manager manager = managerApiClient.getByLoginName(tenantId, "systemManager").getData();
            newof.setUserId(manager.getId());
            newof.setUserName(manager.getName());
            newof.setCategorySource(DataSourceEnums.SYSTEM_DEFAULT.getValue());
            categoryRepository.save(newof);
        }
    }
}
