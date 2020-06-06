package com.nguyen.pagingexample.datasource.factory

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.nguyen.pagingexample.datasource.FeedDataSource
import com.nguyen.pagingexample.model.Article
import org.w3c.dom.Node

class FeedDataFactory : DataSource.Factory<Long, Article>() {

    val mutableLiveData: MutableLiveData<FeedDataSource> by lazy {
        MutableLiveData<FeedDataSource>()
    }

    val feedDataSource: FeedDataSource by lazy {
        FeedDataSource()
    }

    override fun create(): DataSource<Long, Article> {
        mutableLiveData.postValue(feedDataSource)
        return feedDataSource
    }

}