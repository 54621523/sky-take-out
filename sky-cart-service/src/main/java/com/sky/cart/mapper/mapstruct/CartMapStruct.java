package com.sky.cart.mapper.mapstruct;

import com.sky.cart.domain.po.ShoppingCart;
import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.vo.ShoppingCartVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CartMapStruct {

    CartMapStruct INSTANCE = Mappers.getMapper(CartMapStruct.class);

    /**
     * DTO 转 PO（用于新增/修改购物车）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true)
    })
    ShoppingCart shoppingCartDto2Po(ShoppingCartDTO dto);

    /**
     * PO 转 VO（用于查询返回）
     */
    ShoppingCartVO shoppingCartPo2Vo(ShoppingCart po);

    List<ShoppingCartVO> shoppingCartPo2Vo(List<ShoppingCart> pos);
}
