package traincamp.dubbo.hmily.common.account.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountUpdateDto implements Serializable {

    private static final long serialVersionUID = -6735895525336120837L;

    private Long userId;

    private Long accountId;

    private Long amount;
}
