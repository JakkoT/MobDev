package ee.ut.cs.iotbazaar.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import ee.ut.cs.iotbazaar.model.Item
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Item data using Firebase Firestore only.
 * No Room database - all data stored in cloud.
    */
class ItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "ItemRepository"
    private val COLLECTION_NAME = "items"

    /**
     * Get all items as a Flow with real-time updates
     */
    fun getAllItems(): Flow<List<Item>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_NAME)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to items: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            Item.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing item: ${e.message}")
                            null
                        }
                    }
                    trySend(items)
                    Log.d(TAG, "Fetched ${items.size} items from Firestore")
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Insert a new item to Firestore
     */
    suspend fun insert(name: String, reserved: Boolean = false): Result<String> {
        return try {
            val data = hashMapOf(
                "name" to name,
                "reserved" to reserved,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection(COLLECTION_NAME).add(data).await()
            Log.d(TAG, "Item inserted: $name (ID: ${docRef.id})")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting item: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Delete an item from Firestore
     */
    suspend fun delete(item: Item): Result<Unit> {
        return try {
            if (item.id.isNotEmpty()) {
                firestore.collection(COLLECTION_NAME)
                    .document(item.id)
                    .delete()
                    .await()
                Log.d(TAG, "Item deleted: ${item.name}")
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Item ID is empty"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Update an item in Firestore
     */
    suspend fun update(item: Item): Result<Unit> {
        return try {
            if (item.id.isNotEmpty()) {
                val data = hashMapOf(
                    "name" to item.name,
                    "reserved" to item.reserved,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection(COLLECTION_NAME)
                    .document(item.id)
                    .update(data as Map<String, Any>)
                    .await()
                Log.d(TAG, "Item updated: ${item.name}")
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Item ID is empty"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get count of items from Firestore
     */
    suspend fun count(): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME).get().await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error counting items: ${e.message}")
            0
        }
    }

    /**
     * Check if an item exists by name
     */
    suspend fun exists(name: String): Boolean {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking item existence: ${e.message}")
            false
        }
    }

    suspend fun getItemById(id: String): Item? {
        return try {
            val doc = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .await()

            if (doc.exists()) {
                val item = Item.fromMap(doc.id, doc.data ?: emptyMap())
                item
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
