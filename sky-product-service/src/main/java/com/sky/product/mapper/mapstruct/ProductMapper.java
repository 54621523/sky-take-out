package com.sky.product.mapper.mapstruct;

import com.sky.product.domain.po.*;
import com.sky.product.dto.*;
import com.sky.product.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    // ========== Dish 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改菜品）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    Dish dishDto2Po(DishDTO dishDTO);

    /**
     * PO 转 VO（用于查询返回）
     */
    @Mappings({
            @Mapping(target = "categoryName", ignore = true),
            @Mapping(target = "flavors", ignore = true)
    })
    DishVO dishPo2Vo(Dish dish);

    List<DishVO> dishPo2Vo(List<Dish> dishes);

    /**
     * DTO 转 PO（口味）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dishId", ignore = true)
    })
    DishFlavor dishFlavorDto2Po(DishFlavorDTO dto);

    List<DishFlavor> dishFlavorDto2Po(List<DishFlavorDTO> dtos);

    /**
     * PO 转 VO（口味）
     */
    DishFlavorVO dishFlavorPo2Vo(DishFlavor flavor);

    List<DishFlavorVO> dishFlavorPo2Vo(List<DishFlavor> flavors);

    // ========== Setmeal 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改套餐）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    Setmeal setmealDto2Po(SetmealDTO setmealDTO);

    /**
     * PO 转 VO（用于查询返回）
     */
    @Mappings({
            @Mapping(target = "categoryName", ignore = true),
            @Mapping(target = "setmealDishes", ignore = true)
    })
    SetmealVO setmealPo2Vo(Setmeal setmeal);

    List<SetmealVO> setmealPo2Vo(List<Setmeal> setmeals);

    /**
     * DTO 转 PO（套餐菜品关联）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "setmealId", ignore = true)
    })
    SetmealDish setmealDishDto2Po(SetmealDishDTO dto);

    List<SetmealDish> setmealDishDto2Po(List<SetmealDishDTO> dtos);

    /**
     * PO 转 VO（套餐菜品关联）
     */
    SetmealDishVO setmealDishPo2Vo(SetmealDish setmealDish);

    List<SetmealDishVO> setmealDishPo2Vo(List<SetmealDish> setmealDishes);

    // ========== Category 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改分类）
     */
    @Mappings({
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    Category categoryDto2Po(CategoryDTO categoryDTO);

    /**
     * PO 转 VO（用于查询返回）
     */
    CategoryVO categoryPo2Vo(Category category);

    List<CategoryVO> categoryPo2Vo(List<Category> categories);

    List<CategoryDTO> categoryPo2Dto(List<Category> categories);
}
