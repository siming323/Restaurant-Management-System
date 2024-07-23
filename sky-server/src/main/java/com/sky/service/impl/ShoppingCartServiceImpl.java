package com.sky.service.impl;

import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ResolveIdSelfGrowingFailureMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author siming323
 * @date 2023/11/21 11:13
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //构建ShoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.dynamicConditionalQueryShoppingCart(shoppingCart);
        //判断当前加入到购物车中的商品是否已经存在了
        if (shoppingCartList != null && shoppingCartList.size() > 0){
            //上面的动态条件查询永远只会查出一条数据
            ShoppingCart cart = shoppingCartList.get(0);
            //如果已经存在了，只需要将数量加一
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            //TODO 这里一定要放在else下面，不然会出现点两份的时候，原来的加一，之后继续执行添加
            //如果不存在，需要插入一条购物车数据
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId != null){
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                //菜品id为空,则传过来的是套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SHOPPING_CART);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.dynamicConditionalQueryShoppingCart(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        //只能删除当前用户的购物车
        shoppingCartMapper.deleteShoppingCartByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void deleteOneCommodity(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.dynamicConditionalQueryShoppingCart(shoppingCart);
        if (shoppingCarts != null && shoppingCarts.size() > 0){
            shoppingCart = shoppingCarts.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1){
                shoppingCartMapper.deleteShoppingCartById(shoppingCart.getId());
            }else {
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}
