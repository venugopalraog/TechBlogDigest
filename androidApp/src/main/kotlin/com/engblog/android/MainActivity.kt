package com.engblog.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.engblog.android.ui.nav.EngBlogNavGraph
import com.engblog.android.ui.theme.EngBlogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngBlogTheme {
                EngBlogNavGraph()
            }
        }
    }
}
