package com.sky.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private String username;

    private String name;

    @Size(min = 10,max = 13)
    private String phone;

    private String sex;

    @Size(min = 17,max = 20)
    private String idNumber;

}
