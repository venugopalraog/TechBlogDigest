package com.engblog.db

import com.engblog.model.Post
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object PostRepository {

    /** Inserts the post if its link isn't already stored. Returns true if inserted. */
    fun insertIfNew(
        sourceId: String,
        title: String,
        link: String,
        publishedAt: LocalDateTime,
        summary: String?,
        tags: List<String>,
    ): Boolean = transaction {
        val exists = Posts.selectAll().where { Posts.link eq link }.any()
        if (exists) return@transaction false

        Posts.insert {
            it[Posts.sourceId] = sourceId
            it[Posts.title] = title
            it[Posts.link] = link
            it[Posts.publishedAt] = publishedAt
            it[Posts.summary] = summary
            it[Posts.tags] = tags.joinToString(",")
        }
        true
    }

    fun feed(tagFilter: String? = null, limit: Int = 50): List<Post> = transaction {
        val query = (Posts innerJoin Sources).selectAll()
        val rows = if (tagFilter != null) {
            query.andWhere { Posts.tags like "%$tagFilter%" }
        } else {
            query
        }

        rows
            .orderBy(Posts.publishedAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                Post(
                    id = row[Posts.id],
                    sourceId = row[Posts.sourceId],
                    sourceName = row[Sources.name],
                    title = row[Posts.title],
                    link = row[Posts.link],
                    publishedAt = row[Posts.publishedAt].atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    summary = row[Posts.summary],
                    tags = row[Posts.tags].split(",").filter { it.isNotBlank() },
                )
            }
    }
}
