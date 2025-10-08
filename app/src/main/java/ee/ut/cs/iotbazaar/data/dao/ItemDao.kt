package ee.ut.cs.iotbazaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ee.ut.cs.iotbazaar.model.Item
import kotlinx.coroutines.flow.Flow

// Data Access Object (DAO) for the Item entity
// Provides methods to interact with the Item table in the database
// Includes methods to get all items, insert a new item, delete an item, and count total items
@Dao
interface ItemDao {
    @Query("SELECT * FROM item")
    fun getAll(): Flow<List<Item>>

    @Insert
    suspend fun insert(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT COUNT(*) FROM item")
    suspend fun count(): Int
}
