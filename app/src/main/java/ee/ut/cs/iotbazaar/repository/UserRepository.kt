package ee.ut.cs.iotbazaar.repository

import ee.ut.cs.iotbazaar.data.dao.UserDao
import ee.ut.cs.iotbazaar.data.entities.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> = userDao.getAll()
    suspend fun insert(user: User) = userDao.insert(user)
    suspend fun delete(user: User) = userDao.delete(user)
}