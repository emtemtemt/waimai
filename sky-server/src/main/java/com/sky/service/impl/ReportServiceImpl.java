package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //存放全部日期的列表
        ArrayList<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }
        String join = StringUtils.join(localDates, ",");//拼接逗号
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(join);

        //查询每天的营业额
        ArrayList<Double> doubles = new ArrayList<>();
        for (LocalDate data : localDates) {
            LocalDateTime min = LocalDateTime.of(data, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(data, LocalTime.MAX);

            QueryWrapper<Orders> qw = new QueryWrapper<>();
            qw.eq("status", 5); // 根据状态查询
            qw.between("order_time",min,max);
            qw.select("SUM(amount) as totalAmount"); // 计算 amount 的总和

            // 使用 selectMaps 查询
            List<Map<String, Object>> result = orderMapper.selectMaps(qw);
            if (result != null && !result.isEmpty() && result.get(0) != null && result.get(0).get("totalAmount") != null) {
                BigDecimal totalAmount = (BigDecimal) result.get(0).get("totalAmount");
                doubles.add(totalAmount.doubleValue());
            } else {
                doubles.add(0.0); // 如果查询结果为空，默认添加 0.0
            }
        }
        //拼接
        String join1 = StringUtils.join(doubles, ",");
        turnoverReportVO.setTurnoverList(join1);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //存放全部日期的列表
        ArrayList<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }
        String data_list = StringUtils.join(localDates, ",");//拼接逗号

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(data_list);

        //存放每天新增用户列表
        ArrayList<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : localDates) {
            LocalDateTime min = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(date, LocalTime.MAX);
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.between("create_time",min,max);
            Long new_user = userMapper.selectCount(qw);
            newUserList.add(Math.toIntExact(new_user));
        }
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));

        //存放每天全部用户列表
        ArrayList<Integer> allUserList = new ArrayList<>();
        for (LocalDate date : localDates) {
            LocalDateTime min = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(date, LocalTime.MAX);
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.le("create_time",max);
            Long all_user = userMapper.selectCount(qw);
            allUserList.add(Math.toIntExact(all_user));
        }
        userReportVO.setTotalUserList(StringUtils.join(allUserList, ","));

        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //存放全部日期的列表
        ArrayList<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }
        String data_list = StringUtils.join(localDates, ",");//拼接逗号

        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(data_list);

        //查询每天的全部订单
        ArrayList<Integer> allOrdersList = new ArrayList<>();
        ArrayList<Integer> validOrdersList = new ArrayList<>();
        for (LocalDate date : localDates) {
            LocalDateTime min = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(date, LocalTime.MAX);

            QueryWrapper<Orders> qw = new QueryWrapper<>();
            qw.between("order_time",min,max);
            Long all_orders = orderMapper.selectCount(qw);
            allOrdersList.add(Math.toIntExact(all_orders));
            //查询每天的有效订单
            QueryWrapper<Orders> qw2 = new QueryWrapper<>();
            qw2.between("order_time",min,max);
            qw2.eq("status",5);
            Long valid_orders = orderMapper.selectCount(qw2);
            validOrdersList.add(Math.toIntExact(valid_orders));
        }
        orderReportVO.setOrderCountList(StringUtils.join(allOrdersList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrdersList, ","));

        //时间段内全部的订单数
        int sum1 = 0;
        for (Integer i : allOrdersList) {
            sum1 += i; // 累加全部订单数
        }
        orderReportVO.setTotalOrderCount(sum1);
        //时间段内全部的有效订单数
        int sum2 = 0;
        for (Integer i : validOrdersList) {
            sum2 += i; // 累加有效订单数
        }
        orderReportVO.setValidOrderCount(sum2);

        //订单完成率
        Double order_complete = 0.0;
        if(orderReportVO.getTotalOrderCount() != 0){
            order_complete = orderReportVO.getValidOrderCount().doubleValue() / orderReportVO.getTotalOrderCount();
        }
        orderReportVO.setOrderCompletionRate(order_complete);

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime min = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime max = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> top10 = orderMapper.getTop10(min, max);

        // 构建返回结果
        SalesTop10ReportVO reportVO = new SalesTop10ReportVO();

        List<String> names = new ArrayList<>();
        for (GoodsSalesDTO dto : top10) {
            names.add(dto.getName());
        }
        reportVO.setNameList(StringUtils.join(names, ","));

        List<Integer> numbers = new ArrayList<>();
        for (GoodsSalesDTO dto : top10) {
            numbers.add(dto.getNumber());
        }
        reportVO.setNumberList(StringUtils.join(numbers, ","));

        return reportVO;
    }
}
