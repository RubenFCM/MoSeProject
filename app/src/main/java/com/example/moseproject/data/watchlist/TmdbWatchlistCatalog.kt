package com.example.moseproject.data.watchlist

import com.example.moseproject.data.ApiService
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

// Nullable DTOs are deliberate: unknown TMDB metadata is never manufactured as zero.
data class WatchDetailDto(val id: Int?, val title: String?, val name: String?, val poster_path: String?,
    val release_date: String?, val first_air_date: String?, val runtime: Int?, val seasons: List<SeasonDto>?)
data class SeasonDto(val season_number: Int?, val name: String?, val air_date: String?)
data class SeasonEpisodesDto(val episodes: List<EpisodeDto>?)
data class EpisodeDto(val id: Int?, val season_number: Int?, val episode_number: Int?, val name: String?, val air_date: String?, val runtime: Int?)
data class ProvidersDto(val results: Map<String, ProviderRegionDto>?)
data class ProviderRegionDto(val link: String?, val flatrate: List<ProviderDto>?, val free: List<ProviderDto>?,
    val ads: List<ProviderDto>?, val rent: List<ProviderDto>?, val buy: List<ProviderDto>?)
data class ProviderDto(val provider_id: Int?, val provider_name: String?, val logo_path: String?)

interface WatchlistCatalog {
    suspend fun metadata(key: MediaKey): CatalogMetadata
    suspend fun providers(key: MediaKey, region: String): Availability
    suspend fun seasons(key: MediaKey): List<Season>
    suspend fun episodes(key: MediaKey, season: Int, refresh: Boolean = false): List<Episode>
    suspend fun nextAired(key: MediaKey, episode: Episode): Episode?
}

class TmdbWatchlistCatalog(private val service: ApiService, private val today: () -> LocalDate = { LocalDate.now() }) : WatchlistCatalog {
    private val episodeCache = object : LinkedHashMap<Pair<MediaKey, Int>, List<Episode>>(40, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<MediaKey, Int>, List<Episode>>?) = size > 40
    }
    private suspend fun detail(key: MediaKey) = service.watchDetail(key.type.value, key.tmdbId, Constants.API_KEY)

    override suspend fun metadata(key: MediaKey): CatalogMetadata {
        val dto = detail(key)
        require(dto.id == key.tmdbId)
        val title = (if (key.type == MediaType.Movie) dto.title else dto.name)?.takeIf { it.isNotBlank() }
            ?: throw WatchException(WatchFailure.Validation)
        val availability = try { providers(key, "ES") } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { null }
        return CatalogMetadata(key, title.take(500), dto.poster_path.validPoster(),
            (if (key.type == MediaType.Movie) dto.release_date else dto.first_air_date).validDate(),
            if (key.type == MediaType.Movie) dto.runtime?.takeIf { it in 1..100_000 } else null, availability)
    }

    override suspend fun providers(key: MediaKey, region: String): Availability {
        require(region.matches(Regex("[A-Z]{2}")))
        val dto = service.watchProviders(key.type.value, key.tmdbId, Constants.API_KEY).results?.get(region)
        fun List<ProviderDto>?.mapped() = orEmpty().mapNotNull { p ->
            val id = p.provider_id?.takeIf { it > 0 } ?: return@mapNotNull null
            val name = p.provider_name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            Provider(id, name.take(200), p.logo_path.validPoster())
        }.distinctBy { it.id }.take(50)
        val link = dto?.link?.takeIf { it.startsWith("https://www.themoviedb.org/") }
        return Availability(region, link, dto?.flatrate.mapped(), dto?.free.mapped(), dto?.ads.mapped(), dto?.rent.mapped(), dto?.buy.mapped())
    }

    override suspend fun seasons(key: MediaKey): List<Season> {
        require(key.type == MediaType.Tv)
        return detail(key).seasons.orEmpty().mapNotNull {
            val number = it.season_number?.takeIf { n -> n in 0..1000 } ?: return@mapNotNull null
            Season(number, it.name, it.air_date.validDate())
        }.distinctBy { it.number }.sortedBy { it.number }
    }

    override suspend fun episodes(key: MediaKey, season: Int, refresh: Boolean): List<Episode> {
        require(key.type == MediaType.Tv && season in 0..1000)
        if (!refresh) episodeCache[key to season]?.let { return it }
        val result = service.watchSeason(key.tmdbId, season, Constants.API_KEY).episodes.orEmpty().mapNotNull {
            if (it.id == null || it.id <= 0 || it.season_number != season || it.episode_number == null || it.episode_number <= 0) null
            else Episode(it.id, season, it.episode_number, it.name?.takeIf(String::isNotBlank)?.take(500), it.air_date.validDate(), it.runtime?.takeIf { n -> n in 1..100_000 })
        }.distinctBy { it.number }.sortedBy { it.number }
        episodeCache[key to season] = result
        return result
    }

    override suspend fun nextAired(key: MediaKey, episode: Episode): Episode? {
        episodes(key, episode.season, refresh = true).firstOrNull { it.number > episode.number && it.aired(today()) }?.let { return it }
        for (season in seasons(key).filter { it.number > episode.season && it.airDate != null && runCatching { LocalDate.parse(it.airDate) <= today() }.getOrDefault(false) }) {
            episodes(key, season.number).firstOrNull { it.aired(today()) }?.let { return it }
        }
        return null
    }
}

private fun String?.validDate(): String? = this?.takeIf { value -> runCatching { LocalDate.parse(value) }.isSuccess }
private fun String?.validPoster(): String? = this?.takeIf { it.length <= 500 && it.matches(Regex("^/[A-Za-z0-9._/-]+$")) }
