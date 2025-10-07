package ee.ut.cs.iotbazaar.data.entities

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int
)