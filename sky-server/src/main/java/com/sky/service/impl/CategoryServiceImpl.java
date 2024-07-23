package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.IdNotExitsException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author siming323
 * @date 2023/10/7 11:22
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private QueryWhetherTheFieldExistsMapper queryWhetherTheFieldExistsMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    /**
     * 根据类型查询分类，如没有传入类型，则返回全部(不分类)
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 根据id删除菜品分类
     * @param id
     */
    @Override
    public void deleteCategoryById(Long id) {
        if (queryWhetherTheFieldExistsMapper.queryId(id) == 0){
            throw new IdNotExitsException(MessageConstant.ID_NOT_FOUND);
        }
        if (dishMapper.countByCategoryId(id) > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        if (setmealMapper.countByCategoryId(id) > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteCategoryById(id);
    }

    /**
     * 新增菜品分类
     * @param categoryDTO
     */
    @Override
    public void add(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.DISABLE);
        //公共字段CreateTime、UpdateTime、CreateUser、UpdateUser由切面类的通知进行填充
        /*
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        */
        try {
            categoryMapper.insert(category);
        }finally {
            resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.CATEGORY);
        }
    }

    /**
     * 启用/禁用菜品分类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //公共字段UpdateTime、UpdateUser由切面类的通知进行填充
        Category category = Category.builder().status(status).id(id).build();
        categoryMapper.update(category);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        //公共字段UpdateTime、UpdateUser由切面类的通知进行填充
        /*
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        */
        try {
            categoryMapper.update(category);
        }finally {
            resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.CATEGORY);
        }
    }
}
