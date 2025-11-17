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
    // Accept an optional Firestore instance to facilitate testing; if null, resolve lazily.
    private val providedFirestore: FirebaseFirestore? = null
) {
    private val TAG = "ItemRepository"
    // Lazily initialize Firestore to avoid evaluating getInstance() at call site (important for tests)
    private val firestore: FirebaseFirestore by lazy { providedFirestore ?: FirebaseFirestore.getInstance() }
    // Firestore collection name
    private val COLLECTION_NAME = "items"

    /**
     * Get all items as a Flow with real-time updates
     */
    fun getAllItems(): Flow<List<Item>> = callbackFlow {
        // Listen to Firestore collection changes
        val listener = firestore.collection(COLLECTION_NAME)
            .addSnapshotListener { snapshot, error ->
                // Handle errors
                if (error != null) {
                    Log.e(TAG, "Error listening to items: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                // Parse documents to Item objects
                if (snapshot != null) {
                    // Map documents to Item instances, handling parsing errors
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Use fromMap to create Item
                            Item.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing item: ${e.message}")
                            null
                        }
                    }
                    // Send updated list to the Flow
                    trySend(items)
                    Log.d(TAG, "Fetched ${items.size} items from Firestore")
                }
            }
        // Clean up listener on Flow cancellation
        awaitClose { listener.remove() }
    }

    /**
     * Insert a new item to Firestore
     */
    suspend fun insert(name: String, reserved: Boolean = false): Result<String> {
        // Return Result with new document ID or error
        return try {
            val data = hashMapOf(
                "name" to name,
                "reserved" to reserved,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            // Add document to Firestore
            val docRef = firestore.collection(COLLECTION_NAME).add(data).await()
            Log.d(TAG, "Item inserted: $name (ID: ${docRef.id})")
            // Return the new document ID
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
            // Ensure item has a valid ID
            if (item.id.isNotEmpty()) {
                // Delete document by ID
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
                // Prepare updated data
                val data = hashMapOf(
                    "name" to item.name,
                    "reserved" to item.reserved,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                // Update document by ID
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
            // Return the number of documents
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
            // Query Firestore for item with matching name
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
    // Get item by ID as Item
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