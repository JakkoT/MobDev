package ee.ut.cs.iotbazaar.data.entities

/**
 * Data class representing a User for Firebase Firestore.
 *
 * @property id The unique user ID (UID).
 * @property name The display name of the user.
 * @property age The age of the user.
 * @property takenItems A list of maps representing items borrowed by the user.
 * @property createdAt Timestamp of user creation.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val takenItems: List<Map<String, Any>> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, emptyList(), System.currentTimeMillis())

    /**
     * Converts the User object to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any> = hashMapOf(
        "uid" to id,
        "name" to name,
        "age" to age,
        "takenItems" to takenItems,
        "createdAt" to createdAt
    )

    companion object {
        /**
         * Creates a User object from a Firestore document map.
         *
         * @param id The document ID.
         * @param map The data map from the document.
         */
        fun fromMap(id: String, map: Map<String, Any>): User {
            return User(
                id = id,
                name = map["name"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                takenItems = map["takenItems"] as? List<Map<String, Any>> ?: emptyList(),
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}
