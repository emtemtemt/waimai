package com.sky.task;

import com.alibaba.druid.sql.visitor.functions.Now;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    //处理15分钟超时订单
    @Scheduled(cron = "0 * * * * ?") //每分钟触发一次
    public void OrderTimeOut(){
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.eq("status",1);
        LocalDateTime now = LocalDateTime.now().plusMinutes(-15); // 15 分钟前的时间
        qw.le("order_time",now); //查出超出15分钟的订单
        List<Orders> list_order = orderMapper.selectList(qw);
        if(list_order!=null){
            for (Orders orders : list_order) {
                orders.setStatus(6);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(orders);
            }
        }
    }

    //处理一直处于派送中的订单
    @Scheduled(cron = "0 0 1 * * ?") //凌晨一点触发一次
//    @Scheduled(cron = "*/5 * * * * ?")
    public void sendOrder(){
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.eq("status",4);
        LocalDateTime now = LocalDateTime.now().plusMinutes(-65); // 15 分钟前的时间
        qw.le("order_time",now); //查出超出15分钟的订单
        List<Orders> list_order = orderMapper.selectList(qw);
        if(list_order!=null){
            for (Orders orders : list_order) {
                orders.setStatus(5);
                orderMapper.updateById(orders);
            }
        }

    }

}
