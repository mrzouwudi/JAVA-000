package springfactorybean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FoctoryMethodDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-factorybean2.xml");
        ProductService productService = (ProductService)context.getBean("productService");
        Product product = new Product(1, "book", 59, 20);
        productService.saveProduct(product);
    }

}
