package com.engblog

import com.engblog.db.PostRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        // GET /feed?tags=...&limit=...
        get("/feed") {
            val tag = call.request.queryParameters["tags"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val posts = PostRepository.feed(tagFilter = tag, limit = limit)
            call.respond(posts)
        }

        get("/post/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                return@get
            }
            val post = PostRepository.feed(limit = 1000).firstOrNull { it.id == id }
            if (post == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(post)
            }
        }
    }
}
