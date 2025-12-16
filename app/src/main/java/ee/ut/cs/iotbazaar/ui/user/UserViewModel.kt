package ee.ut.cs.iotbazaar.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.data.entities.User
import ee.ut.cs.iotbazaar.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing User data.
 * Handles interactions between the UI and the UserRepository.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the repository
    private val repository = UserRepository()

    /**
     * LiveData observing the list of all users.
     */
    val users: LiveData<List<User>> = repository.getAllUsers().asLiveData()

    /**
     * Adds a new user to the repository.
     *
     * @param name The name of the user.
     * @param age The age of the user.
     */
    fun addUser(name: String, age: Int) = viewModelScope.launch {
        repository.insert(User(name = name, age = age))
    }

    /**
     * Adds a new user if a user with the same name does not already exist.
     *
     * @param name The name of the user.
     * @param age The age of the user.
     */
    fun addUserIfNotExists(name: String, age: Int) = viewModelScope.launch {
        repository.insertIfNotExists(name, age)
    }

    /**
     * Deletes a user from the repository.
     *
     * @param user The user object to delete.
     */
    fun deleteUser(user: User) = viewModelScope.launch {
        repository.delete(user)
    }
}