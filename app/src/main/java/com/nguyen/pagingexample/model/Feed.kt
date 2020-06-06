package com.nguyen.pagingexample.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Feed(
    val id: Long? = null,
    val status: String? = null,
    val totalResults: Long? = null,
    val articles: List<Article>? = null
) : Parcelable