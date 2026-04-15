package com.sky.user.service.impl;


import com.sky.context.BaseContext;
import com.sky.user.domain.po.AddressBook;
import com.sky.user.dto.AddressBookDTO;
import com.sky.user.dubboService.AddressBookDubboService;
import com.sky.user.mapper.AddressBookMapper;
import com.sky.user.mapper.mapstruct.UserMapStruct;
import com.sky.user.service.AddressBookService;
import com.sky.user.vo.AddressBookVO;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DubboService(interfaceClass = AddressBookDubboService.class)
@Service
public class AddressBookServiceImpl implements AddressBookService,AddressBookDubboService {


    @Autowired
    private AddressBookMapper addressBookMapper;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddressBookDTO addressBookDTO) {
        AddressBook addressBook = UserMapStruct.INSTANCE.addressBookDto2Po(addressBookDTO);
        addressBook.setUserId(BaseContext.getCurrentId());
        if(addressBook.getIsDefault() == null) addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBookVO> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.listByUserId(userId);
        return UserMapStruct.INSTANCE.addressBookPo2Vo(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(AddressBookDTO addressBookDTO) {
        //先删除所有的默认地址,然后设置新的默认地址
        addressBookMapper.deleteDefault(BaseContext.getCurrentId());
        addressBookMapper.setDefault(addressBookDTO.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AddressBookDTO addressBookDTO) {
        addressBookDTO.setUserId(BaseContext.getCurrentId());
        AddressBook addressBook = UserMapStruct.INSTANCE.addressBookDto2Po(addressBookDTO) ;
        addressBookMapper.update(addressBook);
    }


    @Override
    public AddressBookVO getDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getDefault(userId);
        return UserMapStruct.INSTANCE.addressBookPo2Vo(addressBook);
    }

    @Override
    public AddressBookVO getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return UserMapStruct.INSTANCE.addressBookPo2Vo(addressBook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
