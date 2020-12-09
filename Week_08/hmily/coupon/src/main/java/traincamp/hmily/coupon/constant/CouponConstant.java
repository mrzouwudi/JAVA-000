package traincamp.hmily.coupon.constant;

public class CouponConstant {
    //状态:未使用
    public final static Integer STATUS_NOUSE = Integer.valueOf(0);
    //状态:准备使用
    public final static Integer STATUS_PREPARE = Integer.valueOf(1);
    //状态:已使用
    public final static Integer STATUS_USED = Integer.valueOf(2);
    //状态:已废弃
    public final static Integer STATUS_DISCARDED = Integer.valueOf(3);
    //验劵:已废弃
    public final static Boolean CHECK_SUCCESS = Boolean.TRUE;
    //验劵:已废弃
    public final static Boolean CHECK_FAIL = Boolean.FALSE;
}
