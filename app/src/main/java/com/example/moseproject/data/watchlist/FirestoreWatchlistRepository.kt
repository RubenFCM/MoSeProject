package com.example.moseproject.data.watchlist

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreWatchlistRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) : WatchlistRepository {
    private fun collection(uid: String) = db.collection("users").document(uid).collection("watchlist")
    private fun authorize(uid: String) {
        val user = auth.currentUser
        if (user == null || user.uid != uid || !user.isEmailVerified) throw WatchException(WatchFailure.Authentication)
    }

    override suspend fun page(uid: String, filter: WatchFilter, after: PageCursor?): WatchPage = request(uid) {
        var query: Query = collection(uid)
        filter.type?.let { query = query.whereEqualTo("mediaType", it.value) }
        filter.status?.let { query = query.whereEqualTo("status", it.value) }
        query = query.orderBy(filter.sort.field, Query.Direction.DESCENDING).orderBy(FieldPath.documentId(), Query.Direction.DESCENDING).limit(30)
        after?.let { query = query.startAfter(Timestamp(it.seconds, it.nanoseconds), it.documentId) }
        val documents = query.get(Source.SERVER).await().documents
        val cursor = documents.lastOrNull()?.let { doc -> doc.getTimestamp(filter.sort.field)?.let { PageCursor(it.seconds, it.nanoseconds, doc.id) } }
        WatchPage(documents.map { it.watchEntry() }, cursor, documents.size == 30)
    }

    override suspend fun get(uid: String, key: MediaKey): WatchEntry? = request(uid) {
        collection(uid).document(key.documentId).get(Source.SERVER).await().takeIf { it.exists() }?.watchEntry()
    }

    override suspend fun add(uid: String, metadata: CatalogMetadata): WatchEntry = request(uid) {
        val progress: WatchProgress = if (metadata.key.type == MediaType.Movie) WatchProgress.Movie() else WatchProgress.Series()
        ProgressPolicy.validate(WatchEntry(metadata, WatchStatus.Pending, progress))
        val doc = collection(uid).document(metadata.key.documentId)
        db.runTransaction { transaction ->
            if (!transaction.get(doc).exists()) {
                transaction.set(doc, mapOf("tmdbId" to metadata.key.tmdbId, "mediaType" to metadata.key.type.value,
                    "catalog" to metadata.toDocument(), "status" to WatchStatus.Pending.value, "progress" to progress.toDocument(),
                    "progressVersion" to 0, "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()))
            }
        }.await()
        // Task completion is the acknowledgement, never a local optimistic snapshot.
        doc.get(Source.SERVER).await().watchEntry()
    }

    override suspend fun saveProgress(uid: String, entry: WatchEntry, status: WatchStatus, progress: WatchProgress): WatchEntry = request(uid) {
        val doc = collection(uid).document(entry.catalog.key.documentId)
        db.runTransaction { tx ->
            val current = tx.get(doc).watchEntry()
            if (current.progressVersion != entry.progressVersion) throw WatchException(WatchFailure.Conflict)
            ProgressPolicy.validate(current.copy(status = status, progress = progress))
            tx.update(doc, mapOf("status" to status.value, "progress" to progress.toDocument(),
                "progressVersion" to current.progressVersion + 1, "updatedAt" to FieldValue.serverTimestamp()))
        }.await()
        doc.get(Source.SERVER).await().watchEntry()
    }

    override suspend fun refreshProviders(uid: String, key: MediaKey, availability: Availability): WatchEntry = request(uid) {
        val doc = collection(uid).document(key.documentId)
        db.runTransaction { tx ->
            if (!tx.get(doc).exists()) throw WatchException(WatchFailure.Conflict)
            tx.update(doc, mapOf("catalog.providers" to availability.toDocument(), "updatedAt" to FieldValue.serverTimestamp()))
        }.await()
        doc.get(Source.SERVER).await().watchEntry()
    }

    override suspend fun delete(uid: String, key: MediaKey) = request(uid) {
        val doc = collection(uid).document(key.documentId)
        // A transaction requires the server and avoids queued offline deletions.
        db.runTransaction { tx -> if (tx.get(doc).exists()) tx.delete(doc); Unit }.await()
    }

    private suspend fun <T> request(uid: String, block: suspend () -> T): T {
        authorize(uid)
        return try { block().also { authorize(uid) } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (known: WatchException) { throw known }
        catch (error: FirebaseFirestoreException) {
            val reason = when (error.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> WatchFailure.Permission
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> WatchFailure.Authentication
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> WatchFailure.Configuration
                FirebaseFirestoreException.Code.ABORTED -> WatchFailure.Conflict
                FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> WatchFailure.Network
                else -> (error.cause as? WatchException)?.reason ?: WatchFailure.Unknown
            }
            throw WatchException(reason)
        } catch (error: Exception) {
            throw WatchException((error.cause as? WatchException)?.reason ?: WatchFailure.Unknown)
        }
    }
}
