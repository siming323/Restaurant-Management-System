package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类管理
 *
 * @author siming323
 * @date 2023/10/7 11:03
 * @Slf4j 日志输出
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "菜品相关Controller接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据类型查询分类，如没有传入类型，则返回全部(不分类)
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品类型查询分类")
    public Result<List<Category>> list(Integer type) {
        log.info("根据菜品类型查询分类");
        log.info("根据菜品类型查询分类->type:{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

    /**
     * 菜品分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类相关接口->categoryPageQueryDTO:{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id删除菜品分类
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除菜品分类")
    public Result deleteCategoryById(Long id) {
        log.info("根据id删除菜品分类");
        categoryService.deleteCategoryById(id);
        return Result.success();
    }

    /**
     * 新增菜品分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增菜品分类");
        categoryService.add(categoryDTO);
        return Result.success();
    }

    /**
     * 启用/禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用分类")
    public Result status(@PathVariable Integer status, Long id) {
        log.info("启用/禁用分类");
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改菜品分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }
}
