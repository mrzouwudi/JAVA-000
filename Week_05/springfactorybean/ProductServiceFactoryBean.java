package springfactorybean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceFactoryBean implements FactoryBean {
    @Override
    public Object getObject() throws Exception {
        ProductService productService = new ProductService();
        productService.setProductDao(new ProductDao());
        return productService;
    }

    @Override
    public Class<?> getObjectType() {
        return ProductService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
