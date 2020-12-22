package traincamp.dubbo.exchange.service;

import org.dromara.hmily.annotation.Hmily;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

public interface ExchangeOperationService {

    @Hmily
    boolean doExchangeOperation(ExchangeTradeDO exchangeTrade);
}
