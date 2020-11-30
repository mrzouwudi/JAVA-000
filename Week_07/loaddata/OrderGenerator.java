package loaddata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class OrderGenerator {

    private SnowFlakeIdService idGenerator = new SnowFlakeIdService();

    private Random random = new Random();

    public List<Order> generateManyOrders(int count){
        List<Order> orders = new ArrayList<>(count);
        for(int i= 0; i < count; i++) {
            orders.add(generateOneOrder());
        }
        return orders;
    }

    public Order generateOneOrder() {
        Order order = new Order();
        order.setId(idGenerator.getId());
        order.setUserId(getRandomId());
        order.setProductId(getRandomId());
        order.setProductName("one product");
        order.setProductCode("1000fff");
        order.setProductPic("http://");
        order.setProcuctIntroduction("some words");
        order.setProductAmout(10);
        order.setProductPrice(300);
        order.setDiscount(0);
        order.setFreight(0);
        int totalPrice = order.getProductAmout() * order.getProductPrice() - order.getDiscount() + order.getFreight();
        order.setTotalPrice(totalPrice);
        order.setReceiverAddress("RPC somewhere");
        order.setReceiverName("someone");
        order.setReceiverMobile("13000013000");
        order.setRemark("");
        order.setProcessState(0);
        order.setCreatedTime(new Date());
        return order;
    }

    private int getRandomId() {
        return random.nextInt(100000);
    }

}
