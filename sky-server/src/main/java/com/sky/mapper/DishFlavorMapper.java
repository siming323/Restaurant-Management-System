package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author siming323
 * @date 2023/10/11 23:26
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 菜品口味批量插入
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dish id删除菜品口味表的数据
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id集合批量删除关联的口味数据
     * @param ids
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id查询对应的口味数据
     * @param dishId
     * @return List<DishFlavor>
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
