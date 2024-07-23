package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.ResolveIdSelfGrowingFailureMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
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

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //操作数据库的时候，使用实体类封装用于对应数据库表单项
        Employee employee = new Employee();
        //属性拷贝
        //DTO和entity的字段名称必须完全一致
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置拷贝不上的字段:status,password,create_time,update_time,create_user,update_user
        employee.setStatus(StatusConstant.ENABLE);
        //默认密码设置为123456，数据库存储md5加密后的数据
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //公共字段CreateTime、UpdateTime、CreateUser、UpdateUser由切面类的通知进行填充
        /*
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 设置当前记录创建人的id和修改人的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        */
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.EMPLOYEE);
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit 0,10
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        //PageHelper插件的约定俗成：返回com.github.pagehelper.Page类型，传入封装的类型
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public PageResult pageQueryWriteSQL(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit 0,10
        Page<Employee> page = employeeMapper.pageQueryWriteSQL(employeePageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 启用/禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?
        //借助Employee实体类上的注解@Builder
        Employee employee = Employee.builder().status(status).id(id).build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("*****");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void updateInformation(EmployeeDTO employeeDTO) {
        //Employee employee = Employee.builder().updateTime(LocalDateTime.now()).updateUser(BaseContext.getCurrentId()).build();
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        //公共字段UpdateTime、UpdateUser由切面类的通知进行填充
        /*
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
         */
        employeeMapper.update(employee);
    }

    /**
     * 修改员工账号密码
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        passwordEditDTO.setId(BaseContext.getCurrentId());
        Long id = passwordEditDTO.getId();
        String oldPasswordFromRequest = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        String newPassword = passwordEditDTO.getNewPassword();
        String oldPasswordFromDatabase = employeeMapper.getOldPasswordFromDatabase(id);
        if (oldPasswordFromRequest.equals(oldPasswordFromDatabase)){
            employeeMapper.update(Employee.builder().id(id).password(DigestUtils.md5DigestAsHex(newPassword.getBytes())).build());
        }else {
            throw new PasswordErrorException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

    }

}
