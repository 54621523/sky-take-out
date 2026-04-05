package com.sky.user.mapper;


import com.sky.user.domain.po.User;
import com.sky.user.vo.UserReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper {


    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    void insert(User user);

    User getByUserId(Long userId);

    UserReportVO userStatistics(LocalDate beginTime, LocalDate endTime);
}
