package springgroovyconfig;

public class ArticleService {
    private ArticleDao articleDao;

    public ArticleService(ArticleDao articleDao) {
        this.articleDao = articleDao;
    }

    public boolean saveArticle(Article article) {
        int ret = articleDao.insertArticle(article);
        return (ret > 0);
    }
}
