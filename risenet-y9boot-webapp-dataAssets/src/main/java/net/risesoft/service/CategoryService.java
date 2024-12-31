package net.risesoft.service;

import java.util.List;

import org.springframework.data.domain.Page;

import net.risesoft.entity.Category;

public interface CategoryService {

    Page<Category> pageAll(int page, int rows);

    Category findByMark(String mark);

    void remove(String id);

    Category saveOrUpdate(Category category);

    List<Category> findByCategorySource(String categorySource);

    List<Category> findAll();

    void initCategoryData();
}
