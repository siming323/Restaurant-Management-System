package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author siming323
 * @date 2023/11/27 9:04
 */
@Mapper
public interface OrderMapper {
    /**
     * 插入数据
     * @param orders
     * @return
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     * @return
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO 封装需要的数据
     * @return 返回Page对象
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getByOrderId(Long id);

    /**
     * 根据订单状态查询订单数量
     * @param toBeConfirmed
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据订单状态和订单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLess(Integer status, LocalDateTime orderTime);

    /**
     * 根据订单状态和订单时间查询订单,动态统计营业额
     * @param map ("status","begin","end")
     * @return double数据
     * @description 订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * 订单时间：开始时间，结束时间
     */
    Double sumByMap(Map map);

    /**
     *根据动态条件统计订单数量
     * @param map
     */
    Integer countByMap(Map map);

    /**
     * 查询商品销量排名
     * @param begin
     * @param end
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
