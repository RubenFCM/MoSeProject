package com.example.moseproject.ui.view.watchlist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moseproject.R
import com.example.moseproject.data.auth.AuthSession
import com.example.moseproject.data.watchlist.*
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.theme.CinemaAccent
import com.example.moseproject.ui.theme.CinemaMuted
import com.example.moseproject.ui.theme.CinemaSurface
import com.example.moseproject.ui.view.components.CinemaScaffold
import com.example.moseproject.ui.view.components.LocalCinemaNavigationClearance
import com.example.moseproject.ui.view.components.imageUrl
import com.example.moseproject.ui.viewmodel.*
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

@Composable
fun WatchlistScreen(nav: NavController, viewModel: WatchlistViewModel, auth: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authState by auth.state.collectAsStateWithLifecycle()
    val uid = (authState.session as? AuthSession.Authenticated)?.user?.uid
    LaunchedEffect(uid) { if (uid != null) viewModel.openList() }
    CinemaScaffold(nav, AppScreen.Watchlist.route, stringResource(R.string.wl_title)) { padding ->
        // Keep interactive rows outside the floating navigation's touch area, also at large font sizes.
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(bottom = LocalCinemaNavigationClearance.current),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (authState.session is AuthSession.Initializing || (uid != null && state.uid != uid)) {
                item { CircularProgressIndicator(); Text(stringResource(R.string.wl_loading)) }
            } else if (uid == null) {
                item {
                    Text(stringResource(R.string.wl_auth_title), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.wl_auth_description))
                    Button(onClick = { nav.navigate(AppScreen.Account.route) }) { Text(stringResource(R.string.wl_sign_in)) }
                }
            } else {
                item { WatchFilters(state.filter, viewModel::setFilter) }
                item {
                    OutlinedButton(onClick = viewModel::openList, enabled = !state.loading && !state.busy) { Text(stringResource(R.string.wl_refresh)) }
                    TextButton(onClick = viewModel::refreshEpisodes, enabled = !state.loading && !state.busy && !state.episodesChecking) {
                        Text(stringResource(R.string.wl_check_episodes))
                    }
                    if (state.episodesChecking) Text(stringResource(R.string.wl_checking_episodes), color = CinemaMuted)
                    if (state.episodesCheckFailed) Text(stringResource(R.string.wl_episodes_check_failed), color = CinemaMuted)
                    WatchFeedback(state)
                    if (state.loading || state.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(stringResource(if (state.busy) R.string.wl_saving else R.string.wl_loading)) }
                    if (state.error != null) TextButton(onClick = viewModel::openList, enabled = !state.busy) { Text(stringResource(R.string.wl_retry)) }
                }
                if (state.entries.isEmpty() && !state.loading && state.error == null) item {
                    Text(stringResource(R.string.wl_empty), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.wl_empty_description), color = CinemaMuted)
                }
                items(state.entries, key = { it.catalog.key.documentId }) { entry ->
                    WatchCard(entry, state.pendingEpisodes[entry.catalog.key], state.busy, onDetails = {
                        nav.navigate((if (entry.catalog.key.type == MediaType.Movie) AppScreen.FilmID.route else AppScreen.SerieID.route) + "/${entry.catalog.key.tmdbId}")
                    }, onManage = { viewModel.openEditor(entry.catalog.key) })
                }
                if (state.hasMore) item { Button(onClick = viewModel::loadMore, enabled = !state.loading && !state.busy) { Text(stringResource(R.string.wl_more)) } }
            }
            item { Credits() }
        }
        if (uid != null && uid == state.uid) WatchEditorDialog(state, viewModel)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class) // FilterChip in the existing Material 3 version.
private fun WatchFilters(filter: WatchFilter, onChange: (WatchFilter) -> Unit) {
    Column {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null, MediaType.Movie, MediaType.Tv).forEach { type ->
                FilterChip(filter.type == type, { onChange(filter.copy(type = type)) }, modifier = Modifier.testTag("wl_type_${type?.value ?: "all"}"), label = {
                    Text(stringResource(when (type) { null -> R.string.wl_all; MediaType.Movie -> R.string.wl_movies; MediaType.Tv -> R.string.wl_series }))
                })
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null, *WatchStatus.entries.toTypedArray()).forEach { status ->
                FilterChip(filter.status == status, { onChange(filter.copy(status = status)) }, label = { Text(stringResource(status?.let(::statusResource) ?: R.string.wl_all)) })
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WatchSort.entries.forEach { sort -> FilterChip(filter.sort == sort, { onChange(filter.copy(sort = sort)) },
                label = { Text(stringResource(if (sort == WatchSort.Added) R.string.wl_added_sort else R.string.wl_updated_sort)) }) }
        }
    }
}

@Composable
private fun WatchCard(entry: WatchEntry, pending: PendingEpisodes?, busy: Boolean, onDetails: () -> Unit, onManage: () -> Unit) {
    Surface(color = CinemaSurface, shape = RoundedCornerShape(18.dp), modifier = Modifier.testTag("wl_entry_${entry.catalog.key.documentId}")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AsyncImage(imageUrl(entry.catalog.posterPath), entry.catalog.title, Modifier.width(80.dp).height(120.dp), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(entry.catalog.title, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(if (entry.catalog.key.type == MediaType.Movie) R.string.wl_movie else R.string.wl_series), color = CinemaMuted)
                    Text(stringResource(R.string.wl_release, entry.catalog.releaseDate ?: stringResource(R.string.wl_unknown)), color = CinemaMuted)
                    entry.catalog.runtimeMinutes?.let { Text(stringResource(R.string.wl_runtime, it)) }
                    Text(stringResource(statusResource(entry.status)), color = CinemaAccent)
                }
            }
            ProgressSummary(entry)
            pending?.let { backlog ->
                if (backlog.needsPosition) Text(stringResource(R.string.wl_pending_needs_position), color = CinemaMuted)
                else if (backlog.count > 0) Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pluralStringResource(R.plurals.wl_pending_episodes, backlog.count, backlog.count), style = MaterialTheme.typography.titleSmall)
                        backlog.next?.let { Text(stringResource(R.string.wl_pending_next, it.season, it.number)) }
                        backlog.newSeason?.let { Text(stringResource(R.string.wl_pending_new_season, it)) }
                        Text(stringResource(R.string.wl_episode_availability_note), style = MaterialTheme.typography.bodySmall)
                    }
                } else Text(stringResource(R.string.wl_no_pending_episodes), color = CinemaMuted)
            }
            ProviderPanel(entry.catalog.availability)
            TextButton(onClick = onDetails) { Text(entry.catalog.title) }
            Button(onClick = onManage, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.wl_manage)) }
        }
    }
}

@Composable
fun ProgressSummary(entry: WatchEntry) {
    when (val progress = entry.progress) {
        is WatchProgress.Movie -> {
            val minute = progress.minute
            val runtime = entry.catalog.runtimeMinutes
            Text(if (minute == null) stringResource(R.string.wl_no_position)
                else if (runtime == null) stringResource(R.string.wl_position_unknown_total, minute)
                else stringResource(R.string.wl_movie_position, minute, runtime))
            if (minute != null && runtime != null) LinearProgressIndicator((minute.toFloat() / runtime).coerceIn(0f, 1f), Modifier.fillMaxWidth())
        }
        is WatchProgress.Series -> {
            progress.current?.let { position ->
                val episode = position.episode
                val summary = if (position.minute == null) stringResource(R.string.wl_episode_position, episode.season, episode.number)
                    else stringResource(R.string.wl_episode_minute, episode.season, episode.number, position.minute)
                Text(if (position.nextToWatch) stringResource(R.string.wl_next_episode, summary) else summary)
                episode.runtimeMinutes?.let { Text(stringResource(R.string.wl_episode_runtime, it), color = CinemaMuted) }
            }
            progress.lastCompleted?.let { Text(stringResource(R.string.wl_completed_episode, stringResource(R.string.wl_episode_position, it.season, it.number))) }
            if (progress.current == null && progress.lastCompleted == null) Text(stringResource(R.string.wl_no_position))
        }
    }
}

@Composable
private fun ProviderPanel(availability: Availability?) {
    val uri = LocalUriHandler.current
    Text(stringResource(R.string.wl_providers_title, availability?.region ?: "ES"), style = MaterialTheme.typography.titleSmall)
    val groups = availability?.let { listOf(R.string.wl_subscription to it.subscription, R.string.wl_free to it.free,
        R.string.wl_ads to it.ads, R.string.wl_rent to it.rent, R.string.wl_buy to it.buy) }.orEmpty()
    if (groups.all { it.second.isEmpty() }) Text(stringResource(R.string.wl_no_providers), color = CinemaMuted)
    groups.filter { it.second.isNotEmpty() }.forEach { (title, providers) ->
        Text(stringResource(R.string.wl_provider_group, stringResource(title), providers.joinToString { it.name }))
    }
    availability?.checkedAtMillis?.let { Text(stringResource(R.string.wl_checked_at, DateFormat.getDateInstance().format(Date(it))), style = MaterialTheme.typography.bodySmall) }
    Text(stringResource(R.string.wl_justwatch), style = MaterialTheme.typography.bodySmall, color = CinemaMuted)
    availability?.link?.takeIf { it.startsWith("https://www.themoviedb.org/") }?.let { link ->
        TextButton(onClick = { runCatching { uri.openUri(link) } }) { Text(stringResource(R.string.wl_provider_link)) }
    }
}

@Composable
private fun Credits() {
    val uri = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.wl_credits), style = MaterialTheme.typography.titleSmall)
        Image(painterResource(R.drawable.tmdb_logo), stringResource(R.string.wl_tmdb), Modifier.width(92.dp).height(24.dp), contentScale = ContentScale.Fit)
        TextButton(onClick = { uri.openUri("https://www.themoviedb.org") }) { Text(stringResource(R.string.wl_tmdb)) }
        Text(stringResource(R.string.wl_tmdb_notice), style = MaterialTheme.typography.bodySmall, color = CinemaMuted)
        Text(stringResource(R.string.wl_justwatch), style = MaterialTheme.typography.bodySmall, color = CinemaMuted)
    }
}

@Composable
fun DetailWatchlistAction(key: MediaKey, nav: NavController, viewModel: WatchlistViewModel, auth: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session by auth.state.collectAsStateWithLifecycle()
    val uid = (session.session as? AuthSession.Authenticated)?.user?.uid
    var login by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key, uid, state.uid) { if (uid != null && uid == state.uid) viewModel.inspect(key) }
    val known = uid != null && uid == state.uid && state.memberships.containsKey(key)
    val entry = if (known) state.memberships[key] else null
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            if (uid == null) login = true
            else if (!known) viewModel.inspect(key)
            else if (entry == null) viewModel.add(key) else viewModel.openEditor(key)
        }, enabled = !state.busy && key !in state.checking, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (entry != null) R.string.wl_in_list else if (uid != null && !known) R.string.wl_retry else R.string.wl_add))
        }
        if (state.busy || key in state.checking) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (uid != null && uid == state.uid) WatchFeedback(state)
    }
    if (login) AlertDialog(onDismissRequest = { login = false }, title = { Text(stringResource(R.string.wl_auth_title)) },
        text = { Text(stringResource(R.string.wl_auth_add)) }, confirmButton = {
            TextButton(onClick = { viewModel.deferAdd(key); login = false; nav.navigate(AppScreen.Account.route) { launchSingleTop = true } }) { Text(stringResource(R.string.wl_sign_in)) }
        }, dismissButton = { TextButton(onClick = { login = false }) { Text(stringResource(R.string.wl_cancel)) } })
    if (uid != null && uid == state.uid) WatchEditorDialog(state, viewModel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchEditorDialog(state: WatchlistUiState, viewModel: WatchlistViewModel) {
    val editor = state.editor ?: return
    val entry = editor.entry
    key(state.uid, entry.catalog.key, entry.progressVersion) {
        var status by remember { mutableStateOf(entry.status) }
        val selected = editor.episodes.firstOrNull { it.id == editor.selectedEpisodeId }
        var minute by remember(editor.selectedEpisodeId) { mutableStateOf(when (val p = entry.progress) {
            is WatchProgress.Movie -> p.minute?.toString().orEmpty()
            is WatchProgress.Series -> p.current?.takeIf { it.episode.id == editor.selectedEpisodeId }?.minute?.toString().orEmpty()
        }) }
        var delete by remember { mutableStateOf(false) }
        var reset by remember { mutableStateOf(false) }
        val progressed = when (val p = entry.progress) { is WatchProgress.Movie -> p.minute != null; is WatchProgress.Series -> p.current != null || p.lastCompleted != null }
        fun save() {
            if (status != entry.status && progressed && (status != WatchStatus.InProgress || entry.status == WatchStatus.Watched)) reset = true
            else viewModel.saveEditor(status, minute)
        }
        AlertDialog(onDismissRequest = { viewModel.closeEditor() }, title = { Text(entry.catalog.title) },
            text = {
                Column(Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.wl_manual), color = CinemaMuted)
                    ProgressSummary(entry)
                    Dropdown(stringResource(R.string.wl_state), stringResource(statusResource(status)), WatchStatus.entries,
                        label = { stringResource(statusResource(it)) }, enabled = !state.busy, onSelect = { next ->
                            if (entry.status == WatchStatus.Watched && next == WatchStatus.InProgress) minute = ""
                            status = next
                        })
                    if (entry.catalog.key.type == MediaType.Tv && status == WatchStatus.InProgress) {
                        Dropdown(stringResource(R.string.wl_choose_season), editor.selectedSeason?.let { seasonLabel(it) } ?: stringResource(R.string.wl_unknown),
                            editor.seasons, label = { seasonLabel(it.number) }, enabled = !state.busy, onSelect = { viewModel.selectSeason(it.number) })
                        if (editor.episodesLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Dropdown(stringResource(R.string.wl_choose_episode), selected?.let { episodeLabel(it) } ?: stringResource(R.string.wl_unknown),
                            editor.episodes, label = { episodeLabel(it) }, enabled = !state.busy && !editor.episodesLoading,
                            itemEnabled = { it.selectable(LocalDate.now()) }, onSelect = { viewModel.selectEpisode(it.id) })
                        if (editor.episodes.isEmpty() && !editor.episodesLoading) {
                            Text(stringResource(R.string.wl_no_episodes))
                            TextButton(onClick = { editor.selectedSeason?.let { viewModel.selectSeason(it, refresh = true) } }, enabled = !state.busy) { Text(stringResource(R.string.wl_retry)) }
                        }
                        selected?.runtimeMinutes?.let { Text(stringResource(R.string.wl_episode_runtime, it)) }
                    }
                    if (status == WatchStatus.InProgress) OutlinedTextField(minute, { minute = it }, modifier = Modifier.fillMaxWidth().testTag("wl_minute"),
                        label = { Text(stringResource(R.string.wl_minute)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, enabled = !state.busy)
                    if (entry.catalog.key.type == MediaType.Tv && status == WatchStatus.InProgress) {
                        TextButton(onClick = { viewModel.saveEditor(status, minute) }, enabled = !state.busy && !editor.episodesLoading && selected != null) { Text(stringResource(R.string.wl_watching_episode)) }
                        OutlinedButton(onClick = viewModel::finishEpisode, enabled = !state.busy && !editor.episodesLoading && selected != null) { Text(stringResource(R.string.wl_finish_episode)) }
                    }
                    ProviderPanel(entry.catalog.availability)
                    TextButton(onClick = { viewModel.refreshProviders(entry.catalog.key) }, enabled = !state.busy) { Text(stringResource(R.string.wl_refresh_providers)) }
                    WatchFeedback(state)
                    if (state.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(stringResource(R.string.wl_saving)) }
                    TextButton(onClick = { delete = true }, enabled = !state.busy) { Text(stringResource(R.string.wl_remove)) }
                }
            }, confirmButton = { TextButton(onClick = { save() }, enabled = !state.busy && !editor.episodesLoading) { Text(stringResource(R.string.wl_save)) } },
            dismissButton = { TextButton(onClick = viewModel::closeEditor, enabled = !state.busy) { Text(stringResource(R.string.wl_close)) } })
        if (delete) AlertDialog(onDismissRequest = { delete = false }, title = { Text(stringResource(R.string.wl_delete_title)) },
            text = { Text(stringResource(R.string.wl_delete_description, entry.catalog.title)) },
            confirmButton = { TextButton(onClick = { delete = false; viewModel.delete(entry.catalog.key) }) { Text(stringResource(R.string.wl_confirm)) } },
            dismissButton = { TextButton(onClick = { delete = false }) { Text(stringResource(R.string.wl_cancel)) } })
        if (reset) AlertDialog(onDismissRequest = { reset = false }, title = { Text(stringResource(R.string.wl_reset_title)) },
            text = { Text(stringResource(R.string.wl_reset_description)) },
            confirmButton = { TextButton(onClick = { reset = false; viewModel.saveEditor(status, minute) }) { Text(stringResource(R.string.wl_confirm)) } },
            dismissButton = { TextButton(onClick = { reset = false }) { Text(stringResource(R.string.wl_cancel)) } })
    }
}

@Composable
private fun <T> Dropdown(title: String, selected: String, items: List<T>, label: @Composable (T) -> String,
    enabled: Boolean, itemEnabled: (T) -> Boolean = { true }, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled) { Text(selected) }
            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 280.dp)) {
                items.forEach { item -> DropdownMenuItem(text = { Text(label(item)) }, onClick = { expanded = false; onSelect(item) }, enabled = itemEnabled(item)) }
            }
        }
    }
}

@Composable private fun seasonLabel(number: Int) = stringResource(if (number == 0) R.string.wl_specials else R.string.wl_season, number)
@Composable private fun episodeLabel(episode: Episode) = stringResource(R.string.wl_episode, episode.number, episode.title ?: stringResource(R.string.wl_unknown)) +
    if (!episode.selectable(LocalDate.now())) " · " + stringResource(R.string.wl_not_aired) else ""
private fun statusResource(status: WatchStatus) = when (status) { WatchStatus.Pending -> R.string.wl_pending; WatchStatus.InProgress -> R.string.wl_in_progress; WatchStatus.Watched -> R.string.wl_watched }

@Composable private fun WatchFeedback(state: WatchlistUiState) {
    state.error?.let { Text(stringResource(when (it) {
        WatchFailure.Authentication, WatchFailure.Permission -> R.string.wl_error_permission
        WatchFailure.Configuration -> R.string.wl_error_configuration
        WatchFailure.Network -> R.string.wl_error_network
        WatchFailure.Conflict -> R.string.wl_error_conflict
        WatchFailure.Validation -> R.string.wl_error_validation
        WatchFailure.Unknown -> R.string.wl_error_unknown
    }), color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
    state.notice?.let { Text(stringResource(when (it) {
        WatchNotice.Added -> R.string.wl_added; WatchNotice.Saved -> R.string.wl_saved
        WatchNotice.Deleted -> R.string.wl_deleted; WatchNotice.ProvidersUpdated -> R.string.wl_providers_updated
    }), color = CinemaAccent, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
}
