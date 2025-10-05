package ee.ut.cs.iotbazaar.model

/**
 * Basic domain model representing an item in the app.
 * Currently only holds an immutable id and name.
 */
data class Item(
    val id: Long,
    val name: String
)

