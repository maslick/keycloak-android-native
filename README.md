# =keycloak android native=
native Android client for Keycloak

## Features
* gets an OAuth2.0 token via the ``Authorization Code`` flow (access type: ``public client``)
* uses ``WebView`` as opposed to using the browser or Chrome Custom Tabs
* stores the access token in the ``SharedPreferences``
* checks if the stored access token is expired and automatically refreshes it
* Proguard configuration for the latest versions of ``RxJava2``, ``retrofit2``, ``okHttp3``


## Usage
Edit the file ``Config.kt`` according to your setup:
```kt
object Config {
    const val clientId = "myClientID"
    const val baseUrl = "https:/[KEYCLOAK-URL]/auth/realms/[REALM]/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "https://any-https-address.si"
}

```
