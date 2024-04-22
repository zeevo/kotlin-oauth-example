package com.example.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureSecurity() {

    authentication {
            oauth("auth-oauth-github") {
                urlProvider = { "http://localhost:8080/auth/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "github",
                        authorizeUrl = "https://github.com/login/oauth/authorize",
                        accessTokenUrl = "https://github.com/login/oauth/access_token",
                        requestMethod = HttpMethod.Post,
                        clientId = System.getenv("GITHUB_CLIENT_ID"),
                        clientSecret = System.getenv("GITHUB_CLIENT_SECRET"),
                    )
                }
                client = HttpClient(Apache)
            }
        }
    routing {
        authenticate("auth-oauth-github") {
                    get("login") {
                        call.respondRedirect("/callback")
                    }
        
                    get("/auth/callback") {
                        val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                        call.sessions.set(UserSession(principal?.accessToken.toString()))
                        call.respondRedirect("/")
                    }
                }
    }
}

data class UserSession(val accessToken: String)
