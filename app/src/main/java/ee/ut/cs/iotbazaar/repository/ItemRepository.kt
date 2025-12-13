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
    suspend fun insert(name: String, stock: Int = 1): Result<String> {
        // Return Result with new document ID or error
        return try {
            val data = hashMapOf(
                "name" to name,
                "stock" to stock,
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
    /**
     * Reserve item: decreases stock by 1 if available and sets reserved=true.
     */
    suspend fun reserveItemForUser(userId: String, itemId: String, returnDate: Long): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users_real").document(userId)
            val itemRef = db.collection("items").document(itemId)

            db.runTransaction { transaction ->

                // Loe või loo user dokumendi transactioni sees
                val userSnap = try {
                    transaction.get(userRef)
                } catch (e: Exception) {
                    // Kui dokument puudub, loo see tühja takenItems listiga
                    transaction.set(userRef, mapOf("takenItems" to emptyList<Map<String, Any>>()))
                    transaction.get(userRef)
                }

                // Loe item dokumendi transactioni sees
                val itemSnap = transaction.get(itemRef)
                if (!itemSnap.exists()) throw Exception("Item does not exist")

                // Default to 3 if stock field is missing (legacy items)
                val stock = itemSnap.getLong("stock")?.toInt() ?: 3
                if (stock <= 0) throw Exception("Item out of stock")

                val newStock = stock - 1

                transaction.update(itemRef, mapOf(
                    "stock" to newStock,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ))

                val takenItems = userSnap.get("takenItems") as? List<Map<String, Any>> ?: emptyList()

                val updatedTakenItems = if (takenItems.any { it["itemId"] == itemId }) {
                    takenItems.map {
                        if (it["itemId"] == itemId) {
                            it.toMutableMap().apply {
                                this["stock"] = ((this["stock"] as? Long ?: 0) + 1)
                                this["returnDate"] = returnDate
                            }
                        } else it
                    }
                } else {
                    takenItems + listOf(mapOf(
                        "itemId" to itemId,
                        "stock" to 1,
                        "returnDate" to returnDate
                    ))
                }

                transaction.update(userRef, "takenItems", updatedTakenItems)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Return item: increases stock by 1 and removes from user's takenItems.
     */
    suspend fun returnItem(userId: String, itemId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users_real").document(userId)
            val itemRef = db.collection("items").document(itemId)

            db.runTransaction { transaction ->
                val userSnap = transaction.get(userRef)
                val itemSnap = transaction.get(itemRef)

                if (!itemSnap.exists()) throw Exception("Item does not exist")

                // 1. Update User's takenItems
                val takenItems = userSnap.get("takenItems") as? List<Map<String, Any>> ?: emptyList()
                val itemEntry = takenItems.find { it["itemId"] == itemId }
                    ?: throw Exception("User does not have this item borrowed")

                val currentUserStock = (itemEntry["stock"] as? Long)?.toInt() ?: 1
                val updatedTakenItems = if (currentUserStock > 1) {
                    // Decrement stock for this item in user list
                    takenItems.map {
                        if (it["itemId"] == itemId) {
                            it.toMutableMap().apply {
                                this["stock"] = currentUserStock - 1
                            }
                        } else it
                    }
                } else {
                    // Remove item from list
                    takenItems.filter { it["itemId"] != itemId }
                }
                transaction.update(userRef, "takenItems", updatedTakenItems)

                // 2. Update Item stock
                // Default to 3 if stock field is missing (legacy items)
                val currentItemStock = itemSnap.getLong("stock")?.toInt() ?: 3
                val newItemStock = currentItemStock + 1

                transaction.update(itemRef, mapOf(
                    "stock" to newItemStock,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ))

            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get map of item IDs to return dates borrowed by the user.
     */
    fun getUserBorrowedItemsInfo(userId: String): Flow<Map<String, Long>> = callbackFlow {
        val userRef = firestore.collection("users_real").document(userId)
        val listener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user borrowed items: ${error.message}")
                return@addSnapshotListener
            }

            val info = if (snapshot != null && snapshot.exists()) {
                val takenItems = snapshot.get("takenItems") as? List<Map<String, Any>> ?: emptyList()
                takenItems.mapNotNull {
                    val id = it["itemId"] as? String
                    val date = it["returnDate"] as? Long
                    if (id != null) id to (date ?: 0L) else null
                }.toMap()
            } else {
                emptyMap()
            }
            trySend(info)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Get map of item IDs to return dates borrowed by the user (One-shot fetch).
     */
    suspend fun getUserBorrowedItemsInfoOneShot(userId: String): Map<String, Long> {
        return try {
            val snapshot = firestore.collection("users_real").document(userId).get().await()
            if (snapshot.exists()) {
                val takenItems = snapshot.get("takenItems") as? List<Map<String, Any>> ?: emptyList()
                takenItems.mapNotNull {
                    val id = it["itemId"] as? String
                    val date = it["returnDate"] as? Long
                    if (id != null) id to (date ?: 0L) else null
                }.toMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching borrowed items one-shot: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Add a scan history entry for the user.
     */
    suspend fun addScanHistory(userId: String, historyItem: ee.ut.cs.iotbazaar.model.ScanHistoryItem): Result<Unit> {
        return try {
            val userRef = firestore.collection("users_real").document(userId)
            // Add to a subcollection "scan_history"
            userRef.collection("scan_history")
                .add(historyItem)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding scan history: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get scan history for the user.
     */
    fun getUserScanHistory(userId: String): Flow<List<ee.ut.cs.iotbazaar.model.ScanHistoryItem>> = callbackFlow {
        val userRef = firestore.collection("users_real").document(userId)
        val listener = userRef.collection("scan_history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to scan history: ${error.message}")
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(ee.ut.cs.iotbazaar.model.ScanHistoryItem::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(history)
            }
        awaitClose { listener.remove() }
    }
}
