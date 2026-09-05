package com.engblog.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Sources : Table("sources") {
    val id = varchar("id", 64)
    val name = varchar("name", 128)
    val feedUrl = varchar("feed_url", 512)
    val siteUrl = varchar("site_url", 512)

    override val primaryKey = PrimaryKey(id)
}

object Posts : Table("posts") {
    val id = integer("id").autoIncrement()
    val sourceId = varchar("source_id", 64).references(Sources.id)
    val title = varchar("title", 512)
    val link = varchar("link", 1024)
    val publishedAt = datetime("published_at")
    val summary = text("summary").nullable()
    val tags = varchar("tags", 512).default("") // comma-separated; simple v1 storage

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(link)
    }
}

object Users : Table("users") {
    val id = varchar("id", 64)
    val interestTags = varchar("interest_tags", 512).default("")
    val savedPostIds = text("saved_post_ids").default("")

    override val primaryKey = PrimaryKey(id)
}
