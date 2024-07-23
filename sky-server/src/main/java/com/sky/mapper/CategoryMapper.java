package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author siming323
 * @date 2023/10/7 11:31
 */
@Mapper
public interface CategoryMapper {
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
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除菜品分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void deleteCategoryById(Long id);

    /**
     * 新增菜品分类
     * @param category
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into " +
            "category(type, name, sort, status, create_time, update_time, create_user, update_user)" +
            " VALUES" +
            " (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 修改菜品分类信息
     * @param category
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);
}
