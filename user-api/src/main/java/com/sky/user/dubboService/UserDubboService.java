package com.sky.user.dubboService;

import com.sky.user.vo.UserReportVO;

import java.time.LocalDate;

public interface UserDubboService {
    UserReportVO userStatistics(LocalDate begin, LocalDate end);
}
