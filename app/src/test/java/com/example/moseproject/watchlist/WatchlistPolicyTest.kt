package com.example.moseproject.watchlist

import com.example.moseproject.data.watchlist.*
import org.junit.Test
import kotlin.test.*

class WatchlistPolicyTest {
    private val episode = Episode(42, 0, 2, null, null, 45)

    @Test fun `IDs are deterministic and distinguish movie and TV`() {
        assertEquals("movie_123", MediaKey(MediaType.Movie, 123).documentId)
        assertEquals("tv_123", MediaKey(MediaType.Tv, 123).documentId)
        assertFailsWith<IllegalArgumentException> { MediaKey(MediaType.Movie, 0) }
    }
    @Test fun `movie minutes choose coherent states and do not manufacture unknown duration`() {
        assertEquals(WatchStatus.InProgress, ProgressPolicy.movie(WatchStatus.InProgress, 45, 120).first)
        assertEquals(WatchStatus.Pending, ProgressPolicy.movie(WatchStatus.InProgress, 0, 120).first)
        assertEquals(WatchStatus.Watched, ProgressPolicy.movie(WatchStatus.InProgress, 120, 120).first)
        assertEquals(WatchStatus.InProgress, ProgressPolicy.movie(WatchStatus.InProgress, 120, null).first)
        assertEquals(WatchProgress.Movie(), ProgressPolicy.movie(WatchStatus.Watched, null, null).second)
        assertEquals(WatchProgress.Movie(), ProgressPolicy.movie(WatchStatus.Pending, 45, 120).second)
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.movie(WatchStatus.InProgress, -1, 120) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.movie(WatchStatus.InProgress, 121, 120) }
    }
    @Test fun `specials season zero and unknown episode minutes are valid`() {
        val saved = ProgressPolicy.series(WatchStatus.InProgress, episode, null).second
        assertEquals(0, saved.current?.episode?.season)
        assertNull(saved.current?.minute)
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.series(WatchStatus.InProgress, episode.copy(season = -1), 3) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.series(WatchStatus.InProgress, episode.copy(number = 0), 3) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.series(WatchStatus.InProgress, episode, 46) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.series(WatchStatus.InProgress, null, 3) }
    }
    @Test fun `finishing an episode advances separately and never completes the whole series`() {
        val next = episode.copy(id = 43, number = 3)
        val (status, progress) = ProgressPolicy.finishedEpisode(episode, next)
        assertEquals(WatchStatus.InProgress, status)
        assertEquals(episode, progress.lastCompleted)
        assertEquals(next, progress.current?.episode)
        assertTrue(progress.current!!.nextToWatch)
        assertEquals(0, progress.current.minute)
        assertEquals(WatchStatus.InProgress, ProgressPolicy.finishedEpisode(episode, null).first)
        assertNull(ProgressPolicy.finishedEpisode(episode, null).second.current)
        assertEquals(WatchProgress.Series(), ProgressPolicy.series(WatchStatus.Watched, episode, 18).second)
    }
    @Test fun `invalid stored progress cannot be rendered as a valid entry`() {
        val catalog = CatalogMetadata(MediaKey(MediaType.Movie, 1), "Movie", null, null, 120)
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.validate(WatchEntry(catalog, WatchStatus.Pending, WatchProgress.Movie(45))) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.validate(WatchEntry(catalog, WatchStatus.InProgress, WatchProgress.Movie(120))) }
        assertFailsWith<IllegalArgumentException> { ProgressPolicy.validate(WatchEntry(catalog, WatchStatus.Watched, WatchProgress.Movie(45))) }
        ProgressPolicy.validate(WatchEntry(catalog.copy(runtimeMinutes = null), WatchStatus.InProgress, WatchProgress.Movie(45)))
    }
}
