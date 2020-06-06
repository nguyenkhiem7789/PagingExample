package com.nguyen.pagingexample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.nguyen.pagingexample.AppController
import com.nguyen.pagingexample.datasource.factory.FeedDataFactory
import com.nguyen.pagingexample.model.Article
import com.nguyen.pagingexample.utils.NetworkState
import org.jetbrains.annotations.NotNull
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FeedViewModel : ViewModel() {

    private val executor: Executor by lazy {
        Executors.newFixedThreadPool(5)
    }

    var networkState: LiveData<NetworkState>? = null

    var articleLivaData: LiveData<PagedList<Article>>? = null

    init {
        val feedDataFactory = FeedDataFactory()
        networkState = Transformations.switchMap(feedDataFactory.mutableLiveData) {
            it.networkState
        }
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20)
            .build()
        articleLivaData = LivePagedListBuilder(feedDataFactory, pagedListConfig)
            .setFetchExecutor(executor)
            .build()

    }

}