package traincamp.dubbo.exchange.service.impl;

import org.springframework.stereotype.Service;
import traincamp.dubbo.exchange.constant.ExchangeConstant;
import traincamp.dubbo.exchange.service.ExchangeRateService;

import java.math.BigDecimal;

/**
 * 汇率查询服务，该实现是个简单模拟，只能查询人民币对美元或者美元对人民币的汇率，这个汇率是2020年12月某日的查询的汇率
 */
@Service
public class ExchangeRateServiceImple implements ExchangeRateService {
    /**
     * 汇率查询，该实现是个简单模拟
     * @param outCurrencyType  兑出的币种，只限人民币（CNY）和美元（USD）
     * @param inCurrencyType  兑入的币种，只限人民币（CNY）和美元（USD）
     * @return
     */
    @Override
    public BigDecimal getExchangeRate(final String outCurrencyType, final String inCurrencyType) {

        //人民币兑换美元
        if(ExchangeConstant.CURRENCY_CNY.equals(outCurrencyType) &&
                ExchangeConstant.CURRENCY_USD.equals(inCurrencyType)) {
            return new BigDecimal("0.1527");
        }
        //美元兑换人民币
        if(ExchangeConstant.CURRENCY_USD.equals(outCurrencyType) &&
                ExchangeConstant.CURRENCY_CNY.equals(inCurrencyType)) {
            return new BigDecimal("6.5507");
        }
        return null;
    }
}
