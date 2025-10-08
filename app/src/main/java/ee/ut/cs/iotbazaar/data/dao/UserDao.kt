package ee.ut.cs.iotbazaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ee.ut.cs.iotbazaar.data.entities.User
import kotlinx.coroutines.flow.Flow

// Data Access Object (DAO) for the User entity
// Provides methods to interact with the User table in the database
// Includes methods to get all users, insert a new user, delete a user, and count
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Insert
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT COUNT(*) FROM user WHERE name = :name")
    suspend fun countByName(name: String): Int
}