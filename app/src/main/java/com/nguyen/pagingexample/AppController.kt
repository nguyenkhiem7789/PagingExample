package com.nguyen.pagingexample

import android.app.Application
import com.nguyen.pagingexample.rest.RestApi
import com.nguyen.pagingexample.rest.RestApiFactory
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

public class AppController : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: AppController? = null

        private fun applicationContext() : AppController {
            return instance as AppController
        }

        val restApi: RestApi by lazy {
            RestApiFactory.create()
        }

        val scheduler: Scheduler by lazy {
            Schedulers.io()
        }

    }

}