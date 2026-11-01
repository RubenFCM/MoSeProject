package com.example.moseproject.data.watchlist

interface WatchlistRepository {
    suspend fun page(uid: String, filter: WatchFilter, after: PageCursor?): WatchPage
    suspend fun get(uid: String, key: MediaKey): WatchEntry?
    suspend fun add(uid: String, metadata: CatalogMetadata): WatchEntry
    suspend fun saveProgress(uid: String, entry: WatchEntry, status: WatchStatus, progress: WatchProgress): WatchEntry
    suspend fun refreshProviders(uid: String, key: MediaKey, availability: Availability): WatchEntry
    suspend fun delete(uid: String, key: MediaKey)
}
