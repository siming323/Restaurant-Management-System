package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

/**
 * @author siming323
 * @date 2023/10/7 11:22
 */
public interface CategoryService {
    /**
     * 根据类型查询分类，如没有传入类型，则返回全部(不分类)
     * @param type
     * @return
     */
    List<Category> list(Integer type);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除菜品分类
     * @param id
     */
    void deleteCategoryById(Long id);

    /**
     * 新增菜品分类
     * @param categoryDTO
     */
    void add(CategoryDTO categoryDTO);

    void startOrStop(Integer status, Long id);

    void update(CategoryDTO categoryDTO);
}
