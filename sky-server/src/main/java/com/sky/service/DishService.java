package com.sky.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    void add(DishDTO dishDTO);

//    PageResult pageSelect(DishPageQueryDTO dishPageQueryDTO);

    IPage<DishVO> dishPage(DishPageQueryDTO dishPageQueryDTO);

    void del(List<Long> ids);

    DishVO selectId(Long id);

    void updateDish(DishDTO dishDTO);

    List<Dish> selectCategoryId(Long categoryId);

    void status(Integer status, Long id);
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}

