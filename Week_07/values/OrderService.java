package traincamp.dbexpone.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import traincamp.dbexpone.dao.OrderMapper;
import traincamp.dbexpone.entity.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OrderService {

    @Autowired
    private SnowFlakeIdService idGenerator;

    @Autowired
    private OrderMapper orderMapper;

    private Random random = new Random();

    public void insertBatchOrders() {
        List<List<Order>> orders = new ArrayList<>(200);
        for (int i=0; i<200; i++){
            orders.add(generateManyOrders(5000));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        Long start = System.currentTimeMillis();
        //orders.stream().parallel().forEach(orderMapper::insertOrderBatch);
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for(List<Order> orderList : orders) {
            executorService.submit(()->{
                orderMapper.insertOrderBatch(orderList);
                try{
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
                //System.out.println(orderList.get(0).getId());
            });
        }
        executorService.shutdown();
        try {
            countDownLatch.await();
        }catch (Exception e) {
            e.printStackTrace();
        }
        Long end = System.currentTimeMillis();
        System.out.println("耗时："+(end - start)+"ms");
    }

    private List<Order> generateManyOrders(int count){
        List<Order> orders = new ArrayList<>(count);
        for(int i= 0; i < count; i++) {
            orders.add(generateOneOrder());
        }
        return orders;
    }

    public void insertOneOrder() {
        Order order = generateOneOrder();
        orderMapper.insert(order);
    }

    private Order generateOneOrder() {
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
        order.setTotalPrice(3000);
        order.setReceiverAddress("beijing somewhere");
        order.setReceiverName("someone");
        order.setReceiverMobile("13000013000");
        order.setRemark("");
        order.setFreight(0);
        order.setProcessState(0);
        order.setCreatedTime(new Date());
        return order;
    }

    private int getRandomId() {
        return random.nextInt(100000);
    }

    private class Task implements Runnable {
        private List<Order> orders;
        private OrderMapper orderMapper;
        public Task(final List<Order> orders,final OrderMapper orderMapper) {
            this.orders = orders;
            this.orderMapper = orderMapper;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            }catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(orders.get(0).getId());
            //orderMapper.insertOrderBatch(orders);
        }
    }
}
