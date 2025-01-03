package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectList;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;




    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 插入菜品
        dishMapper.insert(dish);

        // 设置 dishId 给口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(flavor -> flavor.setDishId(dish.getId()));

        // 批量插入菜品口味
        dishFlavorService.saveBatch(flavors);
    }


    //    @Override
//    public PageResult pageSelect(DishPageQueryDTO dishPageQueryDTO) {
//        String name = dishPageQueryDTO.getName();
//        Integer status = dishPageQueryDTO.getStatus();
//        Integer categoryId = dishPageQueryDTO.getCategoryId();
//
//
//        QueryWrapper<Dish> qw = new QueryWrapper<>();
//        if (name != null) {
//            qw.like("name", name);
//        }
//        if (status != null) {
//            qw.eq("status", status);
//        }
//        if (categoryId != null) {
//            qw.eq("categoryId", categoryId);
//        }
//
//
//        Page<Dish> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
//        dishMapper.selectPage(page, qw);
//
//        PageResult pageResult = new PageResult();
//        pageResult.setTotal(page.getTotal());
//        pageResult.setRecords(page.getRecords());
//
//        return pageResult;
//    }
    @Override
    public IPage<DishVO> dishPage(DishPageQueryDTO dishPageQueryDTO) {
        return dishMapper.selectPageVo(new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize()), new QueryWrapper<DishVO>()
                .like(StringUtils.isNotEmpty(dishPageQueryDTO.getName()), "d.name", dishPageQueryDTO.getName())
                .eq(dishPageQueryDTO.getCategoryId() != null, "d.category_id", dishPageQueryDTO.getCategoryId())
                .eq(dishPageQueryDTO.getStatus() != null, "d.status", dishPageQueryDTO.getStatus())
                .orderByAsc("d.create_time")
        );
    }

    @Override
    @Transactional
    public void del(List<Long> ids) {
        //查找是否在售卖
        QueryWrapper<Dish> qw = new QueryWrapper<>();
        qw.in("id", ids).eq("status",1);
        List<Dish> list = dishMapper.selectList(qw);
            if (!list.isEmpty() ) {
                throw new DeletionNotAllowedException("菜品正在售卖，不能删除");
            }


        //查找是否有关联套餐
        QueryWrapper<SetmealDish> qw2 = new QueryWrapper<>();
        qw2.in("dish_id", ids);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(qw2);
        if (!setmealDishes.isEmpty()) {
            throw new DeletionNotAllowedException("有菜品有关联套餐，不能删除");
        }

        //删除菜品和对应的口味
        dishMapper.deleteBatchIds(ids);
        QueryWrapper<DishFlavor> qw3 = new QueryWrapper<>();
        qw3.in("dish_id", ids);
        dishFlavorsMapper.delete(qw3);
    }

    @Override
    public DishVO selectId(Long id) {
        Dish dish = dishMapper.selectById(id);

        QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dish_id", id);
        List<DishFlavor> dishFlavors = dishFlavorsMapper.selectList(queryWrapper);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {
        //修改菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

//        dishMapper.updateById(dish);
//        //删除原本口味
//        dishFlavorsMapper.deleteById(dishDTO.getId());

        //修改菜品基本信息
        dishMapper.update(dish, new QueryWrapper<Dish>().eq("id", dishDTO.getId()));
        //删除原有得口味数据
        dishFlavorsMapper.delete(new QueryWrapper<DishFlavor>().eq("dish_id", dishDTO.getId()));
        //重新插入口味
//        // Step 3: 重新插入口味
//        List<DishFlavor> flavors = dishDTO.getFlavors().stream().map(flavorDTO -> {
//            DishFlavor flavor = new DishFlavor();
//            BeanUtils.copyProperties(flavorDTO, flavor); // 转换每个 flavorDTO 为 DishFlavor
//            flavor.setDishId(dish.getId()); // 绑定菜品 ID
//            return flavor;
//        }).collect(Collectors.toList());
//
//        // 批量插入口味
//        for (DishFlavor flavor : flavors) {
//            dishFlavorsMapper.insert(flavor); // 单条插入
//        }

        //新建DishFlavor的列表，遍历列表插入元素
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //遍历，为每一个口味插入dish的id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
        }
        //不知道为什么mapper里面的插入只有一个insert了，只能调用service
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public List<Dish> selectCategoryId(Long categoryId) {
        QueryWrapper<Dish> qw = new QueryWrapper<>();
        qw.eq("category_id",categoryId);
        List<Dish> list = dishMapper.selectList(qw);
        return list;
    }

    @Override
    public void status(Integer status, Long id) {
        // 更新菜品状态
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateById(dish);  // 直接使用updateById简化条件构造

        // 如果是停用状态
        if (status == StatusConstant.DISABLE) {
            // 获取所有包含该菜品的套餐ID
            List<Long> setmealIds = setmealDishMapper.selectList(
                            new LambdaQueryWrapper<SetmealDish>()
                                    .select(SetmealDish::getSetmealId)
                                    .eq(SetmealDish::getDishId, id)
                    ).stream()
                    .map(SetmealDish::getSetmealId)
                    .collect(Collectors.toList());

            // 如果有相关套餐，则更新它们的状态
            if (!setmealIds.isEmpty()) {
                setmealIds.forEach(setmealId -> {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.updateById(setmeal);  // 同样使用updateById简化条件构造
                });
            }
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */

    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
//            List<DishFlavor> flavors = dishFlavorsMapper.getByDishId(d.getId());
            QueryWrapper<DishFlavor> qw = new QueryWrapper<>();
            qw.eq("dish_id",d.getId());
            List<DishFlavor> flavors = dishFlavorsMapper.selectList(qw);

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
