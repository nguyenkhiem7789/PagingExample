package com.nguyen.pagingexample.datasource

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.nguyen.pagingexample.AppController
import com.nguyen.pagingexample.model.Article
import com.nguyen.pagingexample.model.Feed
import com.nguyen.pagingexample.utils.API_KEY
import com.nguyen.pagingexample.utils.NetworkState
import com.nguyen.pagingexample.utils.QUERY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedDataSource : PageKeyedDataSource<Long, Article>() {

    companion object {
        private const val TAG = "FeedDataSource"
    }

    val networkState: MutableLiveData<NetworkState> by lazy {
        MutableLiveData<NetworkState>()
    }


    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, Article>
    ) {
        networkState.postValue(NetworkState.LOADING)

        AppController.restApi.fetchFeed(QUERY, API_KEY, 1, params.requestedLoadSize)
            .enqueue(object : retrofit2.Callback<Feed> {
                override fun onFailure(call: Call<Feed>, t: Throwable) {
                    if(t.message != null) {
                        networkState.postValue(NetworkState(NetworkState.Status.FAILED, t.message!!))
                    }
                }

                override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                    if(response.isSuccessful) {
                        if(response.body()!!.articles != null) {
                            callback.onResult(response.body()?.articles!!, null, 2)
                        }
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        networkState.postValue(NetworkState(NetworkState.Status.FAILED, response.message()))
                    }
                }
            })
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, Article>) {
        Log.i(TAG, "Loading Rang " + params.key.toString() + " Count " + params.requestedLoadSize)
        networkState.postValue(NetworkState.LOADING)

        AppController.restApi.fetchFeed(QUERY, API_KEY, params.key, params.requestedLoadSize)
            .enqueue(object : Callback<Feed> {
                override fun onFailure(call: Call<Feed>, t: Throwable) {
                    if(t.message != null) {
                        networkState.postValue(NetworkState(NetworkState.Status.FAILED, t.message!!))
                    }
                }

                override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                    if(response.isSuccessful) {
                        val nextKey = if (params.key == response.body()!!.totalResults) null else params.key + 1
                        if(response.body()?.articles != null) {
                            callback.onResult(response.body()?.articles!!, nextKey)
                        }
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        networkState.postValue(NetworkState(NetworkState.Status.FAILED, response.message()))
                    }
                }

            })
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, Article>) {
        TODO("Not yet implemented")
    }
}