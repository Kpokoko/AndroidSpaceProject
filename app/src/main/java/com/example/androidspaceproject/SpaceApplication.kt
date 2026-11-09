package com.example.androidspaceproject

import android.app.Application
import com.example.androidspaceproject.di.AppContainer

class SpaceApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}