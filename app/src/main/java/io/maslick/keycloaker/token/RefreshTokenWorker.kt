package io.maslick.keycloaker.token

import android.content.Context
import androidx.work.*
import io.maslick.keycloaker.Config
import io.maslick.keycloaker.di.IKeycloakRest
import io.maslick.keycloaker.di.KeycloakToken
import io.maslick.keycloaker.storage.IOAuth2AccessTokenStorage
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(appContext: Context, workerParameters: WorkerParameters): Worker(appContext, workerParameters),
    KoinComponent {

    companion object {
        fun startPeriodicRefreshTokenTask() {
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRefreshTokenWork = PeriodicWorkRequest.Builder(RefreshTokenWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(workConstraints)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork("kc-refresh-token-work",
                ExistingPeriodicWorkPolicy.REPLACE, periodicRefreshTokenWork)
        }
    }

    override fun doWork(): Result {
        val api: IKeycloakRest by inject()
        val storage: IOAuth2AccessTokenStorage by inject()

        if (storage.getStoredAccessToken() == null || storage.getStoredAccessToken()!!.refreshToken == null)
            return Result.failure()

        return try {
            val token = api.refreshAccessToken(storage.getStoredAccessToken()!!.refreshToken!!, Config.clientId).blockingFirst()
            saveTokenToStorage(token, storage)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun saveTokenToStorage(token: KeycloakToken, storage: IOAuth2AccessTokenStorage) {
        val expirationDate = Calendar.getInstance().clone() as Calendar
        val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
        expirationDate.add(Calendar.SECOND, token.expiresIn!!)
        refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
        token.tokenExpirationDate = expirationDate
        token.refreshTokenExpirationDate = refreshExpirationDate
        storage.storeAccessToken(token)
    }
}