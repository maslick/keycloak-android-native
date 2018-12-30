package io.maslick.keycloaker.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.keycloaker.Config
import io.maslick.keycloaker.R
import io.maslick.keycloaker.di.IKeycloakRest
import io.maslick.keycloaker.helper.Helper.formatDate
import io.maslick.keycloaker.helper.Helper.isRefreshTokenExpired
import io.maslick.keycloaker.helper.Helper.parseJwtToken
import io.maslick.keycloaker.storage.IOAuth2AccessTokenStorage
import io.maslick.keycloaker.token.RefreshTokenWorker.Companion.startPeriodicRefreshTokenTask
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : RxAppCompatActivity() {

    val api by inject<IKeycloakRest>()
    val storage by inject<IOAuth2AccessTokenStorage>()

    private val AUTHORIZATION_REQUEST_CODE = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startPeriodicRefreshTokenTask()
        swiper?.apply {
            setOnRefreshListener {
                isRefreshing = true
                showData()
                isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRefreshTokenExpired(storage.getStoredAccessToken()))
            startActivityForResult(Intent(this, LoginActivity::class.java), AUTHORIZATION_REQUEST_CODE)
        showData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_list, menu)
        return true
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.logout -> handleLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {}

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun showData() {
        val token = storage.getStoredAccessToken()

        token?.apply {
            val principal = parseJwtToken(accessToken!!)
            text.movementMethod = ScrollingMovementMethod()
            text.text =
                    "user: ${principal.name} ${principal.surname} (${principal.email})\n" +
                    "id: ${principal.userId}\n" +
                    "available roles: ${principal.roles.joinToString(", ")}\n\n" +
                    "token expires in: $expiresIn sec (${tokenExpirationDate!!.formatDate()})\n" +
                    "refresh expires in: $refreshExpiresIn sec (${refreshTokenExpirationDate!!.formatDate()})\n\n" +
                    "token: $accessToken\n\n" +
                    "refreshToken: $refreshToken"
        }

    }

    @SuppressLint("CheckResult")
    private fun handleLogout() {
        val refreshToken = storage.getStoredAccessToken()!!.refreshToken!!
        api.logout(Config.clientId, refreshToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(this, "logged out :)", Toast.LENGTH_LONG).show()
                storage.removeAccessToken()
                this@MainActivity.startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }, {
                it.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            })
    }
}
