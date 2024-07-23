package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * @author siming323
 * @date 2023/11/25 9:24
 */
public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单分页查询
     * @param page 当前几页
     * @param pageSize 分页大小
     * @param status 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return 返回Result<PageResult>
     */
    PageResult pageQuery(int pageNum, int pageSize, Integer status);

    /**
     * 查询订单详情
     * @param id 订单id
     * @return 返回Result<OrderVO>
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 取消订单
     * @param id 订单id
     */
    void cancelOrderById(Long id);

    /**
     * 再来一单
     * @param id 原订单id
     * @return 返回Result
     */
    void repetition(Long id);

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO 订单分页查询对象
     * @return 返回分页结果
     */
    PageResult conditionSearchOrder(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单统计
     * @return 返回订单统计结果
     */
    OrderStatisticsVO statistics();

    /**
     * 确认订单
     * @param ordersConfirmDTO 订单确认对象
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO 拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO 取消订单对象
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 配送订单
     * @param id 订单id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id 订单id
     */
    void complete(Long id);

    /**
     * 客户催单
     * @param id
     */
    void reminder(Long id);
}
