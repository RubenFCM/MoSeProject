package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.moseproject.data.auth.AuthSession
import com.example.moseproject.data.watchlist.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.time.LocalDate

data class WatchEditor(val entry: WatchEntry, val seasons: List<Season> = emptyList(), val selectedSeason: Int? = null,
    val episodes: List<Episode> = emptyList(), val selectedEpisodeId: Int? = null, val episodesLoading: Boolean = false)
enum class WatchNotice { Added, Saved, Deleted, ProvidersUpdated }
data class WatchlistUiState(
    val uid: String? = null, val entries: List<WatchEntry> = emptyList(), val filter: WatchFilter = WatchFilter(),
    val loading: Boolean = false, val hasMore: Boolean = false, val busy: Boolean = false,
    val error: WatchFailure? = null, val notice: WatchNotice? = null,
    val memberships: Map<MediaKey, WatchEntry?> = emptyMap(), val checking: Set<MediaKey> = emptySet(),
    val editor: WatchEditor? = null, val resumeKey: MediaKey? = null,
    val pendingEpisodes: Map<MediaKey, PendingEpisodes> = emptyMap(),
    val episodesChecking: Boolean = false, val episodesCheckFailed: Boolean = false
)

class WatchlistViewModel(
    private val repository: WatchlistRepository, private val catalog: WatchlistCatalog,
    auth: StateFlow<AuthUiState>, private val saved: SavedStateHandle,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {
    private val mutableState = MutableStateFlow(WatchlistUiState())
    val state = mutableState.asStateFlow()
    private var cursor: PageCursor? = null
    private var listOpened = false
    private var queryJob: Job? = null
    private var operationJob: Job? = null
    private var detailJob: Job? = null
    private var episodeJob: Job? = null
    private var epoch = 0
    private var queryGeneration = 0
    private data class AiredCache(val fromSeason: Int, val episodes: List<Episode>, val checkedAt: Long, val day: LocalDate)
    private val airedCache = object : LinkedHashMap<MediaKey, AiredCache>(40, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<MediaKey, AiredCache>?) = size > 40
    }

    init {
        viewModelScope.launch {
            auth.map { (it.session as? AuthSession.Authenticated)?.user?.uid }.distinctUntilChanged().collect { uid ->
                val previous = state.value.uid
                epoch++
                queryJob?.cancel(); operationJob?.cancel(); detailJob?.cancel(); episodeJob?.cancel()
                cursor = null
                airedCache.clear()
                // There are no personal disk caches; erase all visible/query/editor state on identity loss.
                if (previous != null && previous != uid) clearPending()
                mutableState.value = WatchlistUiState(uid = uid)
                if (uid != null) {
                    val pendingAction = pending()
                    pendingAction?.let { key ->
                        mutableState.update { it.copy(resumeKey = key) }
                        add(key)
                    }
                    if (listOpened && pendingAction == null) load(reset = true)
                }
            }
        }
    }

    /** Only these two public identifiers survive auth navigation/process recreation. */
    fun deferAdd(key: MediaKey) {
        saved["watchlist_pending_type"] = key.type.value
        saved["watchlist_pending_id"] = key.tmdbId
    }
    private fun pending(): MediaKey? {
        val type = MediaType.entries.firstOrNull { it.value == saved.get<String>("watchlist_pending_type") } ?: return null
        val id = saved.get<Int>("watchlist_pending_id")?.takeIf { it > 0 } ?: return null
        return MediaKey(type, id)
    }
    private fun clearPending() { saved.remove<String>("watchlist_pending_type"); saved.remove<Int>("watchlist_pending_id") }
    fun consumeResume() { mutableState.update { it.copy(resumeKey = null) } }
    fun clearFeedback() { mutableState.update { it.copy(error = null, notice = null) } }

    fun openList() { listOpened = true; if (!state.value.loading) load(reset = true) }
    fun refreshEpisodes() { if (!state.value.loading && !state.value.busy) load(reset = true, forceEpisodes = true) }
    fun setFilter(filter: WatchFilter) {
        if (state.value.filter == filter) return
        mutableState.update { it.copy(filter = filter, entries = emptyList()) }
        load(reset = true)
    }
    fun loadMore() { if (state.value.hasMore && !state.value.loading) load(false) }

    private fun load(reset: Boolean, forceEpisodes: Boolean = false) {
        val uid = state.value.uid ?: return
        val generation = epoch
        val filter = state.value.filter
        queryJob?.cancel()
        val query = ++queryGeneration
        if (reset) cursor = null
        mutableState.update { it.copy(loading = true, error = null, episodesChecking = false, episodesCheckFailed = false) }
        queryJob = viewModelScope.launch {
            try {
                val page = repository.page(uid, filter, if (reset) null else cursor)
                ensureSession(uid)
                if (epoch != generation || query != queryGeneration) return@launch
                cursor = page.cursor
                mutableState.update { it.copy(entries = (if (reset) page.entries else it.entries + page.entries).distinctBy { e -> e.catalog.key },
                    hasMore = page.hasMore, loading = false) }
                refreshPendingSummaries()
                val candidates = page.entries.filter { entry ->
                    val cached = airedCache[entry.catalog.key]
                    entry.catalog.key.type == MediaType.Tv && !(entry.status == WatchStatus.Watched && entry.episodeReference() == null) &&
                        (forceEpisodes || cached == null || cached.fromSeason != fromSeason(entry) || cached.day != today() ||
                            nowMillis() - cached.checkedAt !in 0 until 12 * 60 * 60 * 1000L)
                }
                mutableState.update { it.copy(episodesChecking = candidates.isNotEmpty()) }
                for (entry in candidates) {
                    try {
                        val episodes = catalog.airedEpisodes(entry.catalog.key, fromSeason(entry), today())
                        ensureSession(uid)
                        if (query != queryGeneration) return@launch
                        airedCache[entry.catalog.key] = AiredCache(fromSeason(entry), episodes, nowMillis(), today())
                        refreshPendingSummaries()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) {
                        if (epoch == generation && query == queryGeneration) mutableState.update { it.copy(episodesCheckFailed = true) }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (epoch == generation) mutableState.update { it.copy(loading = false, error = failure(error)) } }
            finally { if (epoch == generation && query == queryGeneration) mutableState.update { it.copy(episodesChecking = false) } }
        }
    }

    private fun fromSeason(entry: WatchEntry) = maxOf(1, entry.episodeReference()?.season ?: 1)

    private fun refreshPendingSummaries() {
        val summaries = state.value.entries.filter { it.catalog.key.type == MediaType.Tv }.mapNotNull { entry ->
            if (entry.status == WatchStatus.Watched && entry.episodeReference() == null) {
                entry.catalog.key to PendingEpisodes(0, null, needsPosition = true)
            } else airedCache[entry.catalog.key]?.takeIf { it.fromSeason == fromSeason(entry) }?.let {
                entry.catalog.key to entry.pendingEpisodes(it.episodes, today())
            }
        }.toMap()
        mutableState.update { it.copy(pendingEpisodes = summaries) }
    }

    fun inspect(key: MediaKey) {
        val uid = state.value.uid ?: return
        val generation = epoch
        detailJob?.cancel()
        mutableState.update { it.copy(checking = setOf(key)) }
        detailJob = viewModelScope.launch {
            try {
                val entry = repository.get(uid, key)
                if (generation == epoch) mutableState.update { it.copy(memberships = (it.memberships + (key to entry)).entries.toList().takeLast(100).associate { e -> e.toPair() }, checking = emptySet()) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (generation == epoch) mutableState.update { it.copy(checking = emptySet(), error = failure(error)) } }
        }
    }

    fun add(key: MediaKey) = execute { uid ->
        // Avoid catalogue requests too when the document already exists.
        val existing = repository.get(uid, key)
        ensureSession(uid)
        val entry = existing ?: repository.add(uid, catalog.metadata(key))
        ensureSession(uid)
        upsert(entry)
        clearPending()
        mutableState.update { it.copy(notice = WatchNotice.Added) }
        if (listOpened) load(true)
    }

    fun openEditor(key: MediaKey) = execute { uid ->
        val entry = repository.get(uid, key) ?: throw WatchException(WatchFailure.Conflict)
        ensureSession(uid)
        upsert(entry)
        mutableState.update { it.copy(editor = WatchEditor(entry)) }
        if (key.type == MediaType.Tv) {
            val seasons = catalog.seasons(key)
            ensureSession(uid)
            val progress = entry.progress as WatchProgress.Series
            val selected = progress.current?.episode?.season ?: progress.lastCompleted?.season
                ?: seasons.firstOrNull { it.number > 0 }?.number ?: seasons.firstOrNull()?.number
            mutableState.update { it.copy(editor = it.editor?.copy(seasons = seasons, selectedSeason = selected)) }
            if (selected != null) selectSeason(selected)
        }
    }

    fun selectSeason(season: Int, refresh: Boolean = false) {
        val editor = state.value.editor ?: return
        if (editor.seasons.none { it.number == season }) return
        val generation = epoch
        episodeJob?.cancel()
        mutableState.update { it.copy(editor = it.editor?.copy(selectedSeason = season, episodes = emptyList(), selectedEpisodeId = null, episodesLoading = true), error = null) }
        episodeJob = viewModelScope.launch {
            try {
                val episodes = catalog.episodes(editor.entry.catalog.key, season, refresh)
                if (epoch != generation || state.value.editor?.selectedSeason != season) return@launch
                val current = (editor.entry.progress as? WatchProgress.Series)?.current?.episode?.id
                mutableState.update { it.copy(editor = it.editor?.copy(episodes = episodes, episodesLoading = false,
                    selectedEpisodeId = episodes.firstOrNull { e -> e.id == current }?.id ?: episodes.firstOrNull { e -> e.selectable(today()) }?.id)) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (epoch == generation) mutableState.update { it.copy(error = failure(error), editor = it.editor?.copy(episodesLoading = false)) } }
        }
    }

    fun selectEpisode(id: Int) {
        val editor = state.value.editor ?: return
        if (editor.episodes.none { it.id == id && it.selectable(today()) }) return
        mutableState.update { it.copy(editor = it.editor?.copy(selectedEpisodeId = id)) }
    }

    fun closeEditor() { if (!state.value.busy) { episodeJob?.cancel(); mutableState.update { it.copy(editor = null, error = null) } } }

    fun saveEditor(status: WatchStatus, minuteText: String) = execute { uid ->
        val editor = state.value.editor ?: return@execute
        if (editor.episodesLoading) throw WatchException(WatchFailure.Validation)
        val minute = if (status != WatchStatus.InProgress || minuteText.isBlank()) null else minuteText.toIntOrNull() ?: throw WatchException(WatchFailure.Validation)
        val pair = when (val progress = editor.entry.progress) {
            is WatchProgress.Movie -> ProgressPolicy.movie(status, minute, editor.entry.catalog.runtimeMinutes)
            is WatchProgress.Series -> {
                val completed = if (status == WatchStatus.Watched) catalog.latestAired(editor.entry.catalog.key)
                    ?: selectedEpisode(editor)?.takeIf { it.aired(today()) } ?: progress.lastCompleted else progress.lastCompleted
                ensureSession(uid)
                ProgressPolicy.series(status, selectedEpisode(editor), minute, completed)
            }
        }
        val entry = repository.saveProgress(uid, editor.entry, pair.first, pair.second)
        ensureSession(uid)
        upsert(entry)
        mutableState.update { it.copy(editor = null, notice = WatchNotice.Saved) }
        if (listOpened) load(true)
    }

    fun finishEpisode() = execute { uid ->
        val editor = state.value.editor ?: return@execute
        val episode = selectedEpisode(editor) ?: throw WatchException(WatchFailure.Validation)
        val next = catalog.nextAired(editor.entry.catalog.key, episode)
        ensureSession(uid)
        val pair = ProgressPolicy.finishedEpisode(episode, next)
        val entry = repository.saveProgress(uid, editor.entry, pair.first, pair.second)
        ensureSession(uid)
        upsert(entry)
        mutableState.update { it.copy(editor = null, notice = WatchNotice.Saved) }
        if (listOpened) load(true)
    }

    private fun selectedEpisode(editor: WatchEditor) = editor.episodes.firstOrNull { it.id == editor.selectedEpisodeId && it.selectable(today()) }

    fun refreshProviders(key: MediaKey) = execute { uid ->
        val region = state.value.memberships[key]?.catalog?.availability?.region ?: "ES"
        val availability = catalog.providers(key, region)
        ensureSession(uid)
        val entry = repository.refreshProviders(uid, key, availability)
        ensureSession(uid)
        upsert(entry)
        mutableState.update { it.copy(editor = it.editor?.takeIf { e -> e.entry.catalog.key == key }?.copy(entry = entry) ?: it.editor,
            notice = WatchNotice.ProvidersUpdated) }
        if (listOpened) load(true)
    }

    fun delete(key: MediaKey) = execute { uid ->
        repository.delete(uid, key)
        ensureSession(uid)
        mutableState.update { it.copy(entries = it.entries.filterNot { e -> e.catalog.key == key }, memberships = it.memberships + (key to null),
            editor = null, notice = WatchNotice.Deleted) }
        if (listOpened) load(true)
    }

    private fun upsert(entry: WatchEntry) {
        mutableState.update { it.copy(memberships = it.memberships + (entry.catalog.key to entry),
            entries = it.entries.map { e -> if (e.catalog.key == entry.catalog.key) entry else e }) }
        refreshPendingSummaries()
    }

    private suspend fun ensureSession(uid: String) {
        currentCoroutineContext().ensureActive()
        if (state.value.uid != uid) throw CancellationException("Account changed")
    }

    private fun execute(block: suspend (String) -> Unit) {
        val uid = state.value.uid ?: return
        if (state.value.busy) return
        val generation = epoch
        mutableState.update { it.copy(busy = true, error = null, notice = null) }
        operationJob = viewModelScope.launch {
            try { block(uid) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (epoch == generation) mutableState.update { it.copy(error = failure(error)) } }
            finally { if (epoch == generation) mutableState.update { it.copy(busy = false) } }
        }
    }

    private fun failure(error: Exception) = when (error) {
        is WatchException -> error.reason
        is IllegalArgumentException -> WatchFailure.Validation
        is java.io.IOException -> WatchFailure.Network
        else -> WatchFailure.Unknown
    }

    companion object {
        fun factory(repository: WatchlistRepository, catalog: WatchlistCatalog, auth: StateFlow<AuthUiState>) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                require(modelClass.isAssignableFrom(WatchlistViewModel::class.java))
                @Suppress("UNCHECKED_CAST") // Factory contract, checked against modelClass.
                return WatchlistViewModel(repository, catalog, auth, extras.createSavedStateHandle()) as T
            }
        }
    }
}
