package ee.ut.cs.iotbazaar.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.data.database.AppDatabase
import ee.ut.cs.iotbazaar.data.entities.User
import ee.ut.cs.iotbazaar.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the database and repository
    private val db = AppDatabase.getInstance(application)
    private val repository = UserRepository(db.userDao())
    // LiveData to observe the list of users
    val users: LiveData<List<User>> get() = repository.getAllUsers().asLiveData()

    // LiveData to add new user
    fun addUser(name: String, age: Int) = viewModelScope.launch {
        repository.insert(User(name = name, age = age))

    }
    // Function to add a new user if it doesn't exist
    fun addUserIfNotExists(name: String, age: Int) = viewModelScope.launch {
        repository.insertIfNotExists(name, age)
    }
}