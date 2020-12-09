package traincamp.hmily.coupon.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import traincamp.hmily.coupon.entity.Coupon;
import traincamp.hmily.coupon.entity.CouponExample;

public interface CouponMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Coupon record);

    int insertSelective(Coupon record);

    List<Coupon> selectByExample(CouponExample example);

    Coupon selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Coupon record, @Param("example") CouponExample example);

    int updateByExample(@Param("record") Coupon record, @Param("example") CouponExample example);

    int updateByPrimaryKeySelective(Coupon record);

    int updateByPrimaryKey(Coupon record);
}