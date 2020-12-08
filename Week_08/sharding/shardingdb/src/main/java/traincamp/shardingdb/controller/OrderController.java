package traincamp.shardingdb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.shardingdb.entity.ExpressReceiverInfo;
import traincamp.shardingdb.entity.Order;
import traincamp.shardingdb.entity.Product;
import traincamp.shardingdb.service.OrderService;
import traincamp.shardingdb.service.ProductService;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/save")
    public Order saveOrder(@RequestParam("uid") Integer userId,
                           @RequestParam("pid") Integer productId,
                           @RequestParam("amount") int amount) {
        Product product = productService.getProductById(productId);
        ExpressReceiverInfo receiverInfo = generateReceiverInfo();
        return orderService.saveNormalOrder(userId, product, amount, receiverInfo);
    }

    private ExpressReceiverInfo generateReceiverInfo() {
        ExpressReceiverInfo receiverInfo = new ExpressReceiverInfo();
        receiverInfo.setReceiverName("张三");
        receiverInfo.setReceiverAddress("某市某区某街道123号");
        receiverInfo.setReceiverMobile("13500135000");
        return receiverInfo;
    }

    @GetMapping("/pay")
    public String updatePaidState(@RequestParam("id") Long orderId) {
        boolean success = orderService.updatePaidState(orderId);
        return success ? "支付成功！" : "支付失败！";
    }

    @GetMapping("/get")
    public Order getOrderById(@RequestParam("id") Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if(order != null) {
            return order;
        }
        return new Order();
    }

    @GetMapping("/delete")
    public String deleteById(@RequestParam("id") Long orderId) {
        boolean success = orderService.deleteOrder(orderId);
        return success ? "删除成功！" : "删除失败！";
    }

}
