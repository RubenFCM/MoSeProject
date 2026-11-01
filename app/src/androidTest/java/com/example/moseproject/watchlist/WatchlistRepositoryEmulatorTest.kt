package com.example.moseproject.watchlist

import androidx.test.platform.app.InstrumentationRegistry
import com.example.moseproject.BuildConfig
import com.example.moseproject.data.watchlist.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Real Android SDK + both local emulators. This named app can never address a real project. */
class WatchlistRepositoryEmulatorTest {
    private lateinit var app: FirebaseApp
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var repository: FirestoreWatchlistRepository
    private val key = MediaKey(MediaType.Movie, 123)
    private val metadata = CatalogMetadata(key, "Emulator-only movie", null, null, 120)

    @Before fun setup() {
        Assume.assumeTrue("Start both emulators and build with -PfirebaseAuthEmulator=true", BuildConfig.AUTH_USE_EMULATOR)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val options = FirebaseOptions.Builder().setProjectId(PROJECT).setApiKey("emulator-only-key")
            .setApplicationId("1:1234567890:android:0000000000000000").build()
        app = FirebaseApp.initializeApp(context, options, "watchlist-test-${UUID.randomUUID()}")
        auth = FirebaseAuth.getInstance(app).apply { useEmulator("10.0.2.2", 9099) }
        db = FirebaseFirestore.getInstance(app)
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        db.useEmulator("10.0.2.2", 8080)
        repository = FirestoreWatchlistRepository(db, auth)
    }

    @After fun cleanup() {
        if (::app.isInitialized) runBlocking {
            auth.signOut()
            db.terminate().await()
            app.delete()
        }
    }

    private suspend fun verifiedUser(): String {
        val email = "android-${UUID.randomUUID()}@example.test"
        val user = auth.createUserWithEmailAndPassword(email, "LocalTestOnly!123").await().user!!
        user.sendEmailVerification().await()
        val codes = json("GET", "http://10.0.2.2:9099/emulator/v1/projects/$PROJECT/oobCodes").getJSONArray("oobCodes")
        val code = (0 until codes.length()).map { codes.getJSONObject(it) }.first { it.getString("email") == email }.getString("oobCode")
        json("POST", "http://10.0.2.2:9099/identitytoolkit.googleapis.com/v1/accounts:update?key=emulator-only-key", JSONObject().put("oobCode", code))
        user.reload().await(); user.getIdToken(true).await()
        assertTrue(user.isEmailVerified)
        return user.uid
    }

    @Test fun transactionsPreserveProgressAndMetadataAndDeletionIsConfirmed() = runBlocking { withTimeout(60_000) {
        val uid = verifiedUser()
        val first = repository.add(uid, metadata)
        assertNotNull(first.createdAtMillis)
        assertNotNull(first.updatedAtMillis)
        val updated = repository.saveProgress(uid, first, WatchStatus.InProgress, WatchProgress.Movie(45))
        val duplicate = repository.add(uid, metadata.copy(title = "Must not overwrite"))
        assertEquals(updated.progress, duplicate.progress)
        assertEquals(metadata.title, duplicate.catalog.title)
        val refreshed = repository.refreshProviders(uid, key, Availability("ES"))
        assertEquals(WatchProgress.Movie(45), refreshed.progress)
        assertEquals(1L, refreshed.progressVersion)
        assertNotNull(refreshed.catalog.availability?.checkedAtMillis)
        assertEquals(1, repository.page(uid, WatchFilter(), null).entries.size)
        try {
            repository.saveProgress(uid, first, WatchStatus.Pending, WatchProgress.Movie())
            fail("Stale progress version must be rejected")
        } catch (error: WatchException) { assertEquals(WatchFailure.Conflict, error.reason) }
        repository.delete(uid, key)
        assertNull(repository.get(uid, key))
    } }

    @Test fun signOutAndSwitchingAccountsCannotExposeThePreviousPrivateList() = runBlocking { withTimeout(60_000) {
        val firstUid = verifiedUser()
        repository.add(firstUid, metadata)
        auth.signOut()
        try { repository.get(firstUid, key); fail("Signed-out access must fail") }
        catch (error: WatchException) { assertEquals(WatchFailure.Authentication, error.reason) }
        val secondUid = verifiedUser()
        assertTrue(repository.page(secondUid, WatchFilter(), null).entries.isEmpty())
        try {
            db.collection("users").document(firstUid).collection("watchlist").document(key.documentId).get(Source.SERVER).await()
            fail("Firestore must deny the other UID even when bypassing the repository")
        } catch (error: FirebaseFirestoreException) { assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, error.code) }
    } }

    private fun json(method: String, address: String, body: JSONObject? = null): JSONObject {
        val url = URL(address)
        check(url.host == "10.0.2.2" && url.port == 9099 && PROJECT.startsWith("demo-"))
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000; connection.readTimeout = 10_000
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            check(connection.responseCode == 200) { "Local Auth Emulator request failed" }
            return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally { connection.disconnect() }
    }
    private companion object { const val PROJECT = "demo-cineverse-watchlist" }
}
