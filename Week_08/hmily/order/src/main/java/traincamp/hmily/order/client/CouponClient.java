package traincamp.hmily.order.client;

import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "coupon-service")
public interface CouponClient {
    @GetMapping("/check")
    Boolean check(@RequestParam("pid") Integer productId, @RequestParam("code") String code);

    @GetMapping("/exchange")
    @Hmily
    Boolean exchange(@RequestParam("pid") Integer productId, @RequestParam("code") String code);
}
