package traincamp.dubbo.exchange.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.dubbo.exchange.constant.ExchangeConstant;
import traincamp.dubbo.exchange.service.ExchangeOperationService;
import traincamp.dubbo.hmily.common.account.api.CnyAccountService;
import traincamp.dubbo.hmily.common.account.api.UsdAccountService;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;
import traincamp.dubbo.hmily.common.exchange.mapper.ExchangeMapper;

@Service
@Slf4j
public class ExchangeOperationServiceImpl implements ExchangeOperationService {

    @DubboReference
    private CnyAccountService cnyAccountService;

    @DubboReference
    private UsdAccountService usdAccountService;

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Override
    @HmilyTCC(confirmMethod = "confirmStatus", cancelMethod = "cancelStatus")
    @Transactional
    public boolean doExchangeOperation(final ExchangeTradeDO exchangeTrade) {
        log.info("doExchangeOperation is called");
        log.info(exchangeTrade.toString());
        exchangeMapper.updatePrepareStatus(exchangeTrade);
        AccountChangeDTO accountChangeDTO = new AccountChangeDTO();
        accountChangeDTO.setUserId(exchangeTrade.getUserId());
        accountChangeDTO.setExchangeId(exchangeTrade.getId());
        accountChangeDTO.setChangeAmount(exchangeTrade.getOutAmount());
        accountChangeDTO.setAccountId(exchangeTrade.getOutAccountId());
        //扣减兑出的账户的金额
        if(ExchangeConstant.CURRENCY_CNY.equals(exchangeTrade.getOutCurrencyType())) {
            cnyAccountService.accountDecrease(accountChangeDTO);
        } else {
            usdAccountService.accountDecrease(accountChangeDTO);
        }
        accountChangeDTO.setAccountId(exchangeTrade.getInAccountId());
        accountChangeDTO.setChangeAmount(exchangeTrade.getInAmount());
        //增加兑换回的金额到兑入账户
        if(ExchangeConstant.CURRENCY_CNY.equals(exchangeTrade.getInCurrencyType())) {
            cnyAccountService.accountIncrease(accountChangeDTO);
        } else {
            usdAccountService.accountIncrease(accountChangeDTO);
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmStatus(final ExchangeTradeDO exchangeTrade) {
        log.info("confirmStatus is called");
        exchangeMapper.updateSuccessStatus(exchangeTrade);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelStatus(final ExchangeTradeDO exchangeTrade) {
        log.info("cancelStatus is called");
        exchangeMapper.updateFailStatus(exchangeTrade);
        return Boolean.TRUE;
    }
}
