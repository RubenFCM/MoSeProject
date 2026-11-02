package com.example.moseproject.watchlist

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.R
import com.example.moseproject.data.auth.*
import com.example.moseproject.data.watchlist.*
import com.example.moseproject.ui.theme.MoSeProjectTheme
import com.example.moseproject.ui.view.watchlist.WatchlistScreen
import com.example.moseproject.ui.viewmodel.AuthViewModel
import com.example.moseproject.ui.viewmodel.WatchlistViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WatchlistScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val authRepo = UiWatchAuth()
    private val repository = UiWatchRepository()
    private val catalog = UiWatchCatalog()
    private lateinit var auth: AuthViewModel
    private lateinit var model: WatchlistViewModel
    @Before fun setup() {
        compose.activityRule.scenario.onActivity { activity ->
            auth = AuthViewModel(authRepo, PasswordPolicy())
            model = WatchlistViewModel(repository, catalog, auth.state, SavedStateHandle())
            activity.viewModelStore.put("auth", auth); activity.viewModelStore.put("watchlist", model)
        }
    }
    private fun show(fontScale: Float = 1f) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MoSeProjectTheme {
                    val nav = rememberNavController()
                    NavHost(nav, startDestination = AppScreen.Watchlist.route) {
                        composable(AppScreen.Watchlist.route) { WatchlistScreen(nav, model, auth) }
                        listOf(AppScreen.HomeScreen.route, AppScreen.FilmScreen.route, AppScreen.SerieScreen.route,
                            AppScreen.PeopleScreen.route, AppScreen.Account.route).forEach { route ->
                            composable(route) { Text("Public test destination") }
                        }
                    }
                }
            }
        }
    }
    private fun text(id: Int) = compose.activity.getString(id)
    @Test fun pendingEpisodeAppearsOnFirstVisitAndClearsOnlyAsProgressAdvancesWithLargeFonts() {
        val ten = Episode(10, 1, 10, "Episode ten", "2026-01-01", 45)
        val eleven = ten.copy(id = 11, number = 11, title = "Episode eleven")
        catalog.available = listOf(ten, eleven)
        repository.entry = WatchEntry(CatalogMetadata(MediaKey(MediaType.Tv, 123), "UI test series", null, null, null),
            WatchStatus.InProgress, WatchProgress.Series(EpisodePosition(ten, 15)))
        show(1.6f)
        val notice = compose.activity.resources.getQuantityString(R.plurals.wl_pending_episodes, 1, 1)
        compose.onNodeWithText(notice).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.wl_pending_next, 1, 11)).assertExists()
        compose.onNodeWithText(text(R.string.wl_manage)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.wl_finish_episode)).performScrollTo().performClick()
        compose.onNodeWithText(notice).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.wl_manage)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.wl_finish_episode)).performScrollTo().performClick()
        compose.onNodeWithText(notice).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.wl_no_pending_episodes)).assertExists()
    }
    @Test fun pendingEpisodesInFollowingSeasonAppearWithoutAReleaseBaseline() {
        val finale = Episode(10, 1, 10, "Finale", "2026-01-01", 45)
        val premiere = finale.copy(id = 20, season = 2, number = 1)
        catalog.available = listOf(finale, premiere)
        repository.entry = WatchEntry(CatalogMetadata(MediaKey(MediaType.Tv, 123), "UI test series", null, null, null),
            WatchStatus.InProgress, WatchProgress.Series(lastCompleted = finale))
        show()
        compose.onNodeWithText(compose.activity.getString(R.string.wl_pending_next, 2, 1)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.wl_pending_new_season, 2)).assertExists()
    }
    @Test fun progressIsSavedOnlyOnConfirmationAndWorksWithLargeFonts() {
        show(1.6f)
        compose.onNodeWithText(text(R.string.wl_manage)).performScrollTo().performClick()
        compose.onNodeWithTag("wl_minute").performScrollTo().performTextReplacement("45")
        compose.runOnIdle { check(repository.entry!!.progress == WatchProgress.Movie(10)) }
        compose.onNodeWithText(text(R.string.wl_save)).performClick()
        compose.runOnIdle { check(repository.entry!!.progress == WatchProgress.Movie(45)); check(repository.writes == 1) }
    }
    @Test fun deleteRequiresConfirmationAndFiltersAreApplied() {
        show()
        compose.onNodeWithTag("wl_type_tv").performClick()
        compose.onNodeWithText(text(R.string.wl_empty)).assertExists()
        compose.onNodeWithTag("wl_type_movie").performClick()
        compose.onNodeWithText(text(R.string.wl_manage)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.wl_remove)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.wl_cancel)).performClick()
        compose.runOnIdle { check(repository.entry != null) }
        compose.onNodeWithText(text(R.string.wl_remove)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.wl_confirm)).performClick()
        compose.onNodeWithText(text(R.string.wl_empty)).assertExists()
    }
    @Test fun sessionLossRemovesTheEditorAndPrivateRows() {
        show()
        compose.onNodeWithText(text(R.string.wl_manage)).performScrollTo().performClick()
        compose.onNodeWithTag("wl_minute").assertExists()
        compose.runOnIdle { authRepo.current.value = null }
        compose.onNodeWithTag("wl_minute").assertDoesNotExist()
        compose.onNodeWithTag("wl_entry_movie_123").assertDoesNotExist()
        compose.onNodeWithText(text(R.string.wl_auth_description)).assertExists()
    }
}

private class UiWatchAuth : AuthRepository {
    val current = MutableStateFlow<AuthUser?>(AuthUser("ui-test", "viewer@example.test", true))
    override val users = current
    override suspend fun refreshSession(): AuthSession = current.value?.let { AuthSession.Authenticated(it) } ?: AuthSession.SignedOut
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun register(email: String, password: String) = Unit
    override suspend fun sendVerification() = Unit
    override suspend fun sendPasswordReset(email: String) = Unit
    override suspend fun signOut() { current.value = null }
}
private class UiWatchRepository : WatchlistRepository {
    var entry: WatchEntry? = WatchEntry(CatalogMetadata(MediaKey(MediaType.Movie, 123), "UI test movie", null, null, 120), WatchStatus.InProgress, WatchProgress.Movie(10))
    var writes = 0
    override suspend fun page(uid: String, filter: WatchFilter, after: PageCursor?): WatchPage = WatchPage(listOfNotNull(entry).filter {
        (filter.type == null || filter.type == it.catalog.key.type) && (filter.status == null || filter.status == it.status)
    }, null, false)
    override suspend fun get(uid: String, key: MediaKey) = entry
    override suspend fun add(uid: String, metadata: CatalogMetadata) = entry!!
    override suspend fun saveProgress(uid: String, entry: WatchEntry, status: WatchStatus, progress: WatchProgress): WatchEntry {
        writes++
        return entry.copy(status = status, progress = progress, progressVersion = entry.progressVersion + 1).also { this.entry = it }
    }
    override suspend fun refreshProviders(uid: String, key: MediaKey, availability: Availability) = entry!!
    override suspend fun delete(uid: String, key: MediaKey) { entry = null }
}
private class UiWatchCatalog : WatchlistCatalog {
    var available: List<Episode> = emptyList()
    override suspend fun latestAired(key: MediaKey): Episode? = available.lastOrNull()
    override suspend fun metadata(key: MediaKey): CatalogMetadata = error("UI test must not query TMDB")
    override suspend fun providers(key: MediaKey, region: String) = Availability(region)
    override suspend fun seasons(key: MediaKey) = available.map { Season(it.season, null, null) }.distinctBy { it.number }
    override suspend fun episodes(key: MediaKey, season: Int, refresh: Boolean) = available.filter { it.season == season }
    override suspend fun nextAired(key: MediaKey, episode: Episode): Episode? = available.firstOrNull {
        it.season > episode.season || (it.season == episode.season && it.number > episode.number)
    }
}
