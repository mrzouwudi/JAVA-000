package traincamp.hmily.coupon.service;

public interface CouponService {
    Boolean checkCoupon(Integer productId, String couponCode);
    Boolean exchangeCoupon(Integer productId, String couponCode);
}
