package com.engblog.android

import android.app.Application
import com.engblog.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EngBlogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@EngBlogApp)
            modules(appModule)
        }
    }
}
