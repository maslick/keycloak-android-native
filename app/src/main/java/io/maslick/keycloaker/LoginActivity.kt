package io.maslick.keycloaker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.HttpUrl
import timber.log.Timber

class LoginActivity : RxAppCompatActivity() {

    val clientId = "barkoder-frontend"
    val redirectUri = "https://maslick.io/barkoder"
    val authCodeUrl = Uri.parse("${Downloader.baseUrl}/auth")
        .buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("response_type", "code")
        .build()
        .toString()

    lateinit var api: IKeycloakRest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        api = Downloader.retrofit.create(IKeycloakRest::class.java)
        initTimber()
        initAuth()
    }

    private fun initTimber() {
        val tree = Timber.DebugTree()
        Timber.plant(tree)
    }

    private fun initAuth() {
        val ua = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0"
        webView.settings.userAgentString = ua
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("CheckResult")
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url.startsWith(redirectUri)) {
                    AsyncHelper.uiThreadExecutor { webView.visibility = View.GONE }
                    api.grantNewAccessToken(HttpUrl.parse(url)!!.queryParameter("code")!!, clientId, redirectUri, "authorization_code")
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
}
