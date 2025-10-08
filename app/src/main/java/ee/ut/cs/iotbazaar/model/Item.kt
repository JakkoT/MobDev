package ee.ut.cs.iotbazaar.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity representing an IoT item that a user can reserve/borrow.
@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val reserved: Boolean = false //field indicating whether the item is currently reserved
)
