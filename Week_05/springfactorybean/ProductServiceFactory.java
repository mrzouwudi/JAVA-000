package springfactorybean;

import lombok.Setter;

@Setter
public class ProductServiceFactory {
    private ProductDao productDao;

    public ProductService getProductService() {
        ProductService productService = new ProductService();
        productService.setProductDao(productDao);
        return productService;
    }

}
