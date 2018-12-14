package io.maslick.keycloaker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.HttpUrl
import org.koin.android.ext.android.inject
import timber.log.Timber


class LoginActivity : RxAppCompatActivity() {

    val api by inject<IKeycloakRest>()

    val clientId = "barkoder-frontend"
    val redirectUri = "https://maslick.io/barkoder"
    val authCodeUrl = Uri.parse("${Downloader.baseUrl}/auth")
        .buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("response_type", "code")
        .build()
        .toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initTimber()
        initAuth()
    }

    private fun initTimber() {
        val tree = Timber.DebugTree()
        Timber.plant(tree)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initAuth() {
        webView.settings.userAgentString = "Barkoder/0.1 Android app"
        webView.settings.javaScriptEnabled = true
        clearCookies()
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("CheckResult")
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url.startsWith(redirectUri)) {
                    AsyncHelper.uiThreadExecutor { webView.visibility = View.GONE }
                    api.grantNewAccessToken(HttpUrl.parse(url)!!.queryParameter("code")!!, clientId, redirectUri)
                        .subscribeOn(Schedulers.io())
                        .compose(bindToLifecycle())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("token", it.accessToken)
                            intent.putExtra("refreshToken", it.refreshToken)
                            intent.putExtra("expiresIn", it.expiresIn)
                            intent.putExtra("refreshExpiresIn", it.refreshExpiresIn)
                            this@LoginActivity.startActivity(intent)
                            println(it)
                        }, {
                            it.printStackTrace()
                            Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        })
                }
            }
        }

        webView.loadUrl(authCodeUrl)
    }

    private fun clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

    override fun onBackPressed() {
        finish()
        startActivity(intent)
    }
}
