package io.maslick.keycloaker

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


object Downloader {
    const val baseUrl = "https://activeclouder.ijs.si/auth/realms/barkoder/protocol/openid-connect"
}

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
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Completable
}

data class KeycloakToken(
    @SerializedName("access_token")       var accessToken: String? = null,
    @SerializedName("expires_in")         var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in") var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token")      var refreshToken: String? = null,
    @SerializedName("token_type")         val tokenType: String? = null,
    @SerializedName("id_token")           val idToken: String? = null,
    @SerializedName("not-before-policy")  val notBeforePolicy: Int? = null,
    @SerializedName("session_state")      val sessionState: String? = null
)