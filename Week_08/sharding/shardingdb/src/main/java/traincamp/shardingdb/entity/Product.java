package traincamp.shardingdb.entity;

import java.util.Date;

public class Product {
    private Integer id;

    private String productName;

    private String productCode;

    private String productPic;

    private Integer productPrice;

    private Integer productStorage;

    private String productIntroduction;

    private Integer displayOrder;

    private Byte onSaleFlag;

    private Date createdTime;

    private Date updatedTime;

    public Product(Integer id, String productName, String productCode, String productPic, Integer productPrice, Integer productStorage, String productIntroduction, Integer displayOrder, Byte onSaleFlag, Date createdTime, Date updatedTime) {
        this.id = id;
        this.productName = productName;
        this.productCode = productCode;
        this.productPic = productPic;
        this.productPrice = productPrice;
        this.productStorage = productStorage;
        this.productIntroduction = productIntroduction;
        this.displayOrder = displayOrder;
        this.onSaleFlag = onSaleFlag;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Product() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode == null ? null : productCode.trim();
    }

    public String getProductPic() {
        return productPic;
    }

    public void setProductPic(String productPic) {
        this.productPic = productPic == null ? null : productPic.trim();
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductStorage() {
        return productStorage;
    }

    public void setProductStorage(Integer productStorage) {
        this.productStorage = productStorage;
    }

    public String getProductIntroduction() {
        return productIntroduction;
    }

    public void setProductIntroduction(String productIntroduction) {
        this.productIntroduction = productIntroduction == null ? null : productIntroduction.trim();
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Byte getOnSaleFlag() {
        return onSaleFlag;
    }

    public void setOnSaleFlag(Byte onSaleFlag) {
        this.onSaleFlag = onSaleFlag;
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