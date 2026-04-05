package com.sky.admin.operation.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.admin.dto.EmployeeDTO;
import com.sky.admin.dto.EmployeeLoginDTO;
import com.sky.admin.dto.EmployeePageQueryDTO;
import com.sky.admin.dto.PasswordEditDTO;
import com.sky.admin.operation.domain.po.Employee;
import com.sky.admin.vo.EmployeeLoginVO;
import com.sky.result.PageResult;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employee
     */
    void saveEmployee(EmployeeDTO employee);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
