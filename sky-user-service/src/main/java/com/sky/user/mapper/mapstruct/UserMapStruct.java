package com.sky.user.mapper.mapstruct;

import com.sky.user.domain.po.AddressBook;
import com.sky.user.dto.AddressBookDTO;
import com.sky.user.vo.AddressBookVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapStruct {

    UserMapStruct INSTANCE = Mappers.getMapper(UserMapStruct.class);

    // ========== AddressBook 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改地址）
     */
    @Mappings({
    })
    AddressBook addressBookDto2Po(AddressBookDTO dto);

    /**
     * PO 转 VO（用于查询返回）
     */
    AddressBookVO addressBookPo2Vo(AddressBook po);

    List<AddressBookVO> addressBookPo2Vo(List<AddressBook> pos);

    // ========== User 相关转换 ==========

}
