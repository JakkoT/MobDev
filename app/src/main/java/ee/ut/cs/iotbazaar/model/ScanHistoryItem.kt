package ee.ut.cs.iotbazaar.model

data class ScanHistoryItem(
    val itemId: String = "",
    val itemName: String = "",
    val timestamp: Long = 0,
    val action: String = "" // "BORROW" or "RETURN"
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, "")
}

