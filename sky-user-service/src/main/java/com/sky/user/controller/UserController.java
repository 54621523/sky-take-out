package com.sky.user.controller;


import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.user.dto.UserLoginDTO;
import com.sky.user.service.UserService;
import com.sky.user.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user/user")
@Api(tags = "登录相关接口")
public class UserController {



    @Autowired
    private UserService userService;

    @ApiOperation("微信登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        UserLoginVO userLoginVO = userService.wxLogin(userLoginDTO);
        return Result.success(userLoginVO);
    }

    @ApiOperation("用户退出登录")
    @PostMapping("/logout")
    public Result logout(){
        return Result.success();
    }
}
