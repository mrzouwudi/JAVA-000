package traincamp.dubbo.exchange.service;

import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

public interface ExchangeService {
    ExchangeTradeDO exchangeCny2Usd(Long userId, Long amount);
    ExchangeTradeDO exchangeUsd2Cny(Long userId, Long amount);
}
