package com.afeef.testnews.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.afeef.testnews.data.dao.HeadlinesDao;
import com.afeef.testnews.data.dao.SavedDao;
import com.afeef.testnews.models.Article;
import com.afeef.testnews.models.SavedArticle;

@Database(entities = {Article.class,  SavedArticle.class},
        version = 1,
        exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class NewsDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "news";
    private static NewsDatabase sInstance;

    public static NewsDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(), NewsDatabase.class, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return sInstance;
    }

    public abstract SavedDao savedDao();
    public abstract HeadlinesDao headlinesDao();

}
