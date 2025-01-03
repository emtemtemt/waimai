package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorsServiceImpl extends ServiceImpl<DishFlavorsMapper, DishFlavor> implements DishFlavorService {
}
