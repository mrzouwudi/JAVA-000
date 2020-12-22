package traincamp.dubbo.hmily.common.exchange.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ExchangeTradeDO {
    private Long id;
    private Long userId;
    private Long outAccountId;
    private String outCurrencyType;
    private Long outAmount;
    private Long inAccountId;
    private String inCurrencyType;
    private Long inAmount;
    private Integer status;
    private Date created;
    private Date updated;
}
