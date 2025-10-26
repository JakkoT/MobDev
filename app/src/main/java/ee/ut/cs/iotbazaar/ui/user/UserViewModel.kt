package ee.ut.cs.iotbazaar.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.data.entities.User
import ee.ut.cs.iotbazaar.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the repository
    private val repository = UserRepository()

    // LiveData to observe the list of users
    val users: LiveData<List<User>> = repository.getAllUsers().asLiveData()

    // Function to add a new user
    fun addUser(name: String, age: Int) = viewModelScope.launch {
        repository.insert(User(name = name, age = age))
    }

    // Function to add a new user if it doesn't exist
    fun addUserIfNotExists(name: String, age: Int) = viewModelScope.launch {
        repository.insertIfNotExists(name, age)
    }

    // Function to delete a user
    fun deleteUser(user: User) = viewModelScope.launch {
        repository.delete(user)
    }
}