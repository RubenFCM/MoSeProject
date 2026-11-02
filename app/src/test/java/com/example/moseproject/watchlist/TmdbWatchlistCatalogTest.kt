package com.example.moseproject.watchlist

import com.example.moseproject.data.ApiService
import com.example.moseproject.data.watchlist.*
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import kotlin.test.*

class TmdbWatchlistCatalogTest {
    @Test fun `pending catalogue fetches only regular seasons from the saved season and filters unconfirmed episodes`() = runTest {
        coEvery { api.watchDetail("tv", 123, any()) } returns detail(MediaType.Tv).copy(seasons = listOf(
            SeasonDto(0, "Specials", null), SeasonDto(1, "First", "2025-01-01"),
            SeasonDto(2, "Second", "2026-01-01"), SeasonDto(3, "Future", "2027-01-01")))
        coEvery { api.watchSeason(123, 2, any()) } returns SeasonEpisodesDto(listOf(
            EpisodeDto(21, 2, 1, null, "2026-01-01", 45), EpisodeDto(23, 2, 3, null, "2026-10-24", 45),
            EpisodeDto(24, 2, 4, null, "2026-10-25", 45), EpisodeDto(25, 2, 5, null, null, 45)))
        val result = catalog.airedEpisodes(key, 2, LocalDate.of(2026, 10, 24))
        assertEquals(listOf(1, 3), result.map { it.number })
        coVerify(exactly = 0) { api.watchSeason(123, 0, any()) }
        coVerify(exactly = 0) { api.watchSeason(123, 1, any()) }
        coVerify(exactly = 0) { api.watchSeason(123, 3, any()) }
    }
    @Test fun `release checks use only known aired regular episodes from show details`() = runTest {
        val episode = EpisodeDto(42, 2, 3, "New", "2026-10-24", 45)
        coEvery { api.watchDetail("tv", 123, any()) } returns detail(MediaType.Tv).copy(last_episode_to_air = episode)
        assertEquals(2, catalog.latestAired(key)?.season)
        for (invalid in listOf(episode.copy(air_date = "2026-10-25"), episode.copy(air_date = null),
            episode.copy(air_date = "invalid"), episode.copy(season_number = 0), episode.copy(id = null))) {
            coEvery { api.watchDetail("tv", 123, any()) } returns detail(MediaType.Tv).copy(last_episode_to_air = invalid)
            assertNull(catalog.latestAired(key))
        }
        coVerify(exactly = 0) { api.watchSeason(any(), any(), any()) }
        coVerify(exactly = 0) { api.watchProviders(any(), any(), any()) }
    }
    private val api = mockk<ApiService>()
    private val key = MediaKey(MediaType.Tv, 123)
    private val catalog = TmdbWatchlistCatalog(api) { LocalDate.of(2026, 10, 24) }
    private fun detail(type: MediaType) = WatchDetailDto(123, if (type == MediaType.Movie) "Movie" else null,
        if (type == MediaType.Tv) "Show" else null, null, null, null, null, listOf(SeasonDto(0, "Specials", null), SeasonDto(1, "First", "2026-01-01")))

    @Test fun `missing metadata remains unknown and TV never uses an episode as total runtime`() = runTest {
        coEvery { api.watchDetail("tv", 123, any()) } returns detail(MediaType.Tv).copy(runtime = 45)
        coEvery { api.watchProviders("tv", 123, any()) } returns ProvidersDto(emptyMap())
        val result = catalog.metadata(key)
        assertNull(result.posterPath); assertNull(result.releaseDate); assertNull(result.runtimeMinutes)
        assertEquals("ES", result.availability?.region)
        assertTrue(result.availability!!.subscription.isEmpty())
        coVerify(exactly = 0) { api.watchSeason(any(), any(), any()) }
    }
    @Test fun `nullable Gson DTO does not turn omitted values into zero and rejects missing titles`() = runTest {
        val dto = Gson().fromJson("""{"id":123,"title":"Movie"}""", WatchDetailDto::class.java)
        assertNull(dto.runtime)
        coEvery { api.watchDetail("movie", 123, any()) } returns dto
        coEvery { api.watchProviders("movie", 123, any()) } throws java.io.IOException()
        val result = catalog.metadata(MediaKey(MediaType.Movie, 123))
        assertNull(result.runtimeMinutes); assertNull(result.availability)
        coEvery { api.watchDetail("movie", 123, any()) } returns dto.copy(title = null)
        assertFailsWith<WatchException> { catalog.metadata(MediaKey(MediaType.Movie, 123)) }
    }
    @Test fun `providers are regional separated and links are not invented`() = runTest {
        val provider = ProviderDto(1, "Test provider", null)
        coEvery { api.watchProviders("tv", 123, any()) } returns ProvidersDto(mapOf("ES" to ProviderRegionDto(
            "https://www.themoviedb.org/tv/123/watch?locale=ES", listOf(provider), null, listOf(provider), listOf(provider), null)))
        val result = catalog.providers(key, "ES")
        assertEquals(1, result.subscription.size); assertEquals(1, result.ads.size); assertEquals(1, result.rent.size)
        assertTrue(result.buy.isEmpty()); assertTrue(result.free.isEmpty())
        assertNull(catalog.providers(key, "FR").link)
        coEvery { api.watchProviders("tv", 123, any()) } returns ProvidersDto(mapOf("ES" to ProviderRegionDto("https://evil.example/", null, null, null, null, null)))
        assertNull(catalog.providers(key, "ES").link)
    }
    @Test fun `episodes are fetched per season and cached until explicitly refreshed`() = runTest {
        coEvery { api.watchSeason(123, 0, any()) } returns SeasonEpisodesDto(listOf(EpisodeDto(42, 0, 1, null, null, null), EpisodeDto(null, 0, 2, null, null, null)))
        assertEquals(0, catalog.episodes(key, 0).single().season)
        assertNull(catalog.episodes(key, 0).single().runtimeMinutes)
        coVerify(exactly = 1) { api.watchSeason(123, 0, any()) }
        catalog.episodes(key, 0, refresh = true)
        coVerify(exactly = 2) { api.watchSeason(123, 0, any()) }
        coVerify(exactly = 0) { api.watchSeason(123, 1, any()) }
    }
    @Test fun `automatic advancement only selects known aired episodes and preserves specials numbering`() = runTest {
        val current = Episode(42, 0, 1, null, "2026-01-01", 45)
        coEvery { api.watchSeason(123, 0, any()) } returns SeasonEpisodesDto(listOf(EpisodeDto(42, 0, 1, null, "2026-01-01", 45), EpisodeDto(43, 0, 2, null, "2027-01-01", 45)))
        coEvery { api.watchDetail("tv", 123, any()) } returns detail(MediaType.Tv)
        coEvery { api.watchSeason(123, 1, any()) } returns SeasonEpisodesDto(listOf(EpisodeDto(44, 1, 1, null, "2026-01-01", 45), EpisodeDto(45, 1, 2, null, null, 45)))
        val next = catalog.nextAired(key, current)
        assertEquals(1, next?.season); assertEquals(1, next?.number)
        assertTrue(next!!.aired(LocalDate.of(2026, 10, 24)))
    }
}
