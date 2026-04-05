package com.sky.user.mapper;


import com.sky.user.domain.po.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper {



    void insert(AddressBook addressBook);

    List<AddressBook> listByUserId(Long userId);

    void update(AddressBook addressBook);


    void setDefault(Long id);

    AddressBook getDefault(Long userId);

    void deleteDefault(Long userId);

    AddressBook getById(Long id);

    void deleteById(Long id);
}
