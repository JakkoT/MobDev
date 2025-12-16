package ee.ut.cs.iotbazaar.data.entities

import kotlinx.serialization.descriptors.PrimitiveKind

/**
 * Data class representing an item taken (borrowed) by a user.
 * Note: Currently, the app uses `Map<String, Any>` in `User` class for flexibility with Firestore,
 * but this class can be used for stricter typing in future refactoring.
 *
 * @property itemId The ID of the borrowed item.
 * @property returnDate The timestamp when the item is due.
 * @property stock The quantity borrowed.
 */
data class TakenItem(
    val itemId: String = "",
    val returnDate: Long = 0L,
    val stock: Int = 0
)