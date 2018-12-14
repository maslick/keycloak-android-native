package io.maslick.keycloaker

object Config {
    const val clientId = "barkoder-frontend"
    const val baseUrl = "https://activeclouder.ijs.si/auth/realms/barkoder/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "https://maslick.io/barkoder"
}