package ee.ut.cs.iotbazaar.model

// Data class representing an IoT item that a user can reserve/borrow.
// Used for Firebase Firestore storage
data class Item(
    val id: String = "", // Firebase document ID
    val name: String = "",
    val reserved: Boolean = false,
    val stock: Int = 0
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", false)

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = hashMapOf(
        "name" to name,
        "reserved" to reserved,
        "stock" to stock
    )

    companion object {
        // Create Item from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): Item {
            return Item(
                id = id,
                name = map["name"] as? String ?: "",
                reserved = map["reserved"] as? Boolean ?: false,
                stock = map["stock"] as? Int ?: 1
            )
        }
    }
}
