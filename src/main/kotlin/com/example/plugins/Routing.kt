package com.example.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

fun HTML.layout(children: DIV.() -> Unit) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        script(src = "https://cdn.tailwindcss.com") {}
    }
    body {
        classes = setOf("mx-4", "flex", "min-h-screen", "flex-col", "py-8", "bg-slate-950", "text-slate-100")
        div {
            classes = setOf("mx-auto", "max-w-2xl", "flex", "flex-col", "w-full", "gap-2")
            children()
        }
    }
}

val btn = setOf("font-bold", "uppercase p-2", "rounded", "text-slate-100", "bg-slate-800", "hover:bg-slate-700")

@Serializable
data class GitHubUser(
    val login: String,
    val id: Int,
    @SerialName("node_id") val nodeId: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("gravatar_id") val gravatarId: String,
    val url: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("followers_url") val followersUrl: String,
    @SerialName("following_url") val followingUrl: String,
    @SerialName("gists_url") val gistsUrl: String,
    @SerialName("starred_url") val starredUrl: String,
    @SerialName("subscriptions_url") val subscriptionsUrl: String,
    @SerialName("organizations_url") val organizationsUrl: String,
    @SerialName("repos_url") val reposUrl: String,
    @SerialName("events_url") val eventsUrl: String,
    @SerialName("received_events_url") val receivedEventsUrl: String,
    val type: String,
    @SerialName("site_admin") val siteAdmin: Boolean,
    val name: String?,
    val company: String?,
    val blog: String?,
    val location: String?,
    val email: String?,
    val hireable: Boolean?,
    val bio: String?,
    @SerialName("twitter_username") val twitterUsername: String?,
    @SerialName("public_repos") val publicRepos: Int,
    @SerialName("public_gists") val publicGists: Int,
    val followers: Int,
    val following: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

suspend fun getUser(httpClient: HttpClient, call: ApplicationCall): GitHubUser? {
    val accessToken = call.sessions.get<UserSession>()?.accessToken
    if (accessToken != null) {
        return httpClient.get("https://api.github.com/user") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${accessToken}")
            }
        }.body<GitHubUser>()
    }

    return null;
}

fun Application.configureRouting() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.httpOnly = true
        }
    }
    routing {
        get("/") {
            call.application.environment.log.info(call.sessions.get<UserSession>()?.toString())
            val thing = getUser(applicationHttpClient, call)
            call.respondHtml {
                layout() {
                    if (call.sessions.get("user_session") != null) {
                        a {
                            classes = btn
                            href = "/logout"
                            +"logout"
                        }
                    } else {
                        a {
                            classes = btn
                            href = "/login"
                            +"login"
                        }
                    }
                    if (thing != null) {
                        hr {
                            classes = setOf("border-slate-500")
                        }
                        pre {
                            code {
                                +Json {
                                    prettyPrint = true
                                }.encodeToString(thing)
                            }
                        }
                    }
                }
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }
    }
}
