package traincamp.dubbo.exchange.constant;

public class ExchangeConstant {

    public final static Long EXCHANGE_WORKER = 1L;
    //以下是货币代码，参考http://www.webmasterhome.cn/huilv/huobidaima.asp
    public final static String CURRENCY_CNY = "CNY";
    public final static String CURRENCY_USD = "USD";

    public final static Integer STATUS_CREATE = new Integer(0);
    public final static Integer STATUS_PREPARE = new Integer(1);
    public final static Integer STATUS_SUCCESS = new Integer(2);
    public final static Integer STATUS_FAIL = new Integer(3);
}
