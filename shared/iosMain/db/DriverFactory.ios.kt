package com.engblog.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

// UNTESTED — written on Windows, where Kotlin/Native can't compile Apple targets.
// This is the first thing to build and sanity-check when opening the project on a Mac.
actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AppDatabase.Schema, "engblog.db")
}
