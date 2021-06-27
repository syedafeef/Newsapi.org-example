package com.afeef.testnews.ui.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afeef.testnews.R;
import com.afeef.testnews.data.NewsDatabase;
import com.afeef.testnews.data.dao.SavedDao;
import com.afeef.testnews.models.Article;
import com.afeef.testnews.ui.NewsDetailActivity;
import com.afeef.testnews.ui.adapter.Adapter;

import java.util.ArrayList;
import java.util.List;


public class SavedNewsFragment extends Fragment {

    SavedDao savedDao;
    Adapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<Article> articles = new ArrayList<>();

    public static SavedNewsFragment newInstance() {
        return new SavedNewsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.savednews_fragment, container, false);

        savedDao = NewsDatabase.getInstance(getContext()).savedDao();

        recyclerView = root.findViewById(R.id.rv_saved_news_posts);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        articles = savedDao.getAllSaved();

        if (articles.size()>0){
            updateAdapter(articles);
        }else {
            Toast.makeText(getContext(),"Nothing Saved",Toast.LENGTH_SHORT).show();
        }

        return root;
    }


    public void updateAdapter(List<Article> articleList){
        adapter = new Adapter(articleList, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        initListener();
    }




    private void initListener() {
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("id", article.getId());
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                Pair<View, String> pair = Pair.create((View) imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pair);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, optionsCompat.toBundle());
                } else {
                    startActivity(intent);
                }

            }
        });

    }
}
