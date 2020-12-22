package traincamp.dubbo.hmily.common.account.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.account.entity.AccountFreeze;

public interface UsdAccountFreezeMapper {

    @Insert(" insert into `t_usd_freeze` (id,user_id,account_id,amount,exchange_id,created) " +
            " values ( #{id},#{userId},#{accountId},#{amount},#{exchangeId},#{created})")
    int save(AccountFreeze accountFreeze);

    @Delete("delete from `t_usd_freeze` where id = #{id} and user_id= #{userId}")
    int deleteById(AccountFreeze accountFreeze);

    @Delete("delete from `t_usd_freeze` where exchange_id = #{exchangeId} and user_id= #{userId}")
    int deleteByChangeDTO(AccountChangeDTO accountChangeDTO);
}
