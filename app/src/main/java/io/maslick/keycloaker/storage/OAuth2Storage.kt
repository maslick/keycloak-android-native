package io.maslick.keycloaker.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import io.maslick.keycloaker.di.KeycloakToken

interface IOAuth2AccessTokenStorage {
    fun getStoredAccessToken(): KeycloakToken?
    fun storeAccessToken(token: KeycloakToken)
    fun hasAccessToken(): Boolean
    fun removeAccessToken()
}

class SharedPreferencesOAuth2Storage(val prefs: SharedPreferences, val gson: Gson) : IOAuth2AccessTokenStorage {
    val ACCESS_TOKEN_PREFERENCES_KEY = "OAuth2AccessToken"

    override fun getStoredAccessToken(): KeycloakToken? {
        val tokenStr = prefs.getString(ACCESS_TOKEN_PREFERENCES_KEY, null)
        return if (tokenStr == null) null
        else gson.fromJson(tokenStr, KeycloakToken::class.java)
    }

    override fun storeAccessToken(token: KeycloakToken) {
        prefs.edit()
            .putString(ACCESS_TOKEN_PREFERENCES_KEY, gson.toJson(token))
            .apply()
    }

    override fun hasAccessToken(): Boolean {
        return prefs.contains(ACCESS_TOKEN_PREFERENCES_KEY)
    }

    override fun removeAccessToken() {
        prefs.edit()
            .remove(ACCESS_TOKEN_PREFERENCES_KEY)
            .apply()
    }
}
