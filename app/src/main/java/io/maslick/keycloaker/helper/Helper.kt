package io.maslick.keycloaker.helper

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.migcomponents.migbase64.Base64
import io.maslick.keycloaker.di.KeycloakToken
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

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

object Helper {
    fun isTokenExpired(token: KeycloakToken?): Boolean {
        token?.apply {
            return this.expirationDate != null && Calendar.getInstance().after(this.expirationDate)
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    fun Calendar.formatDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return formatter.format(this.time)
    }

    fun parseJwtToken(jwtToken: String?): Array<String> {
        jwtToken?.apply {
            val splitString = split(".")
            val base64EncodedBody = splitString[1]

            val body = String(Base64.decodeFast(base64EncodedBody))
            val jsonBody = Gson().fromJson(body, JsonObject::class.java)

            val userId = jsonBody.get("sub").asString
            val email = jsonBody.get("email").asString
            val name = jsonBody.get("given_name").asString
            val surname = jsonBody.get("family_name").asString
            val roles = jsonBody.get("realm_access").asJsonObject.getAsJsonArray("roles").map {it.asString}.joinToString(", ")

            return arrayOf(userId, email, name, surname, roles)
        }
    }
}