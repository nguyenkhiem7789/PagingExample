package com.nguyen.pagingexample.utils

class NetworkState(
    val status: Status,
    val msg: String
) {
    enum class Status {
        RUNNING,
        SUCCESS,
        FAILED
    }

    companion object {
        var LOADED: NetworkState? = null
        var LOADING: NetworkState? = null

        init {
            LOADED = NetworkState(
                Status.SUCCESS,
                "Success"
            )
            LOADING = NetworkState(
                Status.RUNNING,
                "Running"
            )
        }
    }

}