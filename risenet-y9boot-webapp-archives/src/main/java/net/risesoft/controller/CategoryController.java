package net.risesoft.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Category;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CategoryService;

/**
 * 门类管理控制器
 *
 * @author yihong
 * @date 2024-10-26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/category", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取门类管理列表
     *
     * @param page 页码
     * @param rows 条数
     * @return
     */
    @GetMapping(value = "/getCategoryList")
    public Y9Page<Category> getCategoryList(@RequestParam Integer page, @RequestParam Integer rows) {
        Page<Category> pageList = categoryService.pageAll(page, rows);
        List<Category> list = pageList.getContent();
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), list, "获取列表成功");
    }

    @GetMapping(value = "/getAllCategory")
    public Y9Result<List<Category>> getAllCategory() {
        List<Category> list = categoryService.findAll();
        return Y9Result.success(list, "获取列表成功");
    }

    /**
     * 移除
     *
     * @param id 视图类型id
     * @return
     */
    @PostMapping(value = "/remove")
    public Y9Result<String> remove(String id) {
        categoryService.remove(id);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 保存
     *
     * @param category 视图类型
     * @return
     */
    @PostMapping(value = "/saveOrUpdate")
    public Y9Result<String> saveOrUpdate(Category category) {
        String id = category.getId();
        if (StringUtils.isEmpty(id)) {
            Category oldCategory = categoryService.findByMark(category.getMark());
            if (null != oldCategory) {
                return Y9Result.failure("保存失败：唯一标示【" + category.getMark() + "】已存在");
            }
        }
        categoryService.saveOrUpdate(category);
        return Y9Result.successMsg("保存成功");
    }
}