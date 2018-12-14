package io.maslick.keycloaker

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

val sharedPrefsModule = module {
    fun prefs(context: Context) = context.getSharedPreferences("barkoder", Context.MODE_PRIVATE)
    single { prefs(get()) }
}

val apiModule = module {
    single {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { mess -> Timber.i(mess) })
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("${Downloader.baseUrl}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(IKeycloakRest::class.java)
    }
}