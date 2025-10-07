package ee.ut.cs.iotbazaar.repository

import ee.ut.cs.iotbazaar.data.dao.ItemDao
import ee.ut.cs.iotbazaar.model.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {
    fun getAllItems(): Flow<List<Item>> = itemDao.getAll()
    suspend fun insert(name: String) = itemDao.insert(Item(name = name))
    suspend fun delete(item: Item) = itemDao.delete(item)
    suspend fun count(): Int = itemDao.count()
}
