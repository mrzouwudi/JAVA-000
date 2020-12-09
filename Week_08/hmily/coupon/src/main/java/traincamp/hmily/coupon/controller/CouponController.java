package traincamp.hmily.coupon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.hmily.coupon.service.CouponService;

@RestController
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/coupon")
    public String coupon() {
        return "hello";
    }

    @GetMapping("/check")
    public Boolean check(@RequestParam("pid") Integer productId, @RequestParam("code")String code) {
        return couponService.checkCoupon(productId, code);
    }

    @GetMapping("/exchange")
    public Boolean exchange(@RequestParam("pid") Integer productId, @RequestParam("code")String code) {
        return couponService.exchangeCoupon(productId, code);
    }
}
