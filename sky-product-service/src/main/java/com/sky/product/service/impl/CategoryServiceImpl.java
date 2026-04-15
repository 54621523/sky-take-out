package com.sky.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.product.domain.po.Category;
import com.sky.product.domain.po.Dish;
import com.sky.product.domain.po.Setmeal;
import com.sky.product.dto.CategoryDTO;
import com.sky.product.dto.CategoryPageQueryDTO;
import com.sky.product.dubboService.CategoryDubboService;
import com.sky.product.mapper.CategoryMapper;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.mapstruct.ProductMapper;
import com.sky.product.mapper.SetmealMapper;
import com.sky.product.service.CategoryService;
import com.sky.product.vo.CategoryVO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务层
 */
@DubboService(interfaceClass = CategoryDubboService.class)
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService, CategoryDubboService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(CategoryDTO categoryDTO) {

        Category category = ProductMapper.INSTANCE.categoryDto2Po(categoryDTO);

        category.setStatus(StatusConstant.DISABLE);
        try {
            categoryMapper.insert(category);
        }catch ( DuplicateKeyException e){
            log.warn("分类名称已存在：{}", categoryDTO.getName());
            throw new BaseException(MessageConstant.ALREADY_EXISTS);
        }
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        List<CategoryVO> records = ProductMapper.INSTANCE.categoryPo2Vo(page.getResult());
        return new PageResult(page.getTotal(), records);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {

        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId,id));
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId,id));
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(CategoryDTO categoryDTO) {

        Category category = ProductMapper.INSTANCE.categoryDto2Po(categoryDTO);

        categoryMapper.updateById(category);
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.updateById(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    public List<CategoryVO> list(Integer type) {
        if(type == null){
            List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                    .eq(Category::getStatus,StatusConstant.ENABLE));
            return ProductMapper.INSTANCE.categoryPo2Vo(categories);
        }
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getType,type));
        return ProductMapper.INSTANCE.categoryPo2Vo(categories);
    }
}
