package traincamp.hmily.order.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import traincamp.hmily.order.client.CouponClient;
import traincamp.hmily.order.constant.OrderConstant;
import traincamp.hmily.order.dao.OrderMapper;
import traincamp.hmily.order.entity.Order;
import traincamp.hmily.order.service.ExchangeService;
import traincamp.hmily.order.service.OrderIdGenerateService;
import traincamp.hmily.order.service.OrderService;

import java.util.Date;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderIdGenerateService idGenerateService;

    @Autowired
    private CouponClient couponClient;

    @Autowired
    private ExchangeService exchangeService;

    @Override
    public String saveOrder(Integer userId, Integer productId, String couponCode) {
        Order order = fillNewOrder(userId, productId, couponCode);
        orderMapper.insertSelective(order);
        Boolean ret = couponClient.check(productId, couponCode);
        if(!ret) {
            return "fail";
        }
        exchangeService.exchange(order);
        return "success";
    }

    private Order fillNewOrder(Integer userId, Integer productId, String couponCode) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setId(idGenerateService.getId());
        order.setStatus(OrderConstant.STATUS_CRTEATE);
        order.setCreatedTime(new Date());
        order.setCouponCode(couponCode);
        return order;
    }

}
