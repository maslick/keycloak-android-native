# =keycloak android native=
native Android client for Keycloak

## Features
* gets the OAuth2.0 access token via the ``Authorization Code`` flow (access type: ``public client``)
* uses Browser authentication
* stores the access token in the ``SharedPreferences``
* checks if the stored access token is expired and periodically refreshes it (via ``WorkManager``)
* uses [Koin](https://github.com/InsertKoinIO/koin) as the dependency injection framework
* Proguard configuration for the latest versions of ``RxJava2``, ``retrofit2``, ``okHttp3``


## Usage
Edit the file ``Config.kt`` according to your setup:
```kt
object Config {
    const val clientId = "myClientID"
    const val baseUrl = "https:/[KEYCLOAK-URL]/auth/realms/[REALM]/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "maslick://oauthresponse"
}
```


Edit ``AndroidManifest.xml`` and add your ``redirectUri`` to the ``LoginActivity`` intent-filter:
```
<data android:scheme="maslick" android:host="oauthresponse"/>
```
