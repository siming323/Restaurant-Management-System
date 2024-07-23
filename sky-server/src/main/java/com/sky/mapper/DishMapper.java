package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author siming323
 * @date 2023/10/7 21:15
 */
@Mapper
public interface DishMapper {
    /**
     * 统计dish表中category_id的个数
     * @param id
     * @return
     */
    @Select("select count(id) from dish where category_id = #{id}")
    Integer countByCategoryId(Long id);

    /**
     * 新增菜品和对应的口味
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return Page<DishVO>
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品数据
     * @param id
     * @return Dish
     */
    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    /**
     * 根据主键id删除菜品数据
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据菜品id集合ids，批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新菜品表单
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据菜品id(categoryId)查询菜品数据
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> listDishByCategoryId(Long categoryId);

    /**
     * 动态条件查询菜品，只查询起售中的菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select dish.* from dish left join setmeal_dish on dish.id = setmeal_dish.dish_id where setmeal_dish.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
