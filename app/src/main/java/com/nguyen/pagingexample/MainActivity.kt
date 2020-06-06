package com.nguyen.pagingexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.nguyen.pagingexample.adapters.FeedListAdapter
import com.nguyen.pagingexample.databinding.FeedActivityBinding
import com.nguyen.pagingexample.model.Article
import com.nguyen.pagingexample.utils.NetworkState
import com.nguyen.pagingexample.viewmodel.FeedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FeedListAdapter
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var binding: FeedActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //step 1: Using DataBinding, we setup the layout for the activity
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //Step 2: Initialize the ViewModel
        feedViewModel = FeedViewModel()

        //Step 3: Setup the adapter class for the RecyclerView
        binding.listFeed.layoutManager = LinearLayoutManager(applicationContext)
        adapter = FeedListAdapter(applicationContext)

        //Step 4: When a new page is available, we call submitList() method
        //of the PagedListAdapter class
        feedViewModel.articleLivaData?.observe(this, Observer<PagedList<Article>> {
            adapter.submitList(it)
        })

        feedViewModel.networkState?.observe(this, Observer<NetworkState> {
            adapter.setNetworkState(it)
        })
        binding.listFeed.adapter = adapter
    }
}