package com.sky.product.dubboService;

import com.sky.exception.BaseException;
import com.sky.product.dto.CategoryDTO;
import com.sky.product.dto.CategoryPageQueryDTO;
import com.sky.product.vo.CategoryVO;
import com.sky.result.PageResult;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface CategoryDubboService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO) throws BaseException, DuplicateKeyException;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 根据类型查询分类列表
     * @param type
     * @return
     */
    List<CategoryVO> list(Integer type);

    /**
     * 根据id删除分类
     * @param id
     */
    void deleteById(Long id);

    /**
     * 启用或禁用分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
