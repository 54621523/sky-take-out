package com.sky.user.service;


import com.sky.user.dto.UserLoginDTO;
import com.sky.user.vo.UserLoginVO;

public interface UserService {

    UserLoginVO wxLogin(UserLoginDTO userLoginDTO);



}
