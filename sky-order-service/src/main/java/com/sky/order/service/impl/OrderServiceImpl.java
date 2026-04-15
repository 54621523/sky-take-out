package com.sky.order.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.dubboService.CartDubboService;
import com.sky.cart.vo.ShoppingCartVO;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.order.domain.po.OrderDetail;
import com.sky.order.domain.po.Orders;
import com.sky.order.dto.*;
import com.sky.order.dubboService.OrderDubboService;
import com.sky.order.mapper.OrderDetailMapper;
import com.sky.order.mapper.OrderMapper;
import com.sky.order.mapper.mapstruct.OrderMapStruct;
import com.sky.order.service.OrderMessageProducer;
import com.sky.order.service.OrderService;
import com.sky.order.vo.*;
import com.sky.result.PageResult;
import com.sky.user.dubboService.AddressBookDubboService;
import com.sky.user.vo.AddressBookVO;
import com.sky.utils.WeChatPayUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@DubboService(interfaceClass = OrderDubboService.class)
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService,OrderDubboService {
    //TODO 修改成根据店铺变化
    private static final String SHOP_NAME = "SKY";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @DubboReference
    private AddressBookDubboService addressBookService;
    @DubboReference
    private CartDubboService shoppingCartService;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    @Autowired
    private OrderMessageProducer orderMessageProducer;



    private volatile String lastDate = "";

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long UserId = BaseContext.getCurrentId();
        //检测购物车内是否有商品
        List<ShoppingCartVO> shoppingCartList = shoppingCartService.listByUserId(UserId);
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //检测地址是否存在
        AddressBookVO addressBookVO = addressBookService.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBookVO == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //创建订单
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(UserId);
        //实际微信用户名其实根本不存在
        //orders.setUserName(userMapper.getByUserId(UserId).getName());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPhone(addressBookVO.getPhone());
        orders.setAddress(addressBookVO.getDetail());
        orders.setConsignee(addressBookVO.getConsignee());
        orders.setNumber(generateOrderNumber());
        //订单表插入一条数据
        orderMapper.insert(orders);
        //明细表插入多条数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).toList();
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartService.clean();

        OrderMessageDTO timeoutMessage = OrderMessageDTO.builder()
                .orderId(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .status(orders.getStatus())
                .userId(UserId)
                .build();
        orderMessageProducer.sendOrderTimeoutMessage(timeoutMessage);


        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        /*
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        */
        paySuccess(ordersPaymentDTO .getOrderNumber());
        return new OrderPaymentVO();
    }

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        return orderMapper.turnoverStatistics(begin,end);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
                orderMapper.updateById(orders);
        OrderMessageDTO paymentMessage = OrderMessageDTO.builder()
                .orderId(ordersDB.getId())
                .orderNumber(outTradeNo)
                .status(orders.getStatus())
                .userId(ordersDB.getUserId())
                .operateTime(LocalDateTime.now())
                .build();
        orderMessageProducer.sendPaymentSuccessMessage(paymentMessage);
    }

    @Override
    public OrderVO getOrderDetailById(Long id) {
        Orders orders = orderMapper.selectById(id);
        OrderVO orderVO = OrderMapStruct.INSTANCE.ordersPo2Vo(orders);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId,id));
        if(!orderDetailList.isEmpty()){
            String orderDishes = orderDetailList.stream()
                    .map(OrderDetail::getName)
                    .collect(Collectors.joining(","));
            orderVO.setOrderDishes(orderDishes);
            orderVO.setOrderDetailList(OrderMapStruct.INSTANCE.orderDetailPo2Vo(orderDetailList));
        }
        return orderVO;
    }

    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page4User(ordersPageQueryDTO);
        return buildOrderPageResult(page, false);
    }

    @Override
    public PageResult pageQuery4Shop(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page4Shop(ordersPageQueryDTO);
        return buildOrderPageResult(page, true);
    }

    private PageResult buildOrderPageResult(Page<Orders> page, boolean includeOrderDishes) {
        List<OrderVO> orderVOList = OrderMapStruct.INSTANCE.ordersPo2Vo(page.getResult());

        if (orderVOList == null || orderVOList.isEmpty()) {
            return new PageResult(page.getTotal(), orderVOList);
        }

        List<Long> orderIds = orderVOList.stream()
                .map(OrderVO::getId)
                .toList();

        List<OrderDetail> allOrderDetails = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>()
                        .in(OrderDetail::getOrderId, orderIds));

        Map<Long, List<OrderDetail>> orderDetailMap = allOrderDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        List<OrderVO> records = orderVOList.stream().map(orderVO -> {
            List<OrderDetail> orderDetailList = orderDetailMap.get(orderVO.getId());
            if (orderDetailList != null && !orderDetailList.isEmpty()) {
                if (includeOrderDishes) {
                    String orderDishes = orderDetailList.stream()
                            .map(OrderDetail::getName)
                            .collect(Collectors.joining(","));
                    orderVO.setOrderDishes(orderDishes);
                }
                orderVO.setOrderDetailList(OrderMapStruct.INSTANCE.orderDetailPo2Vo(orderDetailList));
            }
            return orderVO;
        }).toList();

        return new PageResult(page.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel4User(Long id) {
        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,id)
                .set(Orders::getStatus,Orders.CANCELLED)
                .set(Orders::getCancelReason,"用户取消")
                .set(Orders::getCancelTime,LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel4Shop(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        if(orders == null){
            //TODO 错误信息
            throw new OrderBusinessException("订单不存在");
        }

        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,ordersCancelDTO.getId())
                .set(Orders::getStatus,Orders.CANCELLED)
                .set(Orders::getCancelReason,ordersCancelDTO.getCancelReason())
                .set(Orders::getCancelTime,LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,id)
                .set(Orders::getStatus,Orders.COMPLETED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delivery(Long id) {
        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,id)
                .set(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS));
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderMessageDTO reminderMessage = OrderMessageDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .userId(order.getUserId())
                .content("用户催单")
                .build();
        orderMessageProducer.sendOrderReminderMessage(reminderMessage);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void repetition(Long id) {
        //查询id对应的订单
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(id);
        Long userId = BaseContext.getCurrentId();
        //详单明细添加
        for(OrderDetail orderDetail : orderDetailList){
            //构造DTO调用已有方法
            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            if(orderDetail.getDishId() != null){
                //菜品
                shoppingCartDTO.setDishId(orderDetail.getDishId());
                shoppingCartDTO.setDishFlavor(orderDetail.getDishFlavor());
            }else if(orderDetail.getSetmealId() != null){
                //套餐
                shoppingCartDTO.setSetmealId(orderDetail.getSetmealId());
            }
            shoppingCartService.save(shoppingCartDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,id)
                .set(Orders::getStatus,Orders.CONFIRMED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        if(orders == null){
            //TODO 错误信息
            throw new OrderBusinessException("订单不存在");
        }
        if(!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)){
            //TODO 错误信息
            throw new BaseException("只能拒绝\"待接单\"订单");
        }

        orderMapper.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId,ordersRejectionDTO.getId())
                .set(Orders::getStatus,Orders.REJECTED)
                .set(Orders::getCancelReason,ordersRejectionDTO.getRejectionReason())
                .set(Orders::getCancelTime,LocalDateTime.now()));
    }

    @Override
    public OrderOverViewVO getOverViewOrders(){
        return orderMapper.getOverViewOrders();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end){
        return orderMapper.ordersStatistics(begin,end);
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        return orderMapper.top10(begin, end);
    }


    //按日期隔离的原子计数器
    //private static final ConcurrentHashMap<String, AtomicInteger> dailyCounter = new ConcurrentHashMap<>();
    private String generateOrderNumber() {
        //获取当前日期
        LocalDateTime now = LocalDateTime.now();
        //单机方案，每次重启都会导致订单号重复
        //String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        //AtomicInteger counter = dailyCounter.computeIfAbsent(date, k -> new AtomicInteger(0));
        //int sequence = counter.incrementAndGet();
        //String sequenceStr = String.format("%04d", sequence);
        //String orderNumber = SHOP_NAME + "-" + date + "-" + sequenceStr;
        //return orderNumber;
        String dateKey = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "order:number" + dateKey;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        //一天内过期
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        //订单号尾补齐
        String sequenceStr = String.format("%04d", sequence);
        return SHOP_NAME + "-" + dateKey + "-" + sequenceStr;
    }
}
