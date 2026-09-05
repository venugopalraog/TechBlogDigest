package com.engblog.db

import com.engblog.ingestion.FEED_SOURCES
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val url = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/engblog"
        val user = System.getenv("DATABASE_USER") ?: "postgres"
        val password = System.getenv("DATABASE_PASSWORD") ?: "postgres"

        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.postgresql.Driver"
            username = user
            this.password = password
            maximumPoolSize = 5
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Sources, Posts, Users)
            FEED_SOURCES.forEach { source ->
                val exists = Sources.selectAll().where { Sources.id eq source.id }.any()
                if (!exists) {
                    Sources.insert {
                        it[id] = source.id
                        it[name] = source.name
                        it[feedUrl] = source.feedUrl
                        it[siteUrl] = source.siteUrl
                    }
                }
            }
        }
    }
}
