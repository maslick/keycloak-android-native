package io.maslick.heimdaller

import com.google.gson.GsonBuilder
import de.rheinfabrik.heimdall.OAuth2AccessToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable
import timber.log.Timber


object Downloader {
    const val baseUrl = "https://activeclouder.ijs.si/auth/realms/barkoder/protocol/openid-connect"

    val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Timber.i(it) })
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    val okHttpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(interceptor)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("$baseUrl/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()
}

interface IKeycloakRest {
    @POST("token")
    @FormUrlEncoded
    fun grantNewAccessToken(
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("redirect_uri") uri: String,
        @Field("grant_type") grantType: String
    ): Observable<OAuth2AccessToken>

    @POST("token")
    @FormUrlEncoded
    fun refreshAccessToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): Observable<OAuth2AccessToken>
}