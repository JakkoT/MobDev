package ee.ut.cs.iotbazaar.repository

import ee.ut.cs.iotbazaar.data.dao.ItemDao
import ee.ut.cs.iotbazaar.model.Item
import kotlinx.coroutines.flow.Flow

// Repository class that abstracts access to multiple data sources.
// In this case, it provides a clean API for data access to the rest of the application
// by interacting with the ItemDao to perform database operations related to Item entities.
class ItemRepository(private val itemDao: ItemDao) {
    fun getAllItems(): Flow<List<Item>> = itemDao.getAll()
    suspend fun insert(name: String, reserved: Boolean = false) = itemDao.insert(Item(name = name, reserved = reserved))
    suspend fun delete(item: Item) = itemDao.delete(item)
    suspend fun count(): Int = itemDao.count()
}
