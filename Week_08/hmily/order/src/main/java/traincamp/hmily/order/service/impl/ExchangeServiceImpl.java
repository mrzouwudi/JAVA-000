package traincamp.hmily.order.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import traincamp.hmily.order.client.CouponClient;
import traincamp.hmily.order.constant.OrderConstant;
import traincamp.hmily.order.dao.OrderMapper;
import traincamp.hmily.order.entity.Order;
import traincamp.hmily.order.service.ExchangeService;

import java.util.Date;

@Service
@Slf4j
@SuppressWarnings("all")
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CouponClient couponClient;

    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String exchange(Order order) {
        updateOrderStatus(order, OrderConstant.STATUS_PREPARE);
        couponClient.exchange(order.getProductId(), order.getCouponCode());
        return "success";
    }

    private void confirmOrderStatus(Order order) {
        updateOrderStatus(order, OrderConstant.STATUS_FINISH);
    }

    private void cancelOrderStatus(Order order) {
        updateOrderStatus(order, OrderConstant.STATUS_FAIL);
    }

    private void updateOrderStatus(Order order, Integer status) {
        order.setStatus(status);
        order.setUpdatedTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
    }
}
