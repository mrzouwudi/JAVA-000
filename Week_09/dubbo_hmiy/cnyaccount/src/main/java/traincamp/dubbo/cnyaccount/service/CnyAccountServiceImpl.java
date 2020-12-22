package traincamp.dubbo.cnyaccount.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import traincamp.dubbo.hmily.common.account.api.CnyAccountService;
import traincamp.dubbo.hmily.common.account.entity.Account;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.account.entity.AccountFreeze;
import traincamp.dubbo.hmily.common.account.mapper.CnyAccountFreezeMapper;
import traincamp.dubbo.hmily.common.account.mapper.CnyAccountMapper;
import traincamp.dubbo.hmily.common.generator.IdGenerator;

import java.util.Date;

@DubboService(interfaceClass = CnyAccountService.class)
@Component
@Slf4j
public class CnyAccountServiceImpl implements CnyAccountService {

    @Autowired
    private CnyAccountMapper accountMapper;

    @Autowired
    private CnyAccountFreezeMapper accountFreezeMapper;

    private IdGenerator freezeRecordIdGenerator = new IdGenerator(11L);

    @Override
    public Long getAccountId(final Long userId) {
        Account account = accountMapper.findByUserId(userId);
        return account!=null ? account.getId():null ;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmDecrease", cancelMethod = "cancelDecrease")
    @Transactional(rollbackFor = Exception.class)
    public boolean accountDecrease(final AccountChangeDTO accountChange) {
        log.info("execute CnyAccountServiceImpl.accountDecrease method.the param is " + accountChange.toString());
        int ret = accountMapper.decreaseBalance(accountChange);
        if(0 == ret) {
            throw new HmilyRuntimeException("人民币账户余额不足");
        }
        AccountFreeze accountFreeze = new AccountFreeze();
        accountFreeze.setId(freezeRecordIdGenerator.getId());
        accountFreeze.setAccountId(accountChange.getAccountId());
        accountFreeze.setUserId(accountChange.getUserId());
        accountFreeze.setAmount(accountChange.getChangeAmount());
        accountFreeze.setExchangeId(accountChange.getExchangeId());
        accountFreeze.setCreated(new Date());
        accountFreezeMapper.save(accountFreeze);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDecrease(final AccountChangeDTO accountChangeDTO) {
        accountFreezeMapper.deleteByChangeDTO(accountChangeDTO);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelDecrease(final AccountChangeDTO accountChangeDTO) {
        int ret = accountMapper.increaseBalance(accountChangeDTO);
        if( 0 == ret) {
            throw new HmilyRuntimeException("恢复冻结人民币资产出错");
        }
        accountFreezeMapper.deleteByChangeDTO(accountChangeDTO);
        return Boolean.TRUE;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmIncrease", cancelMethod = "cancelIncrease")
    @Transactional(rollbackFor = Exception.class)
    public boolean accountIncrease(final AccountChangeDTO accountChange) {
        log.info("execute CnyAccountServiceImpl.accountIncrease method.the param is " + accountChange.toString());
        int ret = accountMapper.increaseBalance(accountChange);
        if( 0 == ret) {
            throw new HmilyRuntimeException("转入人民币资产出错");
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmIncrease(final AccountChangeDTO accountChangeDTO) {
        log.info("execute CnyAccountServiceImpl.confirmIncrease method.");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelIncrease(final AccountChangeDTO accountChangeDTO) {
        log.info("execute CnyAccountServiceImpl.cancelIncrease method.");
        int ret = accountMapper.decreaseBalance(accountChangeDTO);
        if(0 == ret) {
            throw new HmilyRuntimeException("人民币账户余额不足或没有相应账户，无法进行取消转入操作");
        }
        return Boolean.TRUE;
    }
}
