package ee.ut.cs.iotbazaar.model

// Data class representing an IoT item that a user can reserve/borrow.
// Used for Firebase Firestore storage
data class Item(
    val id: String = "", // Firebase document ID
    val name: String = "",
    val stock: Int = 0,
    val returnDate: Long? = null // Only used for UI display of borrowed items
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, null)

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = hashMapOf(
        "name" to name,
        "stock" to stock
    )

    companion object {
        // Create Item from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): Item {
            return Item(
                id = id,
                name = map["name"] as? String ?: "",
                // Safely cast Number (Long/Int) to Int, default to 3 for legacy items
                stock = (map["stock"] as? Number)?.toInt() ?: 3,
                returnDate = null
            )
        }
    }
}
