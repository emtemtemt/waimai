package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.ThreadContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapping shoppingCartMapping;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常（地址为空，购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException("地址为空,不能下单");
        }
        Long currentId = ThreadContext.getCurrentId();
        QueryWrapper<ShoppingCart> qw = new QueryWrapper<>();
        qw.eq("user_id",currentId);
        List<ShoppingCart> list1 = shoppingCartMapping.selectList(qw);
        if(list1 == null || list1.isEmpty()){
            throw new ShoppingCartBusinessException("购物车为空,不能下单");
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(0);
        orders.setStatus(1);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);
        orderMapper.insert(orders);

        //向订单详细表插入多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list1) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailService.saveBatch(orderDetails);

        //删除购物车数据
        shoppingCartMapping.delete(qw);

        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
//        Long userId = ThreadContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
////        JSONObject jsonObject = weChatPayUtil.pay(
////                ordersPaymentDTO.getOrderNumber(), //商户订单号
////                new BigDecimal(0.01), //支付金额，单位 元
////                "苍穹外卖订单", //商品描述
////                user.getOpenid() //微信用户的openid
////        );
//        JSONObject jsonObject = new JSONObject();
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;


        paySuccess(ordersPaymentDTO.getOrderNumber());



        String orderNumber = ordersPaymentDTO.getOrderNumber(); //订单号

        Long orderid = orderMapper.getorderId(orderNumber);//根据订单号查主键



        JSONObject jsonObject = new JSONObject();//本来没有2

        jsonObject.put("code", "ORDERPAID"); //本来没有3

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);

        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改

        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付

        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新

        LocalDateTime check_out_time = LocalDateTime.now();



        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderid);

        return vo;  //  修改支付方法中的代码
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
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
    }

    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status) {

        Long user_id = ThreadContext.getCurrentId();
        Page<Orders> page1 = new Page<>(page,pageSize);
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        if (status != null){
            qw.eq("status",status);
        }
        qw.eq("user_id",user_id);
        Page<Orders> ordersPage = orderMapper.selectPage(page1, qw);

        List<OrderVO> list = new ArrayList<>();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (ordersPage != null && ordersPage.getTotal() > 0) {
            for (Orders orders : ordersPage.getRecords()) {
                Long orderId = orders.getId(); // 订单id

                // 查询订单明细
                QueryWrapper<OrderDetail> detailQueryWrapper = new QueryWrapper<>();
                detailQueryWrapper.eq("order_id", orderId);
                List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }


        return new PageResult(ordersPage.getTotal(), list);
    }

    @Override
    public OrderVO details(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.selectById(id);

        // 查询该订单对应的菜品/套餐明细
        QueryWrapper<OrderDetail> qw = new QueryWrapper<>();
        qw.eq("order_id",orders.getId());
        List<OrderDetail> detailList = orderDetailMapper.selectList(qw);
        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(detailList);

        return orderVO;
    }

    @Override
    public void cancel(Long id) {
        //根据id判断是否存在该订单
        Orders orders = orderMapper.selectById(id);
        if(orders == null){
            throw new OrderBusinessException("该订单不存在");
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException("订单状态错误");
        }
        //
        if (orders.getStatus().equals(2)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        //设置新的订单数据
        Orders orders1 = new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelReason("用户取消");
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders1);
    }

    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long user_id = ThreadContext.getCurrentId();
        // 根据订单id查询当前订单详情
        QueryWrapper<OrderDetail> qw = new QueryWrapper<>();
        qw.eq("order_id",id);
        List<OrderDetail> detailList = orderDetailMapper.selectList(qw);

        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail detail : detailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            // 将 OrderDetail 的属性复制到 ShoppingCart 中（忽略 id 字段）
            BeanUtils.copyProperties(detail, shoppingCart, "id");
            // 设置购物车的用户 ID
            shoppingCart.setUserId(user_id);
            // 设置购物车的创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            // 将购物车对象添加到列表中
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapping.insertBatch(shoppingCartList);
        // 将购物车对象批量添加到数据库

    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        int page = ordersPageQueryDTO.getPage();
        int size = ordersPageQueryDTO.getPageSize();
        Page<Orders> page1 = new Page<>(page,size);
        //判断条件
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        if(ordersPageQueryDTO.getStatus() != null){
            qw.eq("status",ordersPageQueryDTO.getStatus());
        }
        if (ordersPageQueryDTO.getUserId() != null) {
            qw.eq("user_id", ordersPageQueryDTO.getUserId());
        }
        if (ordersPageQueryDTO.getPhone() != null) {
            qw.like("phone", ordersPageQueryDTO.getPhone());
        }
        if (ordersPageQueryDTO.getNumber() != null) {
            qw.like("number", ordersPageQueryDTO.getNumber());
        }
        if (ordersPageQueryDTO.getBeginTime() != null && ordersPageQueryDTO.getEndTime() != null) {
            // 如果同时提供了开始时间和结束时间，使用 between 进行范围查询
            qw.between("order_time", ordersPageQueryDTO.getBeginTime(), ordersPageQueryDTO.getEndTime());
        } else {
            // 如果只提供了开始时间，查询大于等于开始时间的记录
            if (ordersPageQueryDTO.getBeginTime() != null) {
                qw.ge("order_time", ordersPageQueryDTO.getBeginTime());
            }
            // 如果只提供了结束时间，查询小于等于结束时间的记录
            if (ordersPageQueryDTO.getEndTime() != null) {
                qw.le("order_time", ordersPageQueryDTO.getEndTime());
            }
        }


        Page<Orders> ordersPage = orderMapper.selectPage(page1, qw);
        // 将 Orders 转换为 OrderVO
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : ordersPage.getRecords()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(getOrderDishesStr(orders));
            orderVOList.add(orderVO);
        }

        // 返回分页结果
        return new PageResult(ordersPage.getTotal(), orderVOList);
        
    }

    @Override
    public OrderStatisticsVO statistics() {
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.eq("status",2);
        Long toBeConfirmed = orderMapper.selectCount(qw);

        QueryWrapper<Orders> qw2 = new QueryWrapper<>();
        qw2.eq("status",3);
        Long confirmed = orderMapper.selectCount(qw2);

        QueryWrapper<Orders> qw3 = new QueryWrapper<>();
        qw3.eq("status",4);
        Long deliveryInProgress = orderMapper.selectCount(qw3);


        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(Math.toIntExact(toBeConfirmed));
        orderStatisticsVO.setConfirmed(Math.toIntExact(confirmed));
        orderStatisticsVO.setDeliveryInProgress(Math.toIntExact(deliveryInProgress));
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long order_id = ordersConfirmDTO.getId();
        Integer status = ordersConfirmDTO.getStatus();
        Orders orders = new Orders();
        orders.setId(order_id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.updateById(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 根据id查询订单
        Long order_id = ordersRejectionDTO.getId();
        Orders orders = orderMapper.selectById(order_id);
        // 订单只有存在且状态为2（待接单）才可以拒单
        if(orders == null || !orders.getStatus().equals(2)) {
            throw new OrderBusinessException("订单状态错误");
        }
        //支付状态
        Integer payStatus = orders.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    orders.getNumber(),
//                    orders.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            log.info("申请退款：{0.01}");
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders1 = new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders1);
    }

    @Override
    public void order_cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.selectById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            log.info("申请退款：{0.02}");
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(6);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.selectById(id);
        if(orders == null && !orders.getStatus().equals(3) ){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setId(orders.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.selectById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    /**
     * 根据订单获取菜品信息字符串
     *
     * @param orders 订单
     * @return 菜品信息字符串
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orders.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);

        // 将菜品信息拼接为字符串
        StringBuilder orderDishesStr = new StringBuilder();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDishesStr.append(orderDetail.getName())
                    .append("*")
                    .append(orderDetail.getNumber())
                    .append(";");
        }
        return orderDishesStr.toString();
    }
}