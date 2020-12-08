package traincamp.shardingdb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.shardingdb.constant.OrderConstant;
import traincamp.shardingdb.dao.OrderMapper;
import traincamp.shardingdb.entity.ExpressReceiverInfo;
import traincamp.shardingdb.entity.Order;
import traincamp.shardingdb.entity.Product;

import java.util.Date;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * 保存订单，这里进行一些简化处理，订单只能购买一种商品，这就不用另外的详情表处理了，而且没有折扣和运费，则总价就是单价乘数量
     * @param userId  用户Id
     * @param product  购买的商品
     * @param amount  购买的数量
     * @param receiverInfo   快递接收人的信息
     * @return
     */
    public Order saveNormalOrder(Integer userId, Product product, int amount, ExpressReceiverInfo receiverInfo) {
        Order order = new Order();
        order.setId(idGeneratorService.getId());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductCode(product.getProductCode());
        order.setProcuctIntroduction(product.getProductIntroduction());
        order.setProductName(product.getProductName());
        order.setProductPic(product.getProductPic());
        order.setProductPrice(product.getProductPrice());
        order.setProductAmout(amount);
        int totalPrice = amount * product.getProductPrice();
        order.setTotalPrice(totalPrice);
        order.setDiscount(0);
        order.setFreight(0);
        order.setRemark("");
        order.setReceiverName(receiverInfo.getReceiverName());
        order.setReceiverAddress(receiverInfo.getReceiverAddress());
        order.setReceiverMobile(receiverInfo.getReceiverMobile());
        order.setProcessState(OrderConstant.PROCESS_STATE_CREATED);
        Date now = new Date();
        order.setCreatedTime(now);
        order.setUpdatedTime(now);
        orderMapper.insertSelective(order);
        //orderMapper.insert(order);
        return order;
    }

    /**
     * 更新订单状态，为已支付状态，同时更新支付时间。为了进行幂等性操作，使用for update先加行锁，再进行更新。
     * @param orderId
     * @return
     */
    public boolean updatePaidState(Long orderId) {
        Integer state = orderMapper.getProcessStateByIdForUpdate(orderId);
        if(state.equals(OrderConstant.PROCESS_STATE_CREATED)) {
            int ret = orderMapper.updatePaidProcessState(orderId, new Date());
            return (ret > 0);
        }
        return false;
    }

    /**
     * 根据订单Id获取订单信息
     * @param orderId
     * @return
     */
    public Order getOrderById(Long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    /**
     * 删除订单。本方法纯粹是为例演示删除的功能的使用，实际应用中订单是不能被删除的，甚至连逻辑删除也没有
     */
    public boolean deleteOrder(Long orderId) {
        int ret = orderMapper.deleteByPrimaryKey(orderId);
        return (ret > 0);
    }
}
