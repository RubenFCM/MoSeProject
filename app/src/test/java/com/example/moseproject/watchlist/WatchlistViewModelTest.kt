package com.example.moseproject.watchlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.moseproject.data.auth.AuthSession
import com.example.moseproject.data.auth.AuthUser
import com.example.moseproject.data.watchlist.*
import com.example.moseproject.ui.viewmodel.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val auth = MutableStateFlow(AuthUiState(session = authenticated("A")))
    private val repo = MemoryWatchRepository()
    private val catalog = MemoryWatchCatalog()
    private val saved = SavedStateHandle()
    private val key = MediaKey(MediaType.Movie, 123)
    private lateinit var vm: WatchlistViewModel
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        vm = WatchlistViewModel(repo, catalog, auth, saved) { LocalDate.of(2026, 10, 24) }
        store.put("watchlist", vm)
    }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    @Test fun `repeated taps submit once and saved UI only appears after server acknowledgement`() = runTest(dispatcher) {
        runCurrent()
        repo.addGate = CompletableDeferred()
        vm.add(key); vm.add(key)
        runCurrent()
        assertTrue(vm.state.value.busy)
        assertNull(vm.state.value.memberships[key])
        assertNull(vm.state.value.notice)
        assertEquals(1, repo.addCalls)
        repo.addGate!!.complete(Unit)
        runCurrent()
        assertEquals(1, repo.documents.size)
        assertEquals(WatchNotice.Added, vm.state.value.notice)
    }
    @Test fun `adding an existing entry never resets progress or fetches TMDB unnecessarily`() = runTest(dispatcher) {
        val entry = sample(key).copy(status = WatchStatus.InProgress, progress = WatchProgress.Movie(45), progressVersion = 3)
        repo.documents["A" to key] = entry
        runCurrent(); vm.add(key); runCurrent()
        assertEquals(entry, vm.state.value.memberships[key])
        assertEquals(0, repo.addCalls); assertEquals(0, catalog.metadataCalls)
    }
    @Test fun `progress and platform updates preserve each others fields and manual watched status`() = runTest(dispatcher) {
        repo.documents["A" to key] = sample(key)
        runCurrent(); vm.openEditor(key); runCurrent()
        vm.saveEditor(WatchStatus.InProgress, "45"); runCurrent()
        val originalMetadata = repo.documents["A" to key]!!.catalog
        vm.refreshProviders(key); runCurrent()
        val after = repo.documents["A" to key]!!
        assertEquals(WatchProgress.Movie(45), after.progress)
        assertEquals(originalMetadata.title, after.catalog.title)
        assertEquals(1L, after.progressVersion)
        vm.openEditor(key); runCurrent(); vm.saveEditor(WatchStatus.Watched, "bad discarded input"); runCurrent()
        vm.refreshProviders(key); runCurrent()
        assertEquals(WatchStatus.Watched, repo.documents["A" to key]!!.status)
        assertEquals(WatchProgress.Movie(), repo.documents["A" to key]!!.progress)
    }
    @Test fun `invalid minute input does not write and a rejected write is never presented as saved`() = runTest(dispatcher) {
        repo.documents["A" to key] = sample(key)
        runCurrent(); vm.openEditor(key); runCurrent()
        for (minute in listOf("-1", "121", "1.5", "2147483648")) {
            vm.saveEditor(WatchStatus.InProgress, minute); runCurrent()
            assertEquals(WatchFailure.Validation, vm.state.value.error)
        }
        assertEquals(0, repo.progressCalls)
        repo.failure = WatchFailure.Permission
        vm.saveEditor(WatchStatus.InProgress, "45"); runCurrent()
        assertEquals(WatchFailure.Permission, vm.state.value.error)
        assertNull(vm.state.value.notice)
        assertEquals(WatchProgress.Movie(), repo.documents["A" to key]!!.progress)
    }
    @Test fun `series selections come from real season results including specials and episodes advance independently`() = runTest(dispatcher) {
        val tv = MediaKey(MediaType.Tv, 123)
        repo.documents["A" to tv] = sample(tv)
        runCurrent(); vm.openEditor(tv); runCurrent()
        vm.selectSeason(0); runCurrent()
        assertEquals(0, vm.state.value.editor?.selectedSeason)
        val selectedId = vm.state.value.editor?.selectedEpisodeId
        vm.selectEpisode(99999)
        assertEquals(selectedId, vm.state.value.editor?.selectedEpisodeId)
        vm.saveEditor(WatchStatus.InProgress, "99"); runCurrent()
        assertEquals(WatchFailure.Validation, vm.state.value.error)
        vm.finishEpisode(); runCurrent()
        val entry = repo.documents["A" to tv]!!
        assertEquals(WatchStatus.InProgress, entry.status)
        val progress = assertIs<WatchProgress.Series>(entry.progress)
        assertEquals(0, progress.lastCompleted?.season)
        assertEquals(2, progress.current?.episode?.number)
        assertTrue(progress.current!!.nextToWatch)
    }
    @Test fun `deletion updates detail membership and list only after acknowledgement`() = runTest(dispatcher) {
        repo.documents["A" to key] = sample(key)
        runCurrent(); vm.openList(); vm.inspect(key); runCurrent()
        assertEquals(1, vm.state.value.entries.size)
        vm.delete(key); runCurrent()
        assertTrue(vm.state.value.entries.isEmpty())
        assertTrue(vm.state.value.memberships.containsKey(key))
        assertNull(vm.state.value.memberships[key])
        assertEquals(WatchNotice.Deleted, vm.state.value.notice)
    }
    @Test fun `guest and unverified users cannot access data and only public IDs are retained to resume adding`() = runTest(dispatcher) {
        auth.value = AuthUiState(session = AuthSession.PendingVerification(AuthUser("A", "test@example.test", false)))
        runCurrent(); vm.add(key); vm.openList(); runCurrent()
        assertEquals(0, repo.readCalls); assertEquals(0, repo.addCalls)
        vm.deferAdd(key)
        assertEquals(setOf("watchlist_pending_type", "watchlist_pending_id"), saved.keys())
        assertEquals("movie", saved.get<String>("watchlist_pending_type"))
        auth.value = AuthUiState(session = authenticated("A"))
        runCurrent()
        assertEquals(key, vm.state.value.resumeKey)
        assertNotNull(vm.state.value.memberships[key])
        assertTrue(saved.keys().isEmpty())
    }
    @Test fun `logout and account switch clear entries drafts pending actions and ignore late responses`() = runTest(dispatcher) {
        repo.documents["A" to key] = sample(key)
        repo.pageGate = CompletableDeferred()
        runCurrent(); vm.openList(); runCurrent()
        vm.deferAdd(key)
        auth.value = AuthUiState(session = AuthSession.SignedOut); runCurrent()
        assertEquals(WatchlistUiState(), vm.state.value)
        assertTrue(saved.keys().isEmpty())
        auth.value = AuthUiState(session = authenticated("B")); runCurrent()
        repo.pageGate!!.complete(Unit); runCurrent()
        assertEquals("B", vm.state.value.uid)
        assertTrue(vm.state.value.entries.isEmpty()); assertTrue(vm.state.value.memberships.isEmpty())
        assertNull(vm.state.value.editor)
    }
    @Test fun `filters and pagination are passed to bounded repository queries`() = runTest(dispatcher) {
        runCurrent()
        vm.setFilter(WatchFilter(MediaType.Tv, WatchStatus.InProgress, WatchSort.Updated)); runCurrent()
        assertEquals(WatchFilter(MediaType.Tv, WatchStatus.InProgress, WatchSort.Updated), repo.lastFilter)
        assertEquals(1, repo.readCalls)
        auth.value = auth.value.copy(operation = AuthOperation.CheckVerification); runCurrent()
        assertEquals(1, repo.readCalls)
    }
}

private fun authenticated(uid: String) = AuthSession.Authenticated(AuthUser(uid, "$uid@example.test", true))
private fun sample(key: MediaKey) = WatchEntry(CatalogMetadata(key, "Title", null, null, if (key.type == MediaType.Movie) 120 else null),
    WatchStatus.Pending, if (key.type == MediaType.Movie) WatchProgress.Movie() else WatchProgress.Series(), createdAtMillis = 1000, updatedAtMillis = 1000)

private class MemoryWatchRepository : WatchlistRepository {
    val documents = mutableMapOf<Pair<String, MediaKey>, WatchEntry>()
    var addGate: CompletableDeferred<Unit>? = null
    var pageGate: CompletableDeferred<Unit>? = null
    var addCalls = 0; var readCalls = 0; var progressCalls = 0
    var lastFilter: WatchFilter? = null
    var failure: WatchFailure? = null
    private fun checkFailure() { failure?.let { throw WatchException(it) } }
    override suspend fun page(uid: String, filter: WatchFilter, after: PageCursor?): WatchPage {
        readCalls++; lastFilter = filter
        if (uid == "A") pageGate?.let { withContext(NonCancellable) { it.await() } }
        checkFailure()
        return WatchPage(documents.filter { it.key.first == uid && (filter.type == null || it.key.second.type == filter.type) && (filter.status == null || it.value.status == filter.status) }.values.toList(), null, false)
    }
    override suspend fun get(uid: String, key: MediaKey): WatchEntry? { checkFailure(); return documents[uid to key] }
    override suspend fun add(uid: String, metadata: CatalogMetadata): WatchEntry {
        addCalls++; addGate?.await(); checkFailure()
        return documents.getOrPut(uid to metadata.key) { sample(metadata.key).copy(catalog = metadata) }
    }
    override suspend fun saveProgress(uid: String, entry: WatchEntry, status: WatchStatus, progress: WatchProgress): WatchEntry {
        checkFailure()
        val old = documents[uid to entry.catalog.key] ?: throw WatchException(WatchFailure.Conflict)
        if (old.progressVersion != entry.progressVersion) throw WatchException(WatchFailure.Conflict)
        progressCalls++
        return old.copy(status = status, progress = progress, progressVersion = old.progressVersion + 1).also { documents[uid to entry.catalog.key] = it }
    }
    override suspend fun refreshProviders(uid: String, key: MediaKey, availability: Availability): WatchEntry {
        checkFailure()
        val old = documents[uid to key]!!
        return old.copy(catalog = old.catalog.copy(availability = availability)).also { documents[uid to key] = it }
    }
    override suspend fun delete(uid: String, key: MediaKey) { checkFailure(); documents.remove(uid to key) }
}

private class MemoryWatchCatalog : WatchlistCatalog {
    var metadataCalls = 0
    override suspend fun metadata(key: MediaKey): CatalogMetadata { metadataCalls++; return sample(key).catalog }
    override suspend fun providers(key: MediaKey, region: String) = Availability(region)
    override suspend fun seasons(key: MediaKey) = listOf(Season(0, "Specials", null), Season(1, "First", null))
    override suspend fun episodes(key: MediaKey, season: Int, refresh: Boolean) = listOf(Episode(10 + season * 2, season, 1, "Episode", "2026-01-01", 45))
    override suspend fun nextAired(key: MediaKey, episode: Episode) = episode.copy(id = episode.id + 1, number = 2)
}
