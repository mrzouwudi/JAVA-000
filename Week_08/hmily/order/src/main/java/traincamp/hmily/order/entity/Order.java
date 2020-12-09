package traincamp.hmily.order.entity;

import java.util.Date;

public class Order {
    private Long id;

    private Integer userId;

    private Integer productId;

    private Integer status;

    private String couponCode;

    private Date createdTime;

    private Date updatedTime;

    public Order(Long id, Integer userId, Integer productId, Integer status, String couponCode, Date createdTime, Date updatedTime) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.status = status;
        this.couponCode = couponCode;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Order() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode == null ? null : couponCode.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}