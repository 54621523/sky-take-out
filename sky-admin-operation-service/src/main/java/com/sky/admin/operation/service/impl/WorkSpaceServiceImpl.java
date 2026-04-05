package com.sky.admin.operation.service.impl;


import com.sky.admin.operation.service.WorkSpaceService;
import com.sky.admin.vo.BusinessDataVO;
import com.sky.order.dubboService.OrderDubboService;
import com.sky.order.vo.OrderReportVO;
import com.sky.order.vo.TurnoverReportVO;
import com.sky.user.dubboService.UserDubboService;
import com.sky.user.vo.UserReportVO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @DubboReference
    private UserDubboService userMapper;
    @DubboReference
    private OrderDubboService orderMapper;


    @Override
    public BusinessDataVO getBusinessData() {

        BusinessDataVO businessDataVO = new BusinessDataVO();
        UserReportVO userReportVO = userMapper.userStatistics(LocalDate.now(), LocalDate.now());
        if(userReportVO == null){
            businessDataVO.setNewUsers(0);
        }else{
            businessDataVO.setNewUsers(Integer.valueOf(userReportVO.getNewUserList()));
        }
        OrderReportVO orderReportVO = orderMapper.ordersStatistics(LocalDate.now(), LocalDate.now());
        if(orderReportVO == null){
            businessDataVO.setOrderCompletionRate(0.0);
            businessDataVO.setValidOrderCount(0);

        }else{
            businessDataVO.setValidOrderCount( orderReportVO.getValidOrderCount());
            businessDataVO.setOrderCompletionRate(orderReportVO.getOrderCompletionRate());
        }
        TurnoverReportVO turnoverReportVO = orderMapper.turnoverStatistics(LocalDate.now(), LocalDate.now());
        if(turnoverReportVO == null){
            businessDataVO.setTurnover(0.0);
            businessDataVO.setUnitPrice(0.0);
        }else{
            businessDataVO.setTurnover(Double.valueOf(turnoverReportVO.getTurnoverList()));
            businessDataVO.setUnitPrice(businessDataVO.getTurnover() / businessDataVO.getValidOrderCount());
        }
        return businessDataVO;
    }


}
