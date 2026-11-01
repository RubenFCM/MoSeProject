package com.example.moseproject

import android.app.Application
import com.example.moseproject.data.auth.AuthContainer
import com.example.moseproject.data.watchlist.WatchlistContainer
import com.example.moseproject.data.watchlist.TmdbWatchlistCatalog
import com.example.moseproject.data.RetrofitServiceFactory

class CineVerseApplication : Application() {
    // One SDK/repository instance per process; emulator configuration runs before its first use.
    val authRepository by lazy { AuthContainer.createRepository(this) }
    val watchlistRepository by lazy { WatchlistContainer.create(this) }
    val watchlistCatalog by lazy { TmdbWatchlistCatalog(RetrofitServiceFactory.makeRetrofitServiceFactory()) }
}
