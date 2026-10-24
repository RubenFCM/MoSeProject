package com.example.moseproject

import android.app.Application
import com.example.moseproject.data.auth.AuthContainer

class CineVerseApplication : Application() {
    // One SDK/repository instance per process; emulator configuration runs before its first use.
    val authRepository by lazy { AuthContainer.createRepository(this) }
}
