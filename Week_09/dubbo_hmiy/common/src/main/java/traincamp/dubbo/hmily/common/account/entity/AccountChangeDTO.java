package traincamp.dubbo.hmily.common.account.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountChangeDTO implements Serializable {
    private static final long serialVersionUID = -1579635746902263733L;

    private Long userId;
    private Long accountId;
    private Long changeAmount;
    private String currencyType;
    private Long exchangeId;
}
