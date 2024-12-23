package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")  //swagger生成接口文档
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }


    @PostMapping
    @ApiOperation("新增员工")
    public Result update(@RequestBody @Validated EmployeeDTO employeeDTO) {
        employeeService.update(employeeDTO);
        return Result.success();
    }
//    EmployeePageQueryDTO
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page_select(EmployeePageQueryDTO employeePageQueryDTO){
        PageResult page = employeeService.pageselect(employeePageQueryDTO);
        return Result.success(page);
    }

    @PostMapping("/status/{status}") // 路径传参 @PathVariable 对应 {status}
    @ApiOperation("启用、禁用员工账号")
    public Result status(Long id,@PathVariable("status") Integer status){

        employeeService.status(id,status);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> select_id(@PathVariable("id") Long id){
        Employee result = employeeService.selectid(id);
        return Result.success(result);
    }

    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update_employee(@RequestBody EmployeeDTO employeeDTO ){
        employeeService.update_employee(employeeDTO);
        return Result.success();
    }

}
