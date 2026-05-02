package com.example.eureka
import androidx.compose.ui.graphics.Color
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StrokeRepository @Inject constructor() {

    private val db = Firebase.firestore

    suspend fun saveStroke(spaceId: String, stroke: Stroke) {
        val data = hashMapOf(
            "authorId"  to stroke.authorId,
            "color"     to stroke.color.toHex(),
            "width"     to stroke.width,
            "isPublic"  to stroke.isPublic,
            "createdAt" to Timestamp.now(),
            "points"    to stroke.points.map { pt ->
                hashMapOf("x" to pt[0], "y" to pt[1], "z" to pt[2])
            }
        )
        db.collection("spaces")
            .document(spaceId)
            .collection("strokes")
            .document(stroke.id)
            .set(data)
            .await()
    }

    fun observeStrokes(spaceId: String, currentUserId: String): Flow<List<Stroke>> =
        callbackFlow {
            val listener = db.collection("spaces")
                .document(spaceId)
                .collection("strokes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val strokes = snapshot?.documents?.mapNotNull { doc ->
                        val isPublic = doc.getBoolean("isPublic") ?: true
                        val authorId = doc.getString("authorId") ?: ""
                        if (!isPublic && authorId != currentUserId) return@mapNotNull null
                        val rawPoints = doc.get("points") as? List<Map<String, Any>>
                            ?: return@mapNotNull null
                        val points = rawPoints.map { pt ->
                            floatArrayOf(
                                (pt["x"] as? Number)?.toFloat() ?: 0f,
                                (pt["y"] as? Number)?.toFloat() ?: 0f,
                                (pt["z"] as? Number)?.toFloat() ?: 0f
                            )
                        }
                        Stroke(
                            id       = doc.id,
                            points   = points,
                            color    = Color(android.graphics.Color.parseColor(
                                doc.getString("color") ?: "#FFFFFF"
                            )),
                            width    = (doc.getDouble("width") ?: 8.0).toFloat(),
                            authorId = authorId,
                            isPublic = isPublic
                        )
                    } ?: emptyList()
                    trySend(strokes)
                }
            awaitClose { listener.remove() }
        }

    suspend fun deleteStroke(spaceId: String, strokeId: String) {
        db.collection("spaces")
            .document(spaceId)
            .collection("strokes")
            .document(strokeId)
            .delete()
            .await()
    }

    suspend fun createSpace(cloudAnchorId: String, userId: String): String {
        val spaceRef = db.collection("spaces").document()
        spaceRef.set(
            hashMapOf(
                "cloudAnchorId" to cloudAnchorId,
                "createdBy"     to userId,
                "createdAt"     to Timestamp.now()
            )
        ).await()
        return spaceRef.id
    }

    suspend fun getCloudAnchorId(spaceId: String): String? {
        val doc = db.collection("spaces").document(spaceId).get().await()
        return doc.getString("cloudAnchorId")
    }
}

fun Color.toHex(): String =
    "#%02X%02X%02X".format(
        (red   * 255).toInt(),
        (green * 255).toInt(),
        (blue  * 255).toInt()
    )