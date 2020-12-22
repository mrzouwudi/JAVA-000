package traincamp.dubbo.hmily.common.account.api;

import org.dromara.hmily.annotation.Hmily;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;

public interface CnyAccountService {

    Long getAccountId(Long userId);

    @Hmily
    boolean accountDecrease(AccountChangeDTO accountChange);

    @Hmily
    boolean accountIncrease(AccountChangeDTO accountChange);
}
