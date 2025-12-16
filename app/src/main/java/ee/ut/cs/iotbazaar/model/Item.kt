package ee.ut.cs.iotbazaar.model

/**
 * Data class representing an IoT item that a user can reserve/borrow.
 * Used for Firebase Firestore storage and UI display.
 *
 * @property id The unique document ID from Firestore.
 * @property name The name of the item.
 * @property stock The current available stock quantity.
 * @property returnDate The timestamp when the item is due for return (only relevant for borrowed items).
 */
data class Item(
    val id: String = "", // Firebase document ID
    val name: String = "",
    val stock: Int = 0,
    val returnDate: Long? = null // Only used for UI display of borrowed items
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, null)

    /**
     * Converts the Item object to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any> = hashMapOf(
        "name" to name,
        "stock" to stock
    )

    companion object {
        /**
         * Creates an Item object from a Firestore document map.
         * Handles potential type mismatches and default values.
         *
         * @param id The document ID.
         * @param map The data map from the document.
         */
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
