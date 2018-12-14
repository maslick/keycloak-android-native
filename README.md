# =keycloak android native=
native Android client for Keycloak

## Features
* gets an OAuth2.0 token via the ``Authorization Code Grant`` flow (public client)
* uses ``WebView`` as opposed to using the browser, intents and URI schemas.
* stores the access token in the ``SharedPreferences``
* checks if the stored access token is expired and automatically refreshes it
