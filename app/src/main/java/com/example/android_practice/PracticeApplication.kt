package com.example.android_practice

import android.app.Application

class PracticeApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
