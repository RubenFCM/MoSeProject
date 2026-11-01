package com.example.moseproject.data.watchlist

import java.time.LocalDate

enum class MediaType(val value: String) { Movie("movie"), Tv("tv") }
data class MediaKey(val type: MediaType, val tmdbId: Int) {
    init { require(tmdbId > 0) }
    val documentId get() = "${type.value}_$tmdbId"
}
enum class WatchStatus(val value: String) { Pending("pending"), InProgress("in_progress"), Watched("watched") }
enum class WatchSort(val field: String) { Added("createdAt"), Updated("updatedAt") }
data class WatchFilter(val type: MediaType? = null, val status: WatchStatus? = null, val sort: WatchSort = WatchSort.Added)

data class Provider(val id: Int, val name: String, val logoPath: String?)
data class Availability(
    val region: String = "ES", val link: String? = null,
    val subscription: List<Provider> = emptyList(), val free: List<Provider> = emptyList(),
    val ads: List<Provider> = emptyList(), val rent: List<Provider> = emptyList(), val buy: List<Provider> = emptyList(),
    val checkedAtMillis: Long? = null
)
data class CatalogMetadata(
    val key: MediaKey, val title: String, val posterPath: String?, val releaseDate: String?,
    val runtimeMinutes: Int?, val availability: Availability? = null
)
data class Episode(
    val id: Int, val season: Int, val number: Int, val title: String?, val airDate: String?, val runtimeMinutes: Int?
) {
    fun aired(today: LocalDate): Boolean = airDate?.let { runCatching { LocalDate.parse(it) <= today }.getOrDefault(false) } ?: false
    fun selectable(today: LocalDate) = airDate == null || aired(today)
}
data class Season(val number: Int, val title: String?, val airDate: String?)
data class EpisodePosition(val episode: Episode, val minute: Int?, val nextToWatch: Boolean = false)

sealed interface WatchProgress {
    data class Movie(val minute: Int? = null) : WatchProgress
    data class Series(val current: EpisodePosition? = null, val lastCompleted: Episode? = null) : WatchProgress
}
data class WatchEntry(
    val catalog: CatalogMetadata, val status: WatchStatus, val progress: WatchProgress,
    val progressVersion: Long = 0, val createdAtMillis: Long? = null, val updatedAtMillis: Long? = null
)
data class PageCursor(val seconds: Long, val nanoseconds: Int, val documentId: String)
data class WatchPage(val entries: List<WatchEntry>, val cursor: PageCursor?, val hasMore: Boolean)
enum class WatchFailure { Authentication, Configuration, Network, Permission, Conflict, Validation, Unknown }
class WatchException(val reason: WatchFailure) : Exception()

object ProgressPolicy {
    fun movie(status: WatchStatus, minute: Int?, runtime: Int?): Pair<WatchStatus, WatchProgress.Movie> {
        require(minute == null || minute in 0..100_000)
        require(runtime == null || minute == null || minute <= runtime)
        if (status == WatchStatus.Pending || status == WatchStatus.Watched) return status to WatchProgress.Movie()
        return when {
            minute == 0 -> WatchStatus.Pending to WatchProgress.Movie()
            runtime != null && minute == runtime -> WatchStatus.Watched to WatchProgress.Movie(minute)
            else -> WatchStatus.InProgress to WatchProgress.Movie(minute)
        }
    }

    fun series(status: WatchStatus, episode: Episode?, minute: Int?, lastCompleted: Episode? = null): Pair<WatchStatus, WatchProgress.Series> {
        if (status != WatchStatus.InProgress) return status to WatchProgress.Series()
        require(episode != null)
        validateEpisode(episode, minute)
        return status to WatchProgress.Series(EpisodePosition(episode, minute), lastCompleted)
    }

    fun finishedEpisode(episode: Episode, next: Episode?): Pair<WatchStatus, WatchProgress.Series> {
        validateEpisode(episode, null)
        next?.let { validateEpisode(it, 0) }
        // Completing an episode never marks the entire show watched.
        return WatchStatus.InProgress to WatchProgress.Series(next?.let { EpisodePosition(it, 0, true) }, episode)
    }

    fun validateEpisode(episode: Episode, minute: Int?) {
        require(episode.id > 0 && episode.season in 0..1000 && episode.number in 1..100_000)
        require(minute == null || minute in 0..100_000)
        require(episode.runtimeMinutes == null || minute == null || minute <= episode.runtimeMinutes)
    }

    fun validate(entry: WatchEntry) {
        require(entry.catalog.title.isNotBlank() && entry.catalog.title.length <= 500)
        require(entry.catalog.runtimeMinutes == null || entry.catalog.runtimeMinutes in 1..100_000)
        when (val progress = entry.progress) {
            is WatchProgress.Movie -> {
                require(entry.catalog.key.type == MediaType.Movie)
                val minute = progress.minute
                require(minute == null || minute in 1..100_000)
                require(minute == null || entry.catalog.runtimeMinutes == null || minute <= entry.catalog.runtimeMinutes)
                require(entry.status != WatchStatus.Pending || minute == null)
                require(entry.status != WatchStatus.InProgress || minute == null || minute != entry.catalog.runtimeMinutes)
                require(entry.status != WatchStatus.Watched || minute == null || minute == entry.catalog.runtimeMinutes)
            }
            is WatchProgress.Series -> {
                require(entry.catalog.key.type == MediaType.Tv && entry.catalog.runtimeMinutes == null)
                progress.current?.let { validateEpisode(it.episode, it.minute) }
                progress.lastCompleted?.let { validateEpisode(it, null) }
                require(entry.status == WatchStatus.InProgress || (progress.current == null && progress.lastCompleted == null))
                require(entry.status != WatchStatus.InProgress || progress.current != null || progress.lastCompleted != null)
                require(progress.current?.nextToWatch != true || (progress.current.minute == 0 && progress.lastCompleted != null))
            }
        }
    }
}
