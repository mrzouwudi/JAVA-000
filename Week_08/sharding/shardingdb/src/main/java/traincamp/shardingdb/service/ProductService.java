package traincamp.shardingdb.service;

import org.springframework.stereotype.Service;
import traincamp.shardingdb.entity.Product;

import java.util.Date;

@Service
public class ProductService {

    public Product getProductById(Integer productId) {
        Product product = new Product();
        product.setId(productId);
        product.setProductCode("6553221233");
        product.setProductName("商品A");
        product.setProductIntroduction("一段描述");
        product.setProductPic("http://somesite.com/staic/product.jpg");
        product.setProductPrice(15000); //15000分=150元
        Date now = new Date();
        product.setCreatedTime(now);
        product.setUpdatedTime(now);
        product.setProductStorage(1000);
        product.setOnSaleFlag((byte)1);
        product.setDisplayOrder(50);
        return product;
    }
}
