package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/8 23:19
 */
@RestController(value = "userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "C端-分类接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询菜品分类接口
     * 调用之前写好的service和mapper
     * @param type 菜品分类类型：1->菜品分类；2->套餐分类
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询菜品分类接口")
    public Result<List<Category>> list(Integer type){
        log.info("用户端查询菜品分类");
        return Result.success(categoryService.list(type));
    }
}
