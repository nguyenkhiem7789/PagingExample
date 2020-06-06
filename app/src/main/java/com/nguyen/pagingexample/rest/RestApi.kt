package com.nguyen.pagingexample.rest

import com.nguyen.pagingexample.model.Feed
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RestApi {
    //https://newsapi.org/v2/everything?q=movies&apiKey=079dac74a5f94ebdb990ecf61c8854b7&pageSize=20&page=2
    @GET("/v2/everything")
    fun fetchFeed(
        @Query("q") q: String?,
        @Query("apiKey") apiKey: String?,
        @Query("page") page: Long,
        @Query("pageSize") pageSize: Int
    ): Call<Feed>
}
