package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.data.database.AppDatabase
import ee.ut.cs.iotbazaar.model.Item
import ee.ut.cs.iotbazaar.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the database and repository
    private val db = AppDatabase.getInstance(application)
    private val repository = ItemRepository(db.itemDao())

    // LiveData to observe the list of items
    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()

    // Function to add a new item
    fun addItem(name: String) = viewModelScope.launch {
        repository.insert(name)
    }
    // Function to delete an item
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }
    // Function to seed the database with initial data if it's empty for now
    fun seedIfEmpty() = viewModelScope.launch {
        if (repository.count() == 0) {
            listOf("ESP32 Dev Board", "Raspberry Pi 4", "Arduino Nano", "LoRa Sensor", "BLE Beacon")
                .forEach { repository.insert(it) }
        }
    }
}
