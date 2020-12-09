package traincamp.hmily.coupon.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.hmily.coupon.constant.CouponConstant;
import traincamp.hmily.coupon.dao.CouponMapper;
import traincamp.hmily.coupon.entity.Coupon;
import traincamp.hmily.coupon.entity.CouponExample;
import traincamp.hmily.coupon.service.CouponService;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponMapper couponMapper;

    /**
     * 验劵，因为劵码是唯一的，因此如果没查到，验劵失败，如果查到，劵的状态如果是未使用，则认为验劵成功，
     * 并且将将劵的状态置为准备中。
     * @param productId
     * @param couponCode
     * @return
     */
    @Override
    @Transactional
    public Boolean checkCoupon(Integer productId, String couponCode) {
        log.info("参数productId：{}， 参数couponCode：{}", productId, couponCode);
        CouponExample example = new CouponExample();
        example.or().andCodeEqualTo(couponCode);
        List<Coupon> couponList = couponMapper.selectByExample(example);
        Boolean result = CouponConstant.CHECK_FAIL;
        if (!couponList.isEmpty()) {
            Coupon coupon = couponList.get(0);
            if(coupon.getProductId().equals(productId) && coupon.getStatus().equals(CouponConstant.STATUS_NOUSE)) {
                result = CouponConstant.CHECK_SUCCESS;
            }
        }
        return result;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmExchange", cancelMethod = "cancelExchange")
    public Boolean exchangeCoupon(Integer productId, String couponCode) {
        updateCouponStatus(productId, couponCode, CouponConstant.STATUS_PREPARE);
        return CouponConstant.CHECK_SUCCESS;
    }

    private void confirmExchange(Integer productId, String couponCode) {
        updateCouponStatus(productId, couponCode, CouponConstant.STATUS_USED);
    }

    private void cancelExchange(Integer productId, String couponCode) {
        updateCouponStatus(productId, couponCode, CouponConstant.STATUS_USED);
    }

    private void updateCouponStatus(Integer productId, String couponCode, Integer status) {
        CouponExample example = new CouponExample();
        example.or().andCodeEqualTo(couponCode).andProductIdEqualTo(productId);
        Coupon coupon = new Coupon();
        coupon.setStatus(status);
        coupon.setUpdatedTime(new Date());
        couponMapper.updateByExampleSelective(coupon, example);
    }
}
