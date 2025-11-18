package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ee.ut.cs.iotbazaar.model.Item
import ee.ut.cs.iotbazaar.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the repository
    private val repository = ItemRepository()

    // LiveData to observe the list of items
    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()
    private val _reserveResult = MutableLiveData<Result<Unit>>()
    val reserveResult: LiveData<Result<Unit>> = _reserveResult

    fun addItem(name: String) = addItem(name, false)


    suspend fun getItem(id: String): Item? {
        return repository.getItemById(id)
    }

    fun addItem(name: String, reserved: Boolean) = viewModelScope.launch {
        repository.insert(name, reserved)
    }

    // Function to delete an item
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    // Function to update an item (for example toggle reserved status)
    fun updateItem(item: Item) = viewModelScope.launch {
        repository.update(item)
    }
    fun reserveItemForCurrentUser(itemId: String, returnDate: Long) = viewModelScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _reserveResult.postValue(Result.failure(Exception("Kasutaja ei ole sisse logitud")))
            return@launch
        }

        val result = repository.reserveItemForUser(userId, itemId, returnDate)
        _reserveResult.postValue(result)
    }
    fun reserveItemForUser(userId: String, itemId: String, returnDate: Long) = viewModelScope.launch {
        val result = repository.reserveItemForUser(userId, itemId, returnDate)
        _reserveResult.postValue(result)
    }


    // Central list of reserved sample items (all reserved)
    private val reservedSeedItems = listOf(
        "ESP32 Dev Board",
        "Raspberry Pi 4",
        "Arduino Nano",
        "LoRa Sensor",
        "BLE Beacon",
        "Smart Thermostat",
        "Zigbee Gateway",
        "NB-IoT Tracker",
        "Industrial Sensor Node"
    )

    /**
     * Ensure all reserved seed items exist. If DB empty, this simply inserts them all;
     * if partially populated, only missing ones are added.
     */
    fun ensureReservedSeedItems() = viewModelScope.launch {
        for (name in reservedSeedItems) {
            if (!repository.exists(name)) {
                repository.insert(name, reserved = true)
            }
        }
    }
}