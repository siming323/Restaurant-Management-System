package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into employee (" +
            "name, username, password, phone, sex, " +
            "id_number, status, create_time, update_time, create_user, update_user) " +
            "values (" +
            "#{name},#{username},#{password},#{phone},#{sex}," +
            "#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser}" +
            ")"
    )
    void insert(Employee employee);

    /**
     * 员工分页查询
     * 这是动态SQL，在XML中进行配置
     * 这里使用了分页插件
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    Page<Employee> pageQueryWriteSQL(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据主键动态修改属性
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据id查询员工
     * @param id
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 根据id查询密码
     * @param id
     * @return
     */
    @Select("select password from employee where id = #{id}")
    String getOldPasswordFromDatabase(Long id);
}
