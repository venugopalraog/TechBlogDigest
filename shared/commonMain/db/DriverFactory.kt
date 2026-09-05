package com.engblog.shared.db

import app.cash.sqldelight.db.SqlDriver

/**
 * commonMain code never constructs this directly — the platform entry point (Android's
 * Application/Activity, iOS's AppDelegate/Swift) builds the actual instance with whatever
 * platform context it needs and hands it to shared code, so the constructor is free to
 * differ per actual implementation even though the expect declaration below has none.
 */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}
