package com.nguyen.pagingexample.adapters

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nguyen.pagingexample.R
import com.nguyen.pagingexample.databinding.FeedItemBinding
import com.nguyen.pagingexample.databinding.NetworkItemBinding
import com.nguyen.pagingexample.model.Article
import com.nguyen.pagingexample.utils.AppUtils
import com.nguyen.pagingexample.utils.NetworkState
import com.squareup.picasso.Picasso

class FeedListAdapter : PagedListAdapter<Article, RecyclerView.ViewHolder> {

    companion object {
        private val TYPE_PROGRESS = 0
        private val TYPE_ITEM = 1
    }

    private var networkState: NetworkState? = null

    lateinit var context: Context

    constructor(context: Context) : super(Article.DIFF_CALLBACK) {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if(viewType == TYPE_PROGRESS) {
            val headerBinding = NetworkItemBinding.inflate(layoutInflater, parent, false)
            val viewHolder = NetworkStateItemViewHolder(headerBinding)
            return viewHolder
        } else {
            val itemBinding = FeedItemBinding.inflate(layoutInflater, parent, false)
            val viewHolder = ArticleItemViewHolder(itemBinding)
            return viewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ArticleItemViewHolder) {
            if(getItem(position) != null) {
                holder.bindTo(getItem(position)!!)
            }
        } else if (holder is NetworkStateItemViewHolder) {
            if(networkState != null) {
                holder.bindView(networkState!!)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(hasExtraRow() && position == itemCount - 1) {
            return TYPE_PROGRESS
        } else {
            return TYPE_ITEM
        }
    }

    private fun hasExtraRow() : Boolean {
        return networkState != null && networkState != NetworkState.LOADED
    }

    fun setNetworkState(newNetworkState: NetworkState) {
        var previousState = this.networkState
        var previousExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        var newExtraRow = hasExtraRow()
        if(previousExtraRow != newExtraRow) {
            if(previousExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if(newExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    inner class ArticleItemViewHolder(val binding: FeedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(article: Article) {
            binding.itemImage.visibility = View.VISIBLE
            binding.itemDesc.visibility = View.VISIBLE
            val author = if (article.author == null || article.author.isEmpty()) context.getString(R.string.author_name) else article.author
            if(article.title != null) {
                val titleString = String.format(context.getString(R.string.item_title), author, article.title)
                val spannableString = SpannableString(titleString)
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context.applicationContext, R.color.secondary_text)),
                    titleString.lastIndexOf(author) + author.length + 1,
                    titleString.lastIndexOf(article.title) - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.itemTitle.text = spannableString
                binding.itemTime.text = java.lang.String.format(
                    context.getString(R.string.item_date),
                    AppUtils.getDate(article.publishedAt),
                    AppUtils.getTime(article.publishedAt)
                )
            }
            binding.itemDesc.text = article.description
            Picasso.get().load(article.urlToImage).resize(250, 200).into(binding.itemImage)
        }
    }

    inner class NetworkStateItemViewHolder(val binding: NetworkItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(networkState: NetworkState) {
            if(networkState.status == NetworkState.Status.RUNNING) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
            if(networkState.status == NetworkState.Status.FAILED) {
                binding.errorMsg.visibility = View.VISIBLE
            } else {
                binding.errorMsg.visibility = View.GONE
            }
        }
    }

}