package com.afeef.testnews.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.afeef.testnews.models.Article;

import java.util.List;

@Dao
public interface HeadlinesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void bulkInsert(List<Article> articles);

    @Query("SELECT * FROM articles ORDER BY id DESC")
    List<Article> getAllArticles();

    @Query("SELECT * FROM articles WHERE category=:category ORDER BY id DESC")
    List<Article> getArticleByCategory(String category);

    @Query("SELECT * FROM articles ORDER BY id DESC")
    List<Article> getAllSearchResult();

    @Query("DELETE  FROM articles")
    void removeAllHeadlines();
}
