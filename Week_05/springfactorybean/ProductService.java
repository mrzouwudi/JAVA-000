package springfactorybean;

import lombok.Setter;

@Setter
public class ProductService {
    private ProductDao productDao;

    public boolean saveProduct(Product product) {
        int ret = productDao.insertProduct(product);
        return (ret > 0);
    }
}
