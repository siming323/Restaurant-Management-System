package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/21 20:39
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> dynamicConditionalQueryShoppingCart(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * update shopping_cart set number = ? where id = ?
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert(
            "insert into shopping_cart (" +
            "name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time" +
            ") " +
            "VALUES (" +
            "#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime}" +
            ")"
    )
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteShoppingCartByUserId(Long userId);

    /**
     * 删除单个商品
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteShoppingCartById(Long id);

    /**
     * 批量插入购物车
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
