package io.maslick.keycloaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.keycloaker.Config.clientId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : RxAppCompatActivity() {

    lateinit var token: String
    lateinit var refreshToken: String
    val api by inject<IKeycloakRest>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        token = intent.getStringExtra("token")
        refreshToken = intent.getStringExtra("refreshToken")
        val expiresIn = intent.getIntExtra("expiresIn", -1)
        val refreshExpiresIn = intent.getIntExtra("refreshExpiresIn", -1)


        text.movementMethod = ScrollingMovementMethod()
        text.text =
                "token: $token\n\n" +
                "refreshToken: $refreshToken\n\n" +
                "expires in: $expiresIn\n\n" +
                "refresh expires in: $refreshExpiresIn"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_list, menu)
        return true
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.logout -> handleLogout()
            R.id.refreshToken -> handleRefreshToken()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("CheckResult")
    private fun handleLogout() {
        api.logout("barkoder-frontend", refreshToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(this, "logged out :)", Toast.LENGTH_LONG).show()
                this@MainActivity.startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }, {
                it.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            })
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun handleRefreshToken() {
        api.refreshAccessToken(refreshToken, clientId)
            .compose(bindToLifecycle())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { tok ->
                token = tok.accessToken!!
                refreshToken = tok.refreshToken!!
                text.text =
                        "token: $token\n\n" +
                        "refreshToken: $refreshToken\n\n" +
                        "expires in: ${tok.expiresIn}\n\n" +
                        "refresh expires in: ${tok.refreshExpiresIn}"
            }
    }

    override fun onBackPressed() {}
}
