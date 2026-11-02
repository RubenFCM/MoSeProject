package com.example.moseproject.watchlist

import com.example.moseproject.data.watchlist.*
import java.time.LocalDate
import org.junit.Test
import kotlin.test.*

class PendingEpisodesTest {
    private val today = LocalDate.of(2026, 11, 1)
    private val ten = Episode(10, 1, 10, null, "2026-01-01", 45)
    private val eleven = ten.copy(id = 11, number = 11)
    private fun entry(progress: WatchProgress.Series, status: WatchStatus = WatchStatus.InProgress) = WatchEntry(
        CatalogMetadata(MediaKey(MediaType.Tv, 1), "Show", null, null, null), status, progress)

    @Test fun `episode after current position is pending even when there is no previous catalogue baseline`() {
        val result = entry(WatchProgress.Series(EpisodePosition(ten, 20))).pendingEpisodes(listOf(ten, eleven), today)
        assertEquals(1, result.count); assertEquals(eleven, result.next)
    }
    @Test fun `queued next episode is counted but a currently watching episode is not counted twice`() {
        val queued = entry(WatchProgress.Series(EpisodePosition(eleven, 0, true), ten))
        assertEquals(1, queued.pendingEpisodes(listOf(ten, eleven), today).count)
        val watching = entry(WatchProgress.Series(EpisodePosition(eleven, 20), ten))
        assertEquals(0, watching.pendingEpisodes(listOf(ten, eleven), today).count)
    }
    @Test fun `season transitions gaps duplicates unknown dates and future episodes count only real aired positions`() {
        val first = ten.copy(id = 20, season = 2, number = 1)
        val third = first.copy(id = 22, number = 3)
        val result = entry(WatchProgress.Series(lastCompleted = ten)).pendingEpisodes(listOf(
            ten, first, third, third, third.copy(number = 4, airDate = null),
            third.copy(number = 5, airDate = "2027-01-01"), third.copy(season = 0)), today)
        assertEquals(2, result.count); assertEquals(first, result.next); assertEquals(2, result.newSeason)
    }
    @Test fun `pending starts from the beginning while legacy watched entries require a saved position`() {
        assertEquals(2, entry(WatchProgress.Series(), WatchStatus.Pending).pendingEpisodes(listOf(ten, eleven), today).count)
        assertTrue(entry(WatchProgress.Series(), WatchStatus.Watched).pendingEpisodes(listOf(ten, eleven), today).needsPosition)
        assertEquals(1, entry(WatchProgress.Series(lastCompleted = ten), WatchStatus.Watched).pendingEpisodes(listOf(ten, eleven), today).count)
    }
}
