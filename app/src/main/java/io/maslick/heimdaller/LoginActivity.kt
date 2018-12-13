package io.maslick.heimdaller

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.HttpUrl
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
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
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url.startsWith(redirectUri)) {
                    AsyncHelper.uiThreadExecutor { webView.visibility = View.GONE }
                    api.grantNewAccessToken(HttpUrl.parse(url)!!.queryParameter("code")!!, clientId, redirectUri, "authorization_code")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            println(it)
                            Toast.makeText(this@LoginActivity, it.tokenType, Toast.LENGTH_LONG).show()
                        }
                }
            }
        }


        webView.loadUrl(authCodeUrl)

    }
}
