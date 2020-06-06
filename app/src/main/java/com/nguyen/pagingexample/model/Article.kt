package com.nguyen.pagingexample.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article (
    val id: Long? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null
) : Parcelable {
    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Article?> = object : DiffUtil.ItemCallback<Article?>() {
            override fun areItemsTheSame(
                @NonNull oldItem: Article,
                @NonNull newItem: Article
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                @NonNull oldItem: Article,
                @NonNull newItem: Article
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}



