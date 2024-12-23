package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void update(EmployeeDTO employeeDTO);

    PageResult pageselect(EmployeePageQueryDTO employeePageQueryDTO);


    void status(Long id, Integer status);

    Employee selectid(Long id);

    void update_employee(EmployeeDTO employeeDTO);
}
