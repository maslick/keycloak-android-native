package io.maslick.keycloaker.token

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import io.maslick.keycloaker.Config
import io.maslick.keycloaker.di.IKeycloakRest
import io.maslick.keycloaker.di.KeycloakToken
import io.maslick.keycloaker.helper.Helper.formatDate
import io.maslick.keycloaker.storage.IOAuth2AccessTokenStorage
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

class RefreshTokenWorker(context: Context, params: WorkerParameters): Worker(context, params), KoinComponent {

    companion object {
        fun startPeriodicRefreshTokenTask() {
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork = PeriodicWorkRequest.Builder(RefreshTokenWorker::class.java, 15, MINUTES)
                .setConstraints(workConstraints)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork("kc-refresh-token-work", REPLACE, periodicWork)
        }

        const val channelId = "keycloaker_channel_id"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "keycloaker notification channel", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val api: IKeycloakRest by inject()
    private val storage: IOAuth2AccessTokenStorage by inject()

    override fun doWork(): Result {
        val notificationId = System.currentTimeMillis().toInt()
        triggerTokenNotification(notificationId)
        if (storage.getStoredAccessToken() == null || storage.getStoredAccessToken()!!.refreshToken == null)
            return Result.failure()

        return try {
            val token = saveTokenToStorage(api.refreshAccessToken(storage.getStoredAccessToken()!!.refreshToken!!, Config.clientId).blockingFirst())
            showOk(System.currentTimeMillis().toInt(), "Token is valid until: ${token.tokenExpirationDate!!.formatDate()}")
            showOk(notificationId, "Refresh token is valid until: ${token.refreshTokenExpirationDate!!.formatDate()}")
            Result.success()
        } catch (e: Exception) {
            showError(notificationId)
            Result.retry()
        }
    }

    private fun triggerTokenNotification(id: Int) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentTitle("Keycloaker")
            .setContentText("Updating token using refresh token...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
    }

    private fun showOk(id: Int, message: String) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentTitle("Keycloaker")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.GREEN)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
    }

    private fun showError(id: Int) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentTitle("Keycloaker")
            .setContentText("Token refresh failed :(")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.RED)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
    }

    private fun saveTokenToStorage(token: KeycloakToken): KeycloakToken {
        val expirationDate = Calendar.getInstance().clone() as Calendar
        val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
        expirationDate.add(Calendar.SECOND, token.expiresIn!!)
        refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
        token.tokenExpirationDate = expirationDate
        token.refreshTokenExpirationDate = refreshExpirationDate
        storage.storeAccessToken(token)
        return token
    }
}