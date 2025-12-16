package ee.ut.cs.iotbazaar.model

/**
 * Data class representing a single entry in the user's scan history.
 * Used for storing and retrieving history from Firestore.
 *
 * @property itemId The ID of the scanned item.
 * @property itemName The name of the scanned item.
 * @property timestamp The time when the scan occurred (in milliseconds).
 * @property action The type of action performed ("BORROW" or "RETURN").
 */
data class ScanHistoryItem(
    val itemId: String = "",
    val itemName: String = "",
    val timestamp: Long = 0,
    val action: String = "" // "BORROW" or "RETURN"
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, "")
}
