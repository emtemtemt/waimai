package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.ThreadContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        QueryWrapper<Employee> qw = new QueryWrapper<>();
        qw.eq("username",username);
        Employee employee = employeeMapper.selectOne(qw);
        //1、根据用户名查询数据库中的数据

//        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee emp = new Employee(); //实例化所需的类
        //对象属性复制，不用一个一个的get set， 把 前端的对象 都复制给 新的实体类对象
        BeanUtils.copyProperties(employeeDTO,emp);
        // StatusConstant = 1 ,
        emp.setStatus(StatusConstant.ENABLE);
        // PasswordConstant.DEFAULT_PASSWORD = 123456
        emp.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8))); //加密
//        emp.setCreateTime(LocalDateTime.now());
//        emp.setUpdateTime(LocalDateTime.now());
//        //TODO
//        //设置用户ID
//        emp.setCreateUser(ThreadContext.getCurrentId());
//        emp.setUpdateUser(ThreadContext.getCurrentId());

        //调用sql
        employeeMapper.insert(emp);
        ThreadContext.removeCurrentId();
    }

    @Override
    public PageResult pageselect(EmployeePageQueryDTO employeePageQueryDTO) {
        QueryWrapper<Employee> eq = new QueryWrapper<>();
        if (employeePageQueryDTO.getName() != null) {
            eq.like("name", employeePageQueryDTO.getName());
        }
        int size = employeePageQueryDTO.getPageSize(); //每页条数
        int page1 = employeePageQueryDTO.getPage(); //页数

        Page<Employee> page = new Page<>(page1,size);
        employeeMapper.selectPage(page,eq);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getRecords());

        return pageResult;
    }

    @Override
    public void status(Long id, Integer status) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);

        employeeMapper.updateById(employee);

    }

    @Override
    public Employee selectid(Long id) {
        Employee employee = new Employee();
        employee = employeeMapper.selectById(id);
        return employee;
    }

    @Override
    public void update_employee(EmployeeDTO employeeDTO) {
        Employee emp = new Employee(); //实例化所需的类
        BeanUtils.copyProperties(employeeDTO,emp);

//        emp.setUpdateTime(LocalDateTime.now());
//        emp.setUpdateUser(ThreadContext.getCurrentId());
        employeeMapper.updateById(emp);
    }


}
