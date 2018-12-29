package io.maslick.keycloaker.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.maslick.keycloaker.Config.baseUrl
import io.maslick.keycloaker.storage.IOAuth2AccessTokenStorage
import io.maslick.keycloaker.storage.SharedPreferencesOAuth2Storage
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

val sharedPrefsModule = module {
    fun prefs(context: Context) = context.getSharedPreferences("barkoder", Context.MODE_PRIVATE)!!
    single { prefs(get()) }
    single<IOAuth2AccessTokenStorage> { SharedPreferencesOAuth2Storage(get(), get()) }
}

val apiModule = module {
    single { GsonBuilder().setLenient().create() }
    single {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { mess -> println(mess) })
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(IKeycloakRest::class.java)
    }
}

///////////////////////////////////////////
// Helper definitions
///////////////////////////////////////////

interface IKeycloakRest {
    @POST("token")
    @FormUrlEncoded
    fun grantNewAccessToken(
        @Field("code")         code: String,
        @Field("client_id")    clientId: String,
        @Field("redirect_uri") uri: String,
        @Field("grant_type")   grantType: String = "authorization_code"
    ): Observable<KeycloakToken>

    @POST("token")
    @FormUrlEncoded
    fun refreshAccessToken(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id")     clientId: String,
        @Field("grant_type")    grantType: String = "refresh_token"
    ): Observable<KeycloakToken>

    @POST("logout")
    @FormUrlEncoded
    fun logout(
        @Field("client_id")     clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Completable
}

data class KeycloakToken(
    @SerializedName("access_token")               var accessToken: String? = null,
    @SerializedName("expires_in")                 var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in")         var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token")              var refreshToken: String? = null,
    @SerializedName("token_type")                 var tokenType: String? = null,
    @SerializedName("id_token")                   var idToken: String? = null,
    @SerializedName("not-before-policy")          var notBeforePolicy: Int? = null,
    @SerializedName("session_state")              var sessionState: String? = null,
    @SerializedName("token_expiration_date")      var tokenExpirationDate: Calendar? = null,
    @SerializedName("refresh_expiration_date")    var refreshTokenExpirationDate: Calendar? = null
)