package traincamp.shardingdb.constant;

public class OrderConstant {
    //创建订单
    public final static Integer PROCESS_STATE_CREATED = Integer.valueOf(0);
    //已支付未发货
    public final static Integer PROCESS_STATE_PAID_UNDELIVERY = Integer.valueOf(1);
    //已发货未收货
    public final static Integer PROCESS_STATE_DELIVERIED = Integer.valueOf(2);
    //已收货
    public final static Integer PROCESS_STATE_PAID_RECEIVIED = Integer.valueOf(3);
    //取消订单
    public final static Integer PROCESS_STATE_CNACLLED = Integer.valueOf(4);
    //退货
    public final static Integer PROCESS_STATE_RETURNED = Integer.valueOf(5);
}
