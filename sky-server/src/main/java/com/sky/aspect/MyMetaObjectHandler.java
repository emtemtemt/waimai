package com.sky.aspect;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.ThreadContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 创建一个类来实现 MetaObjectHandler 接口，并重写 insertFill 和 updateFill 方法

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        System.out.println("开始插入填充...");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", ThreadContext.getCurrentId());
        metaObject.setValue("updateUser", ThreadContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", ThreadContext.getCurrentId());
    }
}
