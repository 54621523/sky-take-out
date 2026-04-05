package com.sky.user.service;


import com.sky.user.dto.AddressBookDTO;
import com.sky.user.vo.AddressBookVO;

import java.util.List;

public interface AddressBookService {


    void add(AddressBookDTO addressBookDTO);

    List<AddressBookVO> list();

    void setDefault(AddressBookDTO addressBookDTO);

    void update(AddressBookDTO addressBookDTO);

    AddressBookVO getDefault();

    AddressBookVO getById(Long id);

    void deleteById(Long id);
}
