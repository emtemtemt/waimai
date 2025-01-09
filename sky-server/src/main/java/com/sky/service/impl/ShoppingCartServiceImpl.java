package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.context.ThreadContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapping;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapping shoppingCartMapping;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(ThreadContext.getCurrentId());

        //判断商品是否存在
        QueryWrapper<ShoppingCart> qw = new QueryWrapper<>();
        if(shoppingCart.getUserId() != null){
        qw.eq("user_id",shoppingCart.getUserId());}
        if(shoppingCart.getSetmealId() != null){
        qw.eq("setmeal_id",shoppingCart.getSetmealId());}
        if(shoppingCart.getDishId() !=null ){
        qw.eq("dish_id",shoppingCart.getDishId());}
        if(shoppingCart.getDishFlavor() != null){
        qw.eq("dish_flavor",shoppingCart.getDishFlavor());}
        List<ShoppingCart> list = shoppingCartMapping.selectList(qw);

        //如果存在，直接加一
        if (list !=null && list.size()>0){
            //获取对应的对象
            ShoppingCart cart = list.get(0);
            //数据加一
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapping.updateById(cart);
        }else {
            //不存在
            //判断是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if(dishId !=null){
                //为菜品
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
//                shoppingCartMapping.updateById(dish);
            }else{
                //为套餐
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
            }
            //最终插入
            shoppingCartMapping.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        Long currentId = ThreadContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        QueryWrapper<ShoppingCart> qw = new QueryWrapper<>();
        qw.eq("user_id",currentId);

        List<ShoppingCart> list = shoppingCartMapping.selectList(qw);
        return list;
    }

    @Override
    public void clean() {
        Long currentId = ThreadContext.getCurrentId();
        QueryWrapper<ShoppingCart> qw = new QueryWrapper<>();
        qw.eq("user_id",currentId);
        shoppingCartMapping.delete(qw);

    }
}
