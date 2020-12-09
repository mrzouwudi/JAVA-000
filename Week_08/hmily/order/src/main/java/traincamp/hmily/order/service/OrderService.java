package traincamp.hmily.order.service;

import traincamp.hmily.order.entity.Order;

public interface OrderService {
    String saveOrder(Integer userId, Integer productId, String couponCode);
}
