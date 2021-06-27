package com.afeef.testnews.api;

import com.afeef.testnews.models.News;
import com.afeef.testnews.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String API_KEY = "ENTER your api key here";

    @GET("top-headlines")
    Call<News> getNews(
            @Query("country") String country ,
            @Query("category") String category ,
            @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<News> getNewsSearch(
            @Query("q") String keyword,
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey
    );

    @Headers("X-Api-Key:" + API_KEY)
    @GET("/v2/sources")
    Call<News> getNewsbyCategory(
            @Query("category") String category,
            @Query("country") String country,
            @Query("language") String language
    );



    @GET("data/2.5/weather?appid=9b8cb8c7f11c077f8c4e217974d9ee40&units=metric")
    Call<WeatherResponse> getWeatherData(@Query("q") String name);

}
