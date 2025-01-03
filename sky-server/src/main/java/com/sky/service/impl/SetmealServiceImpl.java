package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private  SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Override
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //新增套餐
        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        //新增套餐菜品关系
        Long id = setmeal.getId();
        //遍历列表 设置id 插入值
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
            setmealDishMapper.insert(setmealDish);
        });

    }

    @Override
    public PageResult pageSelect(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int size = setmealPageQueryDTO.getPageSize();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        String name = setmealPageQueryDTO.getName();
        Integer status = setmealPageQueryDTO.getStatus();

        Page<Setmeal> setmealPage = new Page<>(page, size);

        QueryWrapper<Setmeal> qw = new QueryWrapper<>();
        if(categoryId != null){
            qw.eq("category_id",categoryId);
        }
        if(name != null){
            qw.like("name",name);
        }
        if(status != null){
            qw.eq("status",status);
        }
        Page<Setmeal> setmealPage1 = setmealMapper.selectPage(setmealPage, qw);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(setmealPage1.getTotal());
        pageResult.setRecords(setmealPage1.getRecords());
//        // 获取记录列表
//        List<Setmeal> records = setmealPage1.getRecords();
//        // 遍历记录列表并处理每个Setmeal对象
//        for (Setmeal setmeal : records) {
//            Long categoryId1 = setmeal.getCategoryId();
//            Category category = categoryMapper.selectById(categoryId1);
//            String categoryName = category.getName();
//        }
        return pageResult;
    }

    @Override
    public void del(List<Long> ids) {
        ids.forEach(id -> {
            //删除套餐
            setmealMapper.deleteById(id);
            //删除对应的关系
            QueryWrapper<SetmealDish> qw = new QueryWrapper<>();
            qw.eq("setmeal_id",id);
            setmealDishMapper.delete(qw);
        });
    }

    @Override
    public SetmealVO selectId(Long id) {

        Setmeal setmeal1 = setmealMapper.selectById(id);
        //根据setmeal_id查询setmealDish列表
        QueryWrapper<SetmealDish> qw = new QueryWrapper<>();
        qw.eq("setmeal_id",id);
        List<SetmealDish> dishList = setmealDishMapper.selectList(qw);
        //获取CategoryId 去查CategoryName
        Long categoryId = setmeal1.getCategoryId();
        Category category = categoryMapper.selectById(categoryId);
        String categoryName = category.getName();

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal1,setmealVO);
        setmealVO.setSetmealDishes(dishList);
        setmealVO.setCategoryName(categoryName);
        return setmealVO;
    }

    @Override
    public SetmealVO update(SetmealDTO setmealDTO) {
        //修改套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateById(setmeal);
        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        Long id = setmeal.getId();
        QueryWrapper<SetmealDish> qw = new QueryWrapper<>();
        qw.eq("setmeal_id",id);
        setmealDishMapper.delete(qw);
        //重新添加套餐和菜品的关联关系
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
            setmealDishMapper.insert(setmealDish);
        });

        return null;
    }

    @Override
    public void status(Integer status, Long id) {
//        //修改status
//        Setmeal setmeal = new Setmeal();
//        setmeal.setStatus(status);
//        setmeal.setId(id);
//        setmealMapper.updateById(setmeal);
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);

    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


}
