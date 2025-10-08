package ee.ut.cs.iotbazaar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ee.ut.cs.iotbazaar.data.dao.UserDao
import ee.ut.cs.iotbazaar.data.dao.ItemDao
import ee.ut.cs.iotbazaar.data.entities.User
import ee.ut.cs.iotbazaar.model.Item

// Room database class that holds the database and serves as the main access point
// for the underlying connection to the app's persisted data.
// Defines the database configuration and serves as the app's main access point to the persisted data.
@Database(entities = [User::class, Item::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance: AppDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myapp_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}