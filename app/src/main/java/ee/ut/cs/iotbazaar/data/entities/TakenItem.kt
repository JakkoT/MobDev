package ee.ut.cs.iotbazaar.data.entities

import kotlinx.serialization.descriptors.PrimitiveKind

data class TakenItem(
    val itemId: String = "",
    val returnDate: Long = 0L,
    val stock: Int = 0
)