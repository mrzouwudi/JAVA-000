package traincamp.dubbo.hmily.common.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import traincamp.dubbo.hmily.common.account.dto.AccountUpdateDto;
import traincamp.dubbo.hmily.common.account.entity.Account;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.account.entity.AccountFreeze;

@Mapper
public interface CnyAccountMapper {

    @Update("update t_cny_account set balance = balance - #{changeAmount}, updated = now() " +
            " where user_id = #{userId} and  balance - #{changeAmount} >= 0 ")
    int decreaseBalance(AccountChangeDTO accountChangeDTO);

    @Update("update t_cny_account set balance = balance + #{changeAmount}, updated = now() " +
            " where user_id = #{userId} and  #{changeAmount} > 0 ")
    int increaseBalance(AccountChangeDTO accountChangeDTO);

    @Select("select id,user_id,balance, created, updated from t_cny_account where user_id =#{userId} limit 1")
    Account findByUserId(Long userId);

    @Select("select id,user_id,balance, created, updated from t_cny_account where id =#{id} limit 1")
    Account findById(Long id);
}
