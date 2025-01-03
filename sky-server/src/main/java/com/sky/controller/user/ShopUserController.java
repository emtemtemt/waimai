package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
public class ShopUserController {
    @Autowired
    private RedisTemplate redisTemplate;
    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result<Integer> get_status(){
        ValueOperations value = redisTemplate.opsForValue();
        Integer status1 = (Integer) value.get("status"); // 取status

        return Result.success(status1);
    }



}
