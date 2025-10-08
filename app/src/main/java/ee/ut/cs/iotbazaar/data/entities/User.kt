package ee.ut.cs.iotbazaar.data.entities

import androidx.room.PrimaryKey
import androidx.room.Entity

// Entity class representing a User in the database
// Defines the schema for the User table with fields for id, name, and age
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int
)