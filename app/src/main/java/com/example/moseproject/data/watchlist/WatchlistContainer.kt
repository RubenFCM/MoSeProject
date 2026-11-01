package com.example.moseproject.data.watchlist

import android.content.Context
import com.example.moseproject.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

object WatchlistContainer {
    fun create(context: Context): WatchlistRepository {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(context)
            ?: return UnconfiguredWatchlistRepository()
        val db = FirebaseFirestore.getInstance(app)
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        if (BuildConfig.DEBUG && BuildConfig.AUTH_USE_EMULATOR) db.useEmulator("10.0.2.2", 8080)
        return FirestoreWatchlistRepository(db, FirebaseAuth.getInstance(app))
    }
}

private class UnconfiguredWatchlistRepository : WatchlistRepository {
    private fun missing(): Nothing = throw WatchException(WatchFailure.Configuration)
    override suspend fun page(uid: String, filter: WatchFilter, after: PageCursor?): WatchPage = missing()
    override suspend fun get(uid: String, key: MediaKey): WatchEntry? = missing()
    override suspend fun add(uid: String, metadata: CatalogMetadata): WatchEntry = missing()
    override suspend fun saveProgress(uid: String, entry: WatchEntry, status: WatchStatus, progress: WatchProgress): WatchEntry = missing()
    override suspend fun refreshProviders(uid: String, key: MediaKey, availability: Availability): WatchEntry = missing()
    override suspend fun delete(uid: String, key: MediaKey): Unit = missing()
}
