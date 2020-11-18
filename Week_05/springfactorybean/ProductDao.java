package springfactorybean;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class ProductDao {
    public int insertProduct(Product product) {
        System.out.println("begin insert a product record");
        System.out.println(product);
        return 1;
    }
}
