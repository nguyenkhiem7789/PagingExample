package com.nguyen.pagingexample.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Source(val name: String? = null) : Parcelable