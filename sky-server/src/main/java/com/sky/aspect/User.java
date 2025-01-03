package com.sky.aspect;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MY 自动填充字段
 * 这个类没用(示例)，在Employee类里修改
 */

//在实体类中，你需要使用 @TableField 注解来标记哪些字段需要自动填充，并指定填充的策略
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
