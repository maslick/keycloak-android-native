package io.maslick.keycloaker.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import io.maslick.keycloaker.di.KeycloakToken
import io.reactivex.Single

interface IOAuth2AccessTokenStorage {
    fun getStoredAccessToken(): Single<KeycloakToken>
    fun storeAccessToken(token: KeycloakToken)
    fun hasAccessToken(): Single<Boolean>
    fun removeAccessToken()
}

class SharedPreferencesOAuth2Storage(val prefs: SharedPreferences, val gson: Gson) : IOAuth2AccessTokenStorage {
    val ACCESS_TOKEN_PREFERENCES_KEY = "OAuth2AccessToken"

    override fun getStoredAccessToken(): Single<KeycloakToken> {
        return Single.just(prefs.getString(ACCESS_TOKEN_PREFERENCES_KEY, null))
            .map { gson.fromJson(it, KeycloakToken::class.java) }
    }

    override fun storeAccessToken(token: KeycloakToken) {
        prefs.edit()
            .putString(ACCESS_TOKEN_PREFERENCES_KEY, gson.toJson(token))
            .apply()
    }

    override fun hasAccessToken(): Single<Boolean> {
        return Single.just(prefs.contains(ACCESS_TOKEN_PREFERENCES_KEY))
    }

    override fun removeAccessToken() {
        prefs.edit()
            .remove(ACCESS_TOKEN_PREFERENCES_KEY)
            .apply()
    }
}
