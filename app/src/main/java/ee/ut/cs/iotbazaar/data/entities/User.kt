package ee.ut.cs.iotbazaar.data.entities

// Data class representing a User
// Used for Firebase Firestore storage
data class User(
    val id: String = "", // Firebase document ID
    val name: String = "",
    val age: Int = 0
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0)

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = hashMapOf(
        "name" to name,
        "age" to age
    )

    companion object {
        // Create User from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): User {
            return User(
                id = id,
                name = map["name"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0
            )
        }
    }
}
