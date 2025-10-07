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
    private val db = AppDatabase.getInstance(application)
    private val repository = UserRepository(db.userDao())
    val users: LiveData<List<User>> get() = repository.getAllUsers().asLiveData()


    fun addUser(name: String, age: Int) = viewModelScope.launch {
        repository.insert(User(name = name, age = age))

    }

    fun addUserIfNotExists(name: String, age: Int) = viewModelScope.launch {
        repository.insertIfNotExists(name, age)
    }
}