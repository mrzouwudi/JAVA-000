package springgroovyconfig;

import org.springframework.context.support.GenericGroovyApplicationContext;

public class GroovyConfigDemo {
    public static void main(String[] args) {
        GenericGroovyApplicationContext context = new GenericGroovyApplicationContext(
                "classpath:ArticleGroovyConfig.groovy");
        ArticleService articleService = (ArticleService)context.getBean("articleService");
        Article article = new Article(1, "文章标题", "文章内容");
        articleService.saveArticle(article);
    }
}
