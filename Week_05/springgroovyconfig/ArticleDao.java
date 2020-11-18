package springgroovyconfig;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class ArticleDao {
    public int insertArticle(Article article) {
        System.out.println("begin insert a article record");
        System.out.println(article);
        return 1;
    }
}
