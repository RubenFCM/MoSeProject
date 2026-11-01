package com.example.moseproject.data.watchlist

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

internal fun Episode.toDocument(): Map<String, Any?> = mapOf("id" to id, "season" to season, "number" to number,
    "title" to title, "airDate" to airDate, "runtimeMinutes" to runtimeMinutes)

internal fun WatchProgress.toDocument(): Map<String, Any?> = when (this) {
    is WatchProgress.Movie -> mapOf("kind" to "movie", "unit" to "minutes", "positionMinutes" to minute)
    is WatchProgress.Series -> mapOf("kind" to "tv", "unit" to "minutes", "current" to current?.let {
        mapOf("episode" to it.episode.toDocument(), "positionMinutes" to it.minute, "mode" to if (it.nextToWatch) "next" else "watching")
    }, "lastCompleted" to lastCompleted?.toDocument())
}

internal fun Availability.toDocument(): Map<String, Any?> {
    fun List<Provider>.encode() = map { mapOf("id" to it.id, "name" to it.name, "logoPath" to it.logoPath) }
    return mapOf("region" to region, "link" to link, "subscription" to subscription.encode(), "free" to free.encode(),
        "ads" to ads.encode(), "rent" to rent.encode(), "buy" to buy.encode(), "checkedAt" to FieldValue.serverTimestamp())
}

internal fun CatalogMetadata.toDocument(): Map<String, Any?> = mapOf("title" to title, "posterPath" to posterPath,
    "releaseDate" to releaseDate, "runtimeMinutes" to runtimeMinutes, "providers" to availability?.toDocument())

internal fun DocumentSnapshot.watchEntry(): WatchEntry {
    fun invalid(): Nothing = throw WatchException(WatchFailure.Validation)
    val data = data ?: invalid()
    val type = MediaType.entries.firstOrNull { it.value == data["mediaType"] } ?: invalid()
    val key = MediaKey(type, (data["tmdbId"] as? Long)?.toInt() ?: invalid())
    if (id != key.documentId) invalid()
    fun Any?.obj(): Map<*, *> = this as? Map<*, *> ?: invalid()
    fun Map<*, *>.integer(name: String) = (get(name) as? Number)?.toInt()
    fun Map<*, *>.string(name: String) = get(name) as? String
    fun Map<*, *>.episode(): Episode = Episode(integer("id") ?: invalid(), integer("season") ?: invalid(),
        integer("number") ?: invalid(), string("title"), string("airDate"), integer("runtimeMinutes"))
    val catalog = data["catalog"].obj()
    val availability = catalog["providers"]?.obj()?.let { providers ->
        fun list(name: String) = (providers[name] as? List<*>)?.map { value ->
            val p = value.obj()
            Provider(p.integer("id") ?: invalid(), p.string("name") ?: invalid(), p.string("logoPath"))
        } ?: invalid()
        Availability(providers.string("region") ?: invalid(), providers.string("link"), list("subscription"), list("free"),
            list("ads"), list("rent"), list("buy"), (providers["checkedAt"] as? Timestamp)?.toDate()?.time)
    }
    val metadata = CatalogMetadata(key, catalog.string("title") ?: invalid(), catalog.string("posterPath"), catalog.string("releaseDate"),
        catalog.integer("runtimeMinutes"), availability)
    val p = data["progress"].obj()
    val progress = if (type == MediaType.Movie) WatchProgress.Movie(p.integer("positionMinutes")) else WatchProgress.Series(
        p["current"]?.obj()?.let { EpisodePosition(it["episode"].obj().episode(), it.integer("positionMinutes"), it["mode"] == "next") },
        p["lastCompleted"]?.obj()?.episode())
    return WatchEntry(metadata, WatchStatus.entries.firstOrNull { it.value == data["status"] } ?: invalid(), progress,
        (data["progressVersion"] as? Long) ?: invalid(), getTimestamp("createdAt")?.toDate()?.time,
        getTimestamp("updatedAt")?.toDate()?.time).also { ProgressPolicy.validate(it) }
}
