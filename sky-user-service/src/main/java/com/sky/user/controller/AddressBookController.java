package com.sky.user.controller;


import com.sky.result.Result;
import com.sky.user.dto.AddressBookDTO;
import com.sky.user.service.AddressBookService;
import com.sky.user.vo.AddressBookVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "地址簿接口")
public class AddressBookController {


    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public Result add(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.add(addressBookDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<AddressBookVO>> list(){
        List<AddressBookVO> list = addressBookService.list();
        return Result.success(list);
    }

    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.setDefault(addressBookDTO);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.update(addressBookDTO);
        return Result.success();
    }

    @GetMapping("/default")
    public Result<AddressBookVO> getDefault(){
        AddressBookVO addressBookVO = addressBookService.getDefault();
        return Result.success(addressBookVO);
    }

    @GetMapping("/{id}")
    public Result<AddressBookVO> getById(@PathVariable Long id){
        AddressBookVO addressBookVO = addressBookService.getById(id);
        return Result.success(addressBookVO);
    }

    @DeleteMapping
    public Result delete(@RequestParam Long id){
        addressBookService.deleteById(id);
        return Result.success();
    }
}
