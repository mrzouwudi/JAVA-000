package loaddata;

import java.util.Date;

public class Order {
    private Long id;

    private Integer userId;

    private Integer productId;

    private String productCode;

    private String productName;

    private String productPic;

    private String procuctIntroduction;

    private Integer productAmout;

    private Integer productPrice;

    private Integer discount;

    private Integer totalPrice;

    private String receiverAddress;

    private String receiverName;

    private String receiverMobile;

    private String remark;

    private Integer freight;

    private Integer processState;

    private Date createdTime;

    private Date updatedTime;

    private Date paidTime;

    private Date deliveriedTime;

    public Order(Long id, Integer userId, Integer productId, String productCode, String productName, String productPic, String procuctIntroduction, Integer productAmout, Integer productPrice, Integer discount, Integer totalPrice, String receiverAddress, String receiverName, String receiverMobile, String remark, Integer freight, Integer processState, Date createdTime, Date updatedTime, Date paidTime, Date deliveriedTime) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.productPic = productPic;
        this.procuctIntroduction = procuctIntroduction;
        this.productAmout = productAmout;
        this.productPrice = productPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
        this.receiverAddress = receiverAddress;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.remark = remark;
        this.freight = freight;
        this.processState = processState;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.paidTime = paidTime;
        this.deliveriedTime = deliveriedTime;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode == null ? null : productCode.trim();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getProductPic() {
        return productPic;
    }

    public void setProductPic(String productPic) {
        this.productPic = productPic == null ? null : productPic.trim();
    }

    public String getProcuctIntroduction() {
        return procuctIntroduction;
    }

    public void setProcuctIntroduction(String procuctIntroduction) {
        this.procuctIntroduction = procuctIntroduction == null ? null : procuctIntroduction.trim();
    }

    public Integer getProductAmout() {
        return productAmout;
    }

    public void setProductAmout(Integer productAmout) {
        this.productAmout = productAmout;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress == null ? null : receiverAddress.trim();
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName == null ? null : receiverName.trim();
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile == null ? null : receiverMobile.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getFreight() {
        return freight;
    }

    public void setFreight(Integer freight) {
        this.freight = freight;
    }

    public Integer getProcessState() {
        return processState;
    }

    public void setProcessState(Integer processState) {
        this.processState = processState;
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

    public Date getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Date paidTime) {
        this.paidTime = paidTime;
    }

    public Date getDeliveriedTime() {
        return deliveriedTime;
    }

    public void setDeliveriedTime(Date deliveriedTime) {
        this.deliveriedTime = deliveriedTime;
    }
}