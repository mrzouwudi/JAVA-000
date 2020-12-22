package traincamp.dubbo.exchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.dubbo.exchange.service.ExchangeService;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

import java.math.BigDecimal;

@RestController
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    private static final BigDecimal AMPLIFY = new BigDecimal("10000");

    /**
     * 人民币兑美元，这里amount必须是整数，即兑换时，人民币必须是整数元进行操作
     * @param userId
     * @param amount
     * @return
     */
    @RequestMapping("/cny2usd")
    public ExchangeTradeDO exchangeCny2Usd(@RequestParam("uid") Long userId,
                                  @RequestParam("amount")Long amount) {
        return exchangeService.exchangeCny2Usd(userId, amount);
    }

    /**
     * 人民币兑美元，这里amount必须是整数，即兑换时，人民币必须是整数元进行操作
     * @param userId
     * @param amount
     * @return
     */
    @RequestMapping("/usd2cny")
    public ExchangeTradeDO exchangeUsd2Cny(@RequestParam("uid") Long userId,
                                  @RequestParam("amount")Long amount) {
        return exchangeService.exchangeUsd2Cny(userId, amount);
    }
}
