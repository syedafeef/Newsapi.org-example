package com.afeef.testnews.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.afeef.testnews.models.Article;
import com.afeef.testnews.models.SavedArticle;

import java.util.List;

@Dao
public interface SavedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavedArticle article);

    @Query("SELECT COUNT(news_id) > 0 FROM saved WHERE news_id = :articleId")
    Boolean isFavourite(int articleId);

    @Query("DELETE FROM saved WHERE news_id=:articleId")
    void removeSaved(int articleId);

    @Query("SELECT articles.* FROM articles, saved WHERE articles.id == saved.news_id ORDER BY saved.time_saved")
    List<Article> getAllSaved();

    @Query("DELETE FROM saved")
    void removeAllSaved();

}
