

import springgroovyconfig.ArticleDao
import springgroovyconfig.ArticleService

beans{
    articleDao(ArticleDao)
    articleService(ArticleService, articleDao)
}