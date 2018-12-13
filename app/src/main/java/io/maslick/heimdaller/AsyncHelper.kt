package io.maslick.heimdaller

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

object AsyncHelper {
    @SuppressLint("CheckResult")
    fun <T> asyncRxExecutor(heavyFunction: () -> T, response : (response : T?) -> Unit) {
        val observable = Single.create<T> { e ->
            e.onSuccess(heavyFunction())
        }
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t: T? ->
                response(t)
            }
    }

    inline fun uiThreadExecutor(crossinline block: () -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post{
            block()
        }
    }
}