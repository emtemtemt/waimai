package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate; //注入redis
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result status(@PathVariable Integer status){

        ValueOperations value = redisTemplate.opsForValue(); //string类型
        value.set("status",status); //设置status

        return Result.success();
    }


    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result<Integer> get_status(  ){
        ValueOperations value = redisTemplate.opsForValue();
        Integer status1 = (Integer) value.get("status"); // 取status

        return Result.success(status1);
    }



}
