package ee.ut.cs.iotbazaar.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import ee.ut.cs.iotbazaar.data.entities.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for User data using Firebase Firestore.
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "UserRepository"
    private val COLLECTION_NAME = "users"

    /**
     * Get all users as a Flow with real-time updates
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_NAME)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to users: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        try {
                            User.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing user: ${e.message}")
                            null
                        }
                    }
                    trySend(users)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Insert a new user to Firestore
     */
    suspend fun insert(user: User): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_NAME)
                .add(user.toMap())
                .await()
            Log.d(TAG, "User inserted: ${user.name} (ID: ${docRef.id})")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Delete a user from Firestore
     */
    suspend fun delete(user: User): Result<Unit> {
        return try {
            if (user.id.isNotEmpty()) {
                firestore.collection(COLLECTION_NAME)
                    .document(user.id)
                    .delete()
                    .await()
                Log.d(TAG, "User deleted: ${user.name}")
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("User ID is empty"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if a user exists by name
     */
    suspend fun existsByName(name: String): Boolean {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user existence: ${e.message}")
            false
        }
    }

    /**
     * Insert user if not exists
     */
    suspend fun insertIfNotExists(name: String, age: Int): Result<String?> {
        return try {
            if (!existsByName(name)) {
                val user = User(name = name, age = age)
                insert(user)
            } else {
                Log.d(TAG, "User already exists: $name")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertIfNotExists: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Update a user in Firestore
     */
    suspend fun update(user: User): Result<Unit> {
        return try {
            if (user.id.isNotEmpty()) {
                firestore.collection(COLLECTION_NAME)
                    .document(user.id)
                    .update(user.toMap())
                    .await()
                Log.d(TAG, "User updated: ${user.name}")
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("User ID is empty"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}")
            Result.failure(e)
        }
    }
}
fun addTakenItemToUser(itemId: String, returnDateMillis: Long, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: run {
        onResult(false)
        return
    }

    val userRef = db.collection("users").document(user.uid)

    val takenItemMap = mapOf(
        "itemId" to itemId,
        "returnDate" to returnDateMillis
    )

    // Lisa massiivi
    userRef.update("takenItems", com.google.firebase.firestore.FieldValue.arrayUnion(takenItemMap))
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { e ->
            // Kui massiivi field veel ei eksisteeri, loo see
            val data = mapOf("takenItems" to listOf(takenItemMap))
            userRef.set(data, SetOptions.merge())
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
}