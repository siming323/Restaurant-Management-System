package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author siming323
 * @date 2023/11/25 9:24
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String baiduAk;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //TODO 需要仔细琢磨业务逻辑过程,别忘记开启事务、重置主键索引
        //处理业务异常(地址簿或购物车为空)
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getAddressById(ordersSubmitDTO.getAddressBookId());
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.dynamicConditionalQueryShoppingCart(ShoppingCart.builder().id(userId).build());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //TODO 检查用户的收获地址是否超出配送范围
        //checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //向订单表插入一条数据
        Orders orders = Orders.builder()
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .status(Orders.PENDING_PAYMENT)
                .number(String.valueOf(System.currentTimeMillis()))
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .userId(userId)
                .build();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //插入数据，返回订单id
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.ORDERS);
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = OrderDetail.builder().orderId(orders.getId()).build();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetailList.add(orderDetail);
        }
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.ORDER_DETAIL);
        orderDetailMapper.insertBatch(orderDetailList);
        //下单成果需要清空购物车
        shoppingCartMapper.deleteShoppingCartByUserId(userId);
        //封装VO返回结果
        return OrderSubmitVO.builder().id(orders.getId()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).orderTime(orders.getOrderTime()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
/*        JSONObject jsonObject = weChatPayUtil.pay(
                //商户订单号
                ordersPaymentDTO.getOrderNumber(),
                //支付金额，单位 元
                new BigDecimal("0.01"),
                //商品描述
                "苍穹外卖订单",
                //微信用户的openid
                user.getOpenid()
        );
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/
        JSONObject jsonObject = new JSONObject();
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomStringUtils.randomNumeric(32);

        jsonObject.put("timeStamp", timeStamp);
        jsonObject.put("nonceStr", nonceStr);
        jsonObject.put("signType", "RSA");
        jsonObject.put("out_trade_no", ordersPaymentDTO.getOrderNumber());
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        //通过websocket向客户端浏览器推送消息:type orderId content‘
        Map map = new HashMap();
        //type 1表示来单提醒,2表示客户催单
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号:" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 历史订单分页查询
     *
     * @param page     当前几页
     * @param pageSize 分页大小
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return 返回Result<PageResult>
     */
    @Override
    public PageResult pageQuery(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                // 订单id
                Long orderId = orders.getId();
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
            return new PageResult(page.getTotal(), list);
        }
        return new PageResult();
    }

    /**
     * 查询订单详情
     *
     * @param id 订单id
     * @return 返回Result<OrderVO>
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders order = orderMapper.getByOrderId(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     * 订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     *
     * @param id 订单id
     */
    @Override
    public void cancelOrderById(Long id) {
        //取消订单的接口，说明订单状态码一定是1-5
        Orders order = orderMapper.getByOrderId(id);
        Integer orderStatus = order.getStatus();
        if (orderStatus.equals(Orders.PENDING_PAYMENT) || orderStatus.equals(Orders.TO_BE_CONFIRMED)) {
            //待接单的订单需要需要退款，待付款的直接取消
            if (orderStatus.equals(Orders.TO_BE_CONFIRMED)) {
                //TODO 调用微信退款接口,这里不做实现
                order.setPayStatus(Orders.REFUND);
            }
            order.setStatus(Orders.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("用户主动取消订单");
            orderMapper.update(order);
        } else {
            //已接单/派送中/已完成 需要联系商家
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    /**
     * 再来一单
     *
     * @param id 原订单id
     * @return 返回Result
     */
    @Override
    public void repetition(Long id) {
        //"再来一单"就是根据现有订单再构造一个购物车
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        //两种方法实现：1.循环遍历，2.stream流
        /*List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = ShoppingCart.builder().name(orderDetail.getName()).image(orderDetail.getImage()).userId(userID).dishId(orderDetail.getDishId()).setmealId(orderDetail.getSetmealId()).dishFlavor(orderDetail.getDishFlavor()).number(orderDetail.getNumber()).amount(orderDetail.getAmount()).createTime(LocalDateTime.now()).build();
            shoppingCartList.add(shoppingCart);
        }*/
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SHOPPING_CART);
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 条件查询历史订单
     *
     * @param ordersPageQueryDTO 订单分页查询对象
     * @return 返回Result<PageResult>
     */
    @Override
    public PageResult conditionSearchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //下面封装OrderVO对象,填补Orders缺的信息字段
        List<OrderVO> orderVOList = new ArrayList<>();
        //获取page的结果集并进行遍历,封装OrderVO对象,并将OrderDetailList填充到OrderVO对象中
        page.getResult().forEach(orders -> {
            OrderVO orderVO = new OrderVO();
            AddressBook addressBook = addressBookMapper.getAddressById(orders.getAddressBookId());
            BeanUtils.copyProperties(orders, orderVO);
            //这里的参数是一个字符串,需要拆分
            orderVO.setOrderDishes(orderDetailMapper.getByOrderId(orders.getId()).stream().map(
                    orderDetail -> (orderDetail.getName() + " ⭐ " + orderDetail.getNumber())
            ).collect(Collectors.joining("")));
            orderVO.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail() + "");
            orderVOList.add(orderVO);
        });
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 订单统计
     *
     * @return 返回OrderStatisticsVO对象
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer completed = orderMapper.countByStatus(Orders.COMPLETED);
        Integer cancelled = orderMapper.countByStatus(Orders.CANCELLED);
        return OrderStatisticsVO.builder().toBeConfirmed(toBeConfirmed).confirmed(confirmed).deliveryInProgress(deliveryInProgress).completed(completed).cancelled(cancelled).build();
    }

    /**
     * 确认订单
     *
     * @param ordersConfirmDTO 订单确认对象
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        if (ordersConfirmDTO != null) {
            orderMapper.update(Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build());
        } else {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO 拒单
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersRejectionDTO == null || ordersRejectionDTO.getId() == null || ordersRejectionDTO.getRejectionReason() == null) {
            throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
        }
        Orders orders = orderMapper.getByOrderId(ordersRejectionDTO.getId());
        if (!orders.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NO_PAID);
        }
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(orders.getId()).status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .payStatus(Orders.REFUND).cancelTime(LocalDateTime.now()).build()
        );
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        if (ordersCancelDTO == null || ordersCancelDTO.getId() == null || ordersCancelDTO.getCancelReason() == null) {
            throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
        }
        Orders orders = orderMapper.getByOrderId(ordersCancelDTO.getId());
        Orders ordersCancel = orders.getPayStatus().equals(Orders.PAID) ?
                Orders.builder().id(orders.getId()).status(Orders.CANCELLED)
                        .cancelReason(ordersCancelDTO.getCancelReason())
                        .payStatus(Orders.REFUND)
                        .cancelTime(LocalDateTime.now()).build() :
                Orders.builder().id(orders.getId()).status(Orders.CANCELLED)
                        .cancelReason(ordersCancelDTO.getCancelReason())
                        .cancelTime(LocalDateTime.now()).build();
        orderMapper.update(ordersCancel);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build());
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(id).status(Orders.COMPLETED).deliveryTime(LocalDateTime.now()).build());
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:"+orders.getNumber());
        //使用websocket发送消息
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address 收货地址
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap<String, String>(16);
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", baiduAk);
        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address", address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
