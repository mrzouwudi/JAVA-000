package traincamp.hmily.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.hmily.order.entity.Order;
import traincamp.hmily.order.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/save")
    public String saveOrder(@RequestParam("uid")Integer userId,
                           @RequestParam("pid") Integer productId,
                           @RequestParam("code") String couponCode) {
        return orderService.saveOrder(userId,productId, couponCode);
    }

}
