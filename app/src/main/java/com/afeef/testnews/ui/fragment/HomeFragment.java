package com.afeef.testnews.ui.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afeef.testnews.callback.CategorySelectedCallback;
import com.afeef.testnews.data.NewsDatabase;
import com.afeef.testnews.data.dao.HeadlinesDao;
import com.afeef.testnews.ui.NewsDetailActivity;
import com.afeef.testnews.R;
import com.afeef.testnews.ui.adapter.CategoryAdapter;
import com.afeef.testnews.utils.Utils;
import com.afeef.testnews.api.ApiClient;
import com.afeef.testnews.api.ApiInterface;
import com.afeef.testnews.models.Article;
import com.afeef.testnews.models.News;
import com.afeef.testnews.ui.MainActivity;
import com.afeef.testnews.ui.adapter.Adapter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, CategorySelectedCallback {


    private RecyclerView recyclerViewNews, recyclerViewCategory;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.LayoutManager layoutManagerHORIZONTAL;

    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private CategoryAdapter categoryAdapter;
    private String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;
    HeadlinesDao headlinesDao;
    String apiCategory = ApiClient.Category.business.title;
    String selectedCategory;
    List<ApiClient.Category> enumValues;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        headlinesDao = NewsDatabase.getInstance(getContext()).headlinesDao();

        enumValues = new ArrayList<>(EnumSet.allOf(ApiClient.Category.class));


        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        recyclerViewNews = root.findViewById(R.id.recyclerViewNews);
        recyclerViewCategory = root.findViewById(R.id.recyclerViewCategory);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerViewNews.setLayoutManager(layoutManager);
        recyclerViewNews.setItemAnimator(new DefaultItemAnimator());
        recyclerViewNews.setNestedScrollingEnabled(false);

        layoutManagerHORIZONTAL = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL,false);
        categoryAdapter = new CategoryAdapter( getActivity(), enumValues,this);
        recyclerViewCategory.setLayoutManager(layoutManagerHORIZONTAL);
        recyclerViewCategory.setAdapter(categoryAdapter);


        onLoadingSwipeRefresh("");

        errorLayout = root.findViewById(R.id.errorLayout);
        errorImage = root.findViewById(R.id.errorImage);
        errorTitle = root.findViewById(R.id.errorTitle);
        errorMessage = root.findViewById(R.id.errorMessage);
        btnRetry = root.findViewById(R.id.btnRetry);

        return root;
    }


    public void clearUI(){
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
    }

    public void getNewNewsAPI(String keyword) {

        clearUI();

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;

        if (keyword.length() > 0) {
            apiCategory = "search";
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", ApiInterface.API_KEY);
        } else {
            call = apiInterface.getNews(country, apiCategory, ApiInterface.API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {

                    if (!articles.isEmpty()) {
                        articles.clear();
                    }

                    articles = response.body().getArticle();

                    if (null != articles){
                        for (Article article : articles) {
                            article.setCategory(apiCategory);
                        }
                        headlinesDao.bulkInsert(articles);
                    }

                    articles.clear();
                    articles = headlinesDao.getArticleByCategory(apiCategory);

                    adapter = new Adapter(articles, getActivity());

                    recyclerViewNews.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(R.drawable.no_result, "No Result", "Please Try Again!\n" + errorCode);

                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(R.drawable.oops, "Oops..", "Network failure, Please Try Again\n" + t.toString());
            }
        });

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


    @Override
    public void onRefresh() {
        clearUI();
        apiCategory = selectedCategory;
        getNewNewsAPI("");
    }

    public void onLoadingSwipeRefresh(final String keyword) {
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        getNewNewsAPI(keyword);
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message) {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });
    }

    @Override
    public void categorySelectedCallback(String category) {
        selectedCategory = category;
        apiCategory = selectedCategory;
        getNewNewsAPI("");
    }
}