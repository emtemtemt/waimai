package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatProperties weChatProperties;

    public static final String WX_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        //发送请求获取 openid
        HashMap<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_URL, map);
        //解析传回来的json数据
        JSONObject jsonObject = JSON.parseObject(json);
        //获取openid
        String openid = jsonObject.getString("openid");


        if (openid==null){
            throw new LoginFailedException("登录失败");
        }

        //查询判断是否有openid （是否是新用户）
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("openid",openid);
        User user = userMapper.selectOne(qw);

        //为新用户 实现注册
        if(user == null){
            User user1 = new User();
            user1.setOpenid(openid);
            user1.setCreateTime(LocalDateTime.now());
            userMapper.insert(user1);
        }

        return user;
    }
}
