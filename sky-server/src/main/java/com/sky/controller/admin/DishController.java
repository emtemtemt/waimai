package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result add(@RequestBody DishDTO dishDTO){
        dishService.add(dishDTO);
        return Result.success();

    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
//    public Result<PageResult> pageSelect(DishPageQueryDTO dishPageQueryDTO){
//        PageResult result = dishService.pageSelect(dishPageQueryDTO);
//        return Result.success(result);
//    }
    public Result page(DishPageQueryDTO dishPageQueryDTO) {
        return Result.success(dishService.dishPage(dishPageQueryDTO));
    }

    @DeleteMapping
    @ApiOperation("删除菜品") //@RequestParam 在url中的?后面添加参数即可使用
    public Result del(@RequestParam List<Long> ids){
        dishService.del(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> selectID(@PathVariable Long id){
        DishVO dishVO = dishService.selectId(id);

        return Result.success(dishVO);

    }
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        dishService.updateDish(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> selectCategoryId (Long categoryId){
        List<Dish> list = dishService.selectCategoryId(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result status(@PathVariable  Integer status, Long id){
        dishService.status(status,id);
        return Result.success();
    }

}
