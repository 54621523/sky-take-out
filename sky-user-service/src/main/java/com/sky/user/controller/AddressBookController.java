package com.sky.user.controller;


import com.sky.result.Result;
import com.sky.user.dto.AddressBookDTO;
import com.sky.user.service.AddressBookService;
import com.sky.user.vo.AddressBookVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "地址簿接口")
public class AddressBookController {


    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBookDTO
     * @return
     */
    @ApiOperation("新增地址")
    @PostMapping
    public Result add(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.add(addressBookDTO);
        return Result.success();
    }

    /**
     * 查询地址列表
     * @return
     */
    @ApiOperation("查询地址列表")
    @GetMapping("/list")
    public Result<List<AddressBookVO>> list(){
        List<AddressBookVO> list = addressBookService.list();
        return Result.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBookDTO
     * @return
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.setDefault(addressBookDTO);
        return Result.success();
    }

    /**
     * 修改地址
     * @param addressBookDTO
     * @return
     */
    @ApiOperation("修改地址")
    @PutMapping
    public Result update(@RequestBody AddressBookDTO addressBookDTO){
        addressBookService.update(addressBookDTO);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBookVO> getDefault(){
        AddressBookVO addressBookVO = addressBookService.getDefault();
        return Result.success(addressBookVO);
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBookVO> getById(@PathVariable Long id){
        AddressBookVO addressBookVO = addressBookService.getById(id);
        return Result.success(addressBookVO);
    }

    /**
     * 删除地址
     * @param id
     * @return
     */
    @ApiOperation("删除地址")
    @DeleteMapping
    public Result delete(@RequestParam Long id){
        addressBookService.deleteById(id);
        return Result.success();
    }
}
