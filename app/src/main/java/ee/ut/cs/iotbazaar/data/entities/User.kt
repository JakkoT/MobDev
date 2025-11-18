package ee.ut.cs.iotbazaar.data.entities

// Data class representing a User
// Used for Firebase Firestore storage

// Data class representing a User for Firebase Firestore
data class User(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val takenItems: List<Map<String, Any>> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, emptyList(), System.currentTimeMillis())

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = hashMapOf(
        "uid" to id,
        "name" to name,
        "age" to age,
        "takenItems" to takenItems,
        "createdAt" to createdAt
    )

    companion object {
        // Create User from Firestore document
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
