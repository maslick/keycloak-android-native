package io.maslick.keycloaker.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.keycloaker.R
import io.maslick.keycloaker.di.IKeycloakRest
import io.maslick.keycloaker.storage.IOAuth2AccessTokenStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import io.maslick.keycloaker.helper.Helper.formatDate

class MainActivity : RxAppCompatActivity() {

    val api by inject<IKeycloakRest>()
    val storage by inject<IOAuth2AccessTokenStorage>()

    private val AUTHORIZATION_REQUEST_CODE = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHORIZATION_REQUEST_CODE && resultCode == Activity.RESULT_OK)
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

    @SuppressLint("CheckResult")
    private fun init() {
        // check token
        startActivityForResult(Intent(this, LoginActivity::class.java), AUTHORIZATION_REQUEST_CODE)
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun showData() {
        val token = storage.getStoredAccessToken()

        token?.apply {
            text.movementMethod = ScrollingMovementMethod()
            text.text =
                    "token: ${token.accessToken}\n\n" +
                    "refreshToken: ${token.refreshToken}\n\n" +
                    "expires in: ${token.expiresIn} (${token.expirationDate!!.formatDate()})\n\n" +
                    "refresh expires in: ${token.refreshExpiresIn} sec\n\n"
        }

    }

    @SuppressLint("CheckResult")
    private fun handleLogout() {
        val refreshToken = storage.getStoredAccessToken()!!.refreshToken!!
        api.logout("barkoder-frontend", refreshToken)
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
