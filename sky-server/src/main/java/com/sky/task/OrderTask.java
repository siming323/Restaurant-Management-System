package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author siming323
 * @date 2023/12/27 11:41
 */
//@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时任务,每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时任务,每分钟触发一次,当前时间:{}",LocalDateTime.now());
        //查询超时订单
        //select * from orders where status = #{status} and order_time < #{orderTime}
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLess(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if (ordersList!= null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                //修改订单状态
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("超时未支付");
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveredOrder() {
        log.info("定时任务,处理一直处于派送中的订单:{}",LocalDateTime.now());
        //查询一直处于派送中的订单
        //select * from orders where status = #{status} and order_time < #{orderTime}
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLess(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (ordersList!= null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                //修改订单状态
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
